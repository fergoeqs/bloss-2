package org.fergoeqs.blps1.controllers;

import jakarta.validation.Valid;
import org.fergoeqs.blps1.dto.ApplicationRequest;
import org.fergoeqs.blps1.dto.ApplicationResponse;
import org.fergoeqs.blps1.services.ApplicationService;
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
            @Valid @RequestBody ApplicationRequest request) {

        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vacancy/{vacancyId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByVacancyId(@PathVariable Long vacancyId) {
        List<ApplicationResponse> applications = applicationService.getApplicationsByVacancyId(vacancyId);
        return ResponseEntity.ok(applications);
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
            @RequestParam String coverLetter) {

        if (coverLetter == null || coverLetter.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ApplicationResponse response = applicationService.addCoverLetter(id, coverLetter);
        return ResponseEntity.ok(response);
    }
}