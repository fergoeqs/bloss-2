package org.fergoeqs.blps1.controllers;

import jakarta.validation.Valid;
import org.fergoeqs.blps1.dto.VacancyRequest;
import org.fergoeqs.blps1.dto.VacancyResponse;
import org.fergoeqs.blps1.models.employerdb.Vacancy;
import org.fergoeqs.blps1.models.securitydb.User;
import org.fergoeqs.blps1.services.VacancyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER_CREATOR')")
    public ResponseEntity<VacancyResponse> createVacancy(
            @Valid @RequestBody VacancyRequest request,
            @AuthenticationPrincipal User user) {
        VacancyResponse response = vacancyService.createVacancy(request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("{id}/open")
    @PreAuthorize("hasAnyRole('EMPLOYER_CREATOR', 'EMPLOYER_REVIEWER')")
    public ResponseEntity<Vacancy> openVacancy(@PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.openVacancy(id));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('EMPLOYER_CREATOR', 'EMPLOYER_REVIEWER')")
    public ResponseEntity<?> closeVacancy(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Vacancy vacancy = vacancyService.closeVacancy(id, pageable);
            return ResponseEntity.ok(vacancy);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER_CREATOR')")
    public ResponseEntity<?> deleteVacancy(@PathVariable Long id) {
        vacancyService.deleteVacancy(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllVacancies(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Page<Vacancy> vacancies = vacancyService.getAllVacancies(PageRequest.of(page, size));
        return ResponseEntity.ok(vacancies.getContent());
    }

    @GetMapping("/actual")
    public ResponseEntity<?> getOpenVacancies(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        Page<Vacancy> vacancies = vacancyService.getAllByStatusIsOpen(PageRequest.of(page, size));
        return ResponseEntity.ok(vacancies.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacancy> getVacancyById(@PathVariable Long id) {
        return vacancyService.getVacancyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/close-by-issue")
    public ResponseEntity<?> closeVacancyByIssueKey(@RequestBody Map<String, String> request) {
        String issueKey = request.get("issueKey");
        vacancyService.closeVacancyByIssueKey(issueKey);
        return ResponseEntity.ok().build();
    }
}