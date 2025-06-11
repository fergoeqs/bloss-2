package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.dto.VacancyRequest;
import org.fergoeqs.blps1.dto.VacancyResponse;
import org.fergoeqs.blps1.exceptions.ResourceNotFoundException;
import org.fergoeqs.blps1.models.applicantdb.Application;
import org.fergoeqs.blps1.models.employerdb.Employer;
import org.fergoeqs.blps1.models.employerdb.Vacancy;
import org.fergoeqs.blps1.models.enums.ApplicationStatus;
import org.fergoeqs.blps1.models.enums.Role;
import org.fergoeqs.blps1.models.enums.VacancyStatus;
import org.fergoeqs.blps1.repositories.applicantdb.ApplicationRepository;
import org.fergoeqs.blps1.repositories.employerdb.EmployerRepository;
import org.fergoeqs.blps1.repositories.employerdb.VacancyRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final EmployerRepository employerRepository;
    private final ApplicationRepository applicationRepository;
    private final TransactionService transactionService;


    public VacancyService(VacancyRepository vacancyRepository, EmployerRepository employerRepository,
                          ApplicationRepository applicationRepository, TransactionService transactionService) {
        this.vacancyRepository = vacancyRepository;
        this.employerRepository = employerRepository;
        this.applicationRepository = applicationRepository;
        this.transactionService = transactionService;
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

    public VacancyResponse createVacancy(VacancyRequest request, Long userId) {
        return transactionService.execute("createVacancy", 30, (status) -> {
                    Employer employer = employerRepository.findByUserId(userId)
                            .orElseThrow(() -> new AccessDeniedException("User is not an employer"));

                    if (employer.getRole() != Role.EMPLOYER_CREATOR) {
                        throw new AccessDeniedException("Only creators can create vacancies");
                    }

                    if (request.title() == null || request.title().isBlank()) {
                        throw new IllegalArgumentException("Title cannot be empty");
                    }

                    Vacancy vacancy = new Vacancy();
                    vacancy.setPendingLimit(request.pendingLimit() != null ?
                            request.pendingLimit() : 100);
                    vacancy.setStatus(VacancyStatus.OPEN);
                    vacancy.setTitle(request.title());
                    vacancy.setDescription(request.description());
                    vacancy.setResumeRequired(request.resumeRequired());
                    vacancy.setCoverLetterRequired(request.coverLetterRequired());
                    vacancy.setKeywords(request.keywords());
                    vacancy.setEmployer(employer);

                    Vacancy savedVacancy = vacancyRepository.save(vacancy);

                    return new VacancyResponse(
                            savedVacancy.getId(),
                            savedVacancy.getTitle(),
                            savedVacancy.getDescription(),
                            savedVacancy.isResumeRequired(),
                            savedVacancy.isCoverLetterRequired()
                    );
                }
        );
    }

    public void deleteVacancy(Long id) {
        transactionService.execute("deleteVacancy", 20, status -> {
                    vacancyRepository.deleteById(id);
                    return null;
                });
    }

    public Vacancy openVacancy(Long id) {
        return transactionService.execute("openVacancy", 30, status -> {
                    Vacancy vacancy = vacancyRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Vacancy not found with id: " + id));

                    if (vacancy.getStatus() == VacancyStatus.OPEN) {
                        return vacancy;
                    }
                    vacancy.setStatus(VacancyStatus.OPEN);
                return vacancyRepository.save(vacancy);
                });
    }

    public Vacancy closeVacancy(Long id, Pageable pageable) {
        return transactionService.execute("closeVacancy", 30, status -> {
                    Vacancy vacancy = vacancyRepository.findById(id).orElseThrow(()
                            -> new IllegalArgumentException("Vacancy not found"));

                    Page<Application> applications = applicationRepository.findByVacancyId(vacancy.getId(), pageable);
                    applications.stream()
                            .filter(app -> app.getStatus() == ApplicationStatus.PENDING
                                    || app.getStatus() == ApplicationStatus.PENDING_WITH_WARNING)
                            .forEach(app -> {
                                app.setStatus(ApplicationStatus.REJECTED);
                                applicationRepository.save(app);
                            });
                    vacancy.setStatus(VacancyStatus.CLOSED);
                    return vacancy;
                }
        );
    }

    public void closeVacancyByIssueKey(String issueKey) {
        Application application = applicationRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        Vacancy vacancy = vacancyRepository.findById(application.getVacancyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        vacancy.setStatus(VacancyStatus.CLOSED);
        vacancyRepository.save(vacancy);
    }

}