package org.fergoeqs.blps1.controllers;

import jakarta.validation.Valid;
import org.fergoeqs.blps1.dto.VacancyRequest;
import org.fergoeqs.blps1.dto.VacancyResponse;
import org.fergoeqs.blps1.models.Vacancy;
import org.fergoeqs.blps1.services.VacancyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @PostMapping
    public ResponseEntity<VacancyResponse> createVacancy(
            @Valid @RequestBody VacancyRequest request) {
        VacancyResponse response = vacancyService.createVacancy(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("{id}/open")
    public ResponseEntity<Vacancy> openVacancy(@PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.openVacancy(id));
    }

    @PutMapping("{id}/close")
    public ResponseEntity<Vacancy> closeVacancy(@PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.closeVacancy(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVacancy(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Vacancy>> getAllVacancies() {
        List<Vacancy> vacancies = vacancyService.getAllVacancies();
        return ResponseEntity.ok(vacancies);
    }

    @GetMapping("/actual")
    public ResponseEntity<List<Vacancy>> getOpenVacancies() {
        List<Vacancy> vacancies = vacancyService.getAllByStatusIsOpen();
        return ResponseEntity.ok(vacancies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacancy> getVacancyById(@PathVariable Long id) {
        return vacancyService.getVacancyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}