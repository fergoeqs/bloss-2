package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.dto.VacancyRequest;
import org.fergoeqs.blps1.dto.VacancyResponse;
import org.fergoeqs.blps1.models.employerdb.Employer;
import org.fergoeqs.blps1.models.employerdb.Vacancy;
import org.fergoeqs.blps1.models.enums.Role;
import org.fergoeqs.blps1.models.enums.VacancyStatus;
import org.fergoeqs.blps1.repositories.employerdb.EmployerRepository;
import org.fergoeqs.blps1.repositories.employerdb.VacancyRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Transactional(readOnly = true)
@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final EmployerRepository employerRepository;


    public VacancyService(VacancyRepository vacancyRepository, EmployerRepository employerRepository) {
        this.vacancyRepository = vacancyRepository;
        this.employerRepository = employerRepository;
    }

    @Transactional
    public VacancyResponse createVacancy(VacancyRequest request, Long userId) {
        Employer employer = employerRepository.findByUserId(userId)
                .orElseThrow(() -> new AccessDeniedException("User is not an employer"));

        if (employer.getRole() != Role.EMPLOYER_CREATOR) {
            throw new AccessDeniedException("Only creators can create vacancies");
        }

        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        Vacancy vacancy = new Vacancy();
        vacancy.setStatus(VacancyStatus.OPEN);
        vacancy.setTitle(request.title());
        vacancy.setDescription(request.description());
        vacancy.setResumeRequired(request.resumeRequired());
        vacancy.setCoverLetterRequired(request.coverLetterRequired());
        vacancy.setKeywords(request.keywords());


        Vacancy savedVacancy = vacancyRepository.save(vacancy);


        return new VacancyResponse(
                savedVacancy.getId(),
                savedVacancy.getTitle(),
                savedVacancy.getDescription(),
                savedVacancy.isResumeRequired(),
                savedVacancy.isCoverLetterRequired());
    }

    @Transactional
    public void deleteVacancy(Long id) {
        vacancyRepository.deleteById(id);
    }

    @Transactional
    public Vacancy openVacancy(Long id) {
        Vacancy vacancy = vacancyRepository.findById(id).orElseThrow(()
                -> new IllegalArgumentException("Vacancy not found"));
        vacancy.setStatus(VacancyStatus.OPEN);
        return vacancy;
    }

    @Transactional
    public Vacancy closeVacancy(Long id) {
        Vacancy vacancy = vacancyRepository.findById(id).orElseThrow(()
                -> new IllegalArgumentException("Vacancy not found"));
        vacancy.setStatus(VacancyStatus.CLOSED);
        return vacancy;
    }

    public Page<Vacancy> getAllVacancies(Pageable pageable) {
        return vacancyRepository.findAll(pageable);
    }

    public Page<Vacancy> getAllByStatusIsOpen(Pageable pageable) {
        return vacancyRepository.findAllByStatus(VacancyStatus.OPEN, pageable);
    }

    public Optional<Vacancy> getVacancyById(Long id) {
        return vacancyRepository.findById(id);
    }
}