package org.fergoeqs.blps1.controllers;

import jakarta.validation.Valid;
import org.fergoeqs.blps1.dto.ApplicationRequest;
import org.fergoeqs.blps1.dto.ApplicationResponse;
import org.fergoeqs.blps1.services.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationRequest request) throws Exception {

        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
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