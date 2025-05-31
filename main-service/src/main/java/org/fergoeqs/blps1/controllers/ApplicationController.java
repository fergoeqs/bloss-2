package org.fergoeqs.blps1.controllers;

import jakarta.validation.Valid;
import org.fergoeqs.blps1.dto.ApplicationRequest;
import org.fergoeqs.blps1.dto.ApplicationResponse;
import org.fergoeqs.blps1.services.ApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JmsTemplate jmsTemplate;
    private final String applicationQueue;

    public ApplicationController(
            JmsTemplate jmsTemplate,
            @Value("${app.queue.application}") String applicationQueue, ApplicationService applicationService) {

        this.jmsTemplate = jmsTemplate;
        this.applicationQueue = applicationQueue;
        this.applicationService = applicationService;

    }


    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationRequest request) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        jmsTemplate.convertAndSend(applicationQueue, request, message -> {
            message.setJMSCorrelationID(correlationId);
            return message;
        });
        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.ok(response);
//        return ResponseEntity.accepted().body(
//                new ApplicationResponse(
//                        correlationId,
//                        "Ваша заявка принята в обработку"
//                )
//        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApplicationResponse> deleteApplication(@PathVariable Long id) throws Exception {
        applicationService.deleteApplication(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/vacancy/{vacancyId}")
    public ResponseEntity<?> getApplicationsByVacancyId(@PathVariable Long vacancyId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Page<ApplicationResponse> applications = applicationService.getApplicationsByVacancyId(vacancyId, PageRequest.of(page, size));
        return ResponseEntity.ok(applications.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable Long id) {
        return applicationService.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cover-letter")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApplicationResponse> addCoverLetter(
            @PathVariable Long id,
            @RequestBody String coverLetter) {

        if (coverLetter == null || coverLetter.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ApplicationResponse response = applicationService.addCoverLetter(id, coverLetter);
        return ResponseEntity.ok(response);
    }
}