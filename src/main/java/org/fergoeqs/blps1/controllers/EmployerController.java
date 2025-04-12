package org.fergoeqs.blps1.controllers;

import org.fergoeqs.blps1.dto.ApplicationResponse;
import org.fergoeqs.blps1.models.Application;
import org.fergoeqs.blps1.services.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer")
public class EmployerController {

    private final ApplicationService applicationService;

    public EmployerController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/vacancies/{vacancyId}/applications")
    public ResponseEntity<?> getApplicationsByVacancyId(@PathVariable Long vacancyId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        Page<ApplicationResponse> applications = applicationService.getApplicationsByVacancyId(vacancyId, PageRequest.of(page, size));
        return ResponseEntity.ok(applications.getContent());
    }

    @PutMapping("/applications/{id}/accept")
    public ResponseEntity<ApplicationResponse> acceptApplication(@PathVariable Long id) {
        ApplicationResponse updatedApplication = applicationService.acceptApplication(id);
        return ResponseEntity.ok(updatedApplication);
    }

    @PutMapping("/applications/{id}/reject")
    public ResponseEntity<ApplicationResponse> rejectApplication(@PathVariable Long id) {
        ApplicationResponse updatedApplication = applicationService.rejectApplication(id);
        return ResponseEntity.ok(updatedApplication);
    }
}