package org.fergoeqs.blps1.controllers;

import org.fergoeqs.blps1.dto.ApplicationResponse;
import org.fergoeqs.blps1.models.securitydb.User;
import org.fergoeqs.blps1.services.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer")
@PreAuthorize("hasRole('EMPLOYER_REVIEWER')")
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
    @PreAuthorize("hasAnyRole('EMPLOYER_CREATOR', 'EMPLOYER_REVIEWER')")
    public ResponseEntity<ApplicationResponse> acceptApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        ApplicationResponse updatedApplication = applicationService.acceptApplication(id, user.getId());
        return ResponseEntity.ok(updatedApplication);
    }

    @PutMapping("/applications/{id}/reject")
    @PreAuthorize("hasAnyRole('EMPLOYER_CREATOR', 'EMPLOYER_REVIEWER')")
    public ResponseEntity<ApplicationResponse> rejectApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        ApplicationResponse updatedApplication = applicationService.rejectApplication(id, user.getId());
        return ResponseEntity.ok(updatedApplication);
    }
}