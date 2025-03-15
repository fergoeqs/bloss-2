package org.fergoeqs.blps1.controllers;

import org.fergoeqs.blps1.models.Application;
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
    public ResponseEntity<Application> createApplication(
            @RequestParam Long vacancyId,
            @RequestParam Long applicantId,
            @RequestParam(required = false) Long resumeId,
            @RequestParam(required = false) String coverLetter) {
        Application application = applicationService.createApplication(vacancyId, applicantId, resumeId, coverLetter);
        return ResponseEntity.ok(application);
    }

    @GetMapping("/vacancy/{vacancyId}")
    public ResponseEntity<List<Application>> getApplicationsByVacancyId(@PathVariable Long vacancyId) {
        List<Application> applications = applicationService.getApplicationsByVacancyId(vacancyId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        return applicationService.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}