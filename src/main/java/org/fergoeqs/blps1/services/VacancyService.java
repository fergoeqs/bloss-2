package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.dto.VacancyRequest;
import org.fergoeqs.blps1.dto.VacancyResponse;
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

    public VacancyResponse createVacancy(VacancyRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        Vacancy vacancy = new Vacancy();
        vacancy.setTitle(request.title());
        vacancy.setDescription(request.description());
        vacancy.setResumeRequired(request.isResumeRequired());
        vacancy.setCoverLetterRequired(request.isCoverLetterRequired());
        vacancy.setKeywords(request.keywords());


        Vacancy savedVacancy = vacancyRepository.save(vacancy);


        return new VacancyResponse(
                savedVacancy.getId(),
                savedVacancy.getTitle(),
                savedVacancy.getDescription(),
                savedVacancy.isResumeRequired(),
                savedVacancy.isCoverLetterRequired());
    }

    public List<Vacancy> getAllVacancies() {
        return vacancyRepository.findAll();
    }

    public Optional<Vacancy> getVacancyById(Long id) {
        return vacancyRepository.findById(id);
    }
}