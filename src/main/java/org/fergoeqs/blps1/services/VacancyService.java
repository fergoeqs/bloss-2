package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.models.Vacancy;
import org.fergoeqs.blps1.repositories.VacancyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    public VacancyService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    public Vacancy createVacancy(Vacancy vacancy) {
        return vacancyRepository.save(vacancy);
    }

    public List<Vacancy> getAllVacancies() {
        return vacancyRepository.findAll();
    }

    public Optional<Vacancy> getVacancyById(Long id) {
        return vacancyRepository.findById(id);
    }
}