package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.dto.ApplicationRequest;
import org.fergoeqs.blps1.dto.ApplicationResponse;
import org.fergoeqs.blps1.exceptions.ResourceNotFoundException;
import org.fergoeqs.blps1.models.applicantdb.Applicant;
import org.fergoeqs.blps1.models.applicantdb.Application;
import org.fergoeqs.blps1.models.applicantdb.Resume;
import org.fergoeqs.blps1.models.employerdb.Employer;
import org.fergoeqs.blps1.models.employerdb.Vacancy;
import org.fergoeqs.blps1.models.enums.ApplicationStatus;
import org.fergoeqs.blps1.repositories.applicantdb.ApplicantRepository;
import org.fergoeqs.blps1.repositories.applicantdb.ApplicationRepository;
import org.fergoeqs.blps1.repositories.applicantdb.ResumeRepository;
import org.fergoeqs.blps1.repositories.employerdb.EmployerRepository;
import org.fergoeqs.blps1.repositories.employerdb.VacancyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final ApplicantRepository applicantRepository;
    private final ResumeRepository resumeRepository;
    private final EmployerRepository employerRepository;
    private final TransactionService transactionService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              VacancyRepository vacancyRepository,
                              ApplicantRepository applicantRepository,
                              ResumeRepository resumeRepository, EmployerRepository employerRepository,
                              TransactionService transactionService) {
        this.applicationRepository = applicationRepository;
        this.vacancyRepository = vacancyRepository;
        this.applicantRepository = applicantRepository;
        this.resumeRepository = resumeRepository;
        this.employerRepository = employerRepository;
        this.transactionService = transactionService;
    }

    public ApplicationResponse createApplication(ApplicationRequest request) {
        return transactionService.execute(
                "createApplication",
                30,
                status -> {
                    Application application = new Application();

                    Vacancy vacancy = vacancyRepository.findById(request.vacancyId())
                            .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

                    if (vacancy.getPendingCount() >= vacancy.getPendingLimit()) {
                        throw new IllegalStateException("Vacancy has reached application limit");
                    }

                    Applicant applicant = applicantRepository.findById(request.applicantId())
                            .orElseThrow(() -> new ResourceNotFoundException("Applicant not found"));

                    application.setVacancyId(request.vacancyId());
                    application.setApplicant(applicant);

                    String warning = null;
                    if (vacancy.isResumeRequired()) {
                        Resume resume;
                        if (applicant.getResumes() == null || applicant.getResumes().isEmpty()) {
                            application.setStatus(ApplicationStatus.RESUME_REQUIRED);
                            throw new IllegalStateException("Resume required");
                        } else {
                            if (applicant.getResumes().size() > 1) {
                                if (request.resumeId() == null) {
                                    resume = applicant.getResumes().get(0);
                                } else {
                                    resume = resumeRepository.findById(request.resumeId())
                                            .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

                                    if (!resume.getApplicant().getId().equals(applicant.getId())) {
                                        throw new IllegalStateException("Resume doesn't belong to applicant");
                                    }
                                }
                            } else {
                                resume = applicant.getResumes().get(0);
                            }
                            application.setResume(resume);
                            if (!isResumeMatching(resume, vacancy)) {
                                warning = "The resume does not meet the requirements of the vacancy. The application may be rejected.";
                            }
                        }
                    }

                    if (vacancy.isCoverLetterRequired() &&
                            (request.coverLetter() == null || request.coverLetter().isBlank())) {
                        application.setStatus(ApplicationStatus.COVER_LETTER_REQUIRED);
                        throw new IllegalStateException("Cover letter required");
                    } else {
                        application.setCoverLetter(request.coverLetter());
                    }

                    if (application.getStatus() == null) {
                        application.setStatus(warning != null ?
                                ApplicationStatus.PENDING_WITH_WARNING :
                                ApplicationStatus.PENDING);
                    }
                    vacancy.setPendingCount(vacancy.getPendingCount() + 1);
                    vacancyRepository.save(vacancy);
                    Application saved = applicationRepository.save(application);
                    return mapToResponse(saved);
                }
        );
    }

    public void deleteApplication(Long applicationId) {
        transactionService.execute("deleteApplication", 10, status -> {
                    applicationRepository.deleteById(applicationId);
                    return null;
                }
        );
    }

    public Page<ApplicationResponse> getApplicationsByVacancyId(Long vacancyId, Pageable pageable) {
        return applicationRepository.findByVacancyId(vacancyId, pageable)
                .map(this::mapToResponse);
    }

    public Optional<ApplicationResponse> getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .map(this::mapToResponse);
    }

    public ApplicationResponse acceptApplication(Long applicationId, Long userId) {
        return transactionService.execute("acceptApplication", 30, status -> {
                    Application application = applicationRepository.findById(applicationId)
                            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

                    Employer reviewer = employerRepository.findByUserId(userId)
                            .orElseThrow(() -> new AccessDeniedException("Not an employer"));

                    if (application.getStatus() == ApplicationStatus.PENDING
                            || application.getStatus() == ApplicationStatus.PENDING_WITH_WARNING) {

                        Vacancy vacancy = vacancyRepository.findById(application.getVacancyId())
                                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

                        vacancy.setPendingCount(vacancy.getPendingCount() - 1);
                        vacancyRepository.save(vacancy);
                    }

                    application.setStatus(ApplicationStatus.ACCEPTED);
                    Application updated = applicationRepository.save(application);
                    Vacancy vacancy = vacancyRepository.findById(updated.getVacancyId())
                            .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

                    logger.info("Applicant {} accepted for vacancy {}",
                            updated.getApplicant().getName(),
                            vacancy.getTitle());

                    return mapToResponse(updated);
                }
        );
    }

    public ApplicationResponse rejectApplication(Long applicationId, Long userId) {
        return transactionService.execute("rejectApplication", 30, status -> {

                    Application application = applicationRepository.findById(applicationId)
                            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

                    Employer reviewer = employerRepository.findByUserId(userId)
                            .orElseThrow(() -> new AccessDeniedException("User with id " + userId + " is not registered as employer"));

                    if (application.getStatus() == ApplicationStatus.PENDING
                            || application.getStatus() == ApplicationStatus.PENDING_WITH_WARNING) {

                        Vacancy vacancy = vacancyRepository.findById(application.getVacancyId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Vacancy not found for application: " + applicationId));

                        vacancy.setPendingCount(vacancy.getPendingCount() - 1);
                        vacancyRepository.save(vacancy);
                    }
                    application.setStatus(ApplicationStatus.REJECTED);
                    Application updated = applicationRepository.save(application);

                    Vacancy vacancy = vacancyRepository.findById(updated.getVacancyId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Vacancy not found after update for application: " + applicationId));

                    logger.info("Application {} rejected by employer {}. Applicant: {}, Vacancy: {}",
                            applicationId,
                            reviewer.getId(),
                            updated.getApplicant().getName(),
                            vacancy.getTitle());

                    return mapToResponse(updated);
                }
        );
    }

    public ApplicationResponse addCoverLetter(Long applicationId, String coverLetter) {
        return transactionService.execute("addCoverLetter", 15, status -> {
                    Application application = applicationRepository.findById(applicationId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Application not found with id: " + applicationId));

                    if (coverLetter == null || coverLetter.trim().isEmpty()) {
                        throw new IllegalArgumentException("Cover letter cannot be empty");
                    }
                    application.setCoverLetter(coverLetter);
                    application.setStatus(ApplicationStatus.PENDING);
                    Application updated = applicationRepository.save(application);
                    logger.info("Cover letter added to application {}. New status: PENDING",
                            applicationId);

                    return mapToResponse(updated);
                }
        );
    }

    private ApplicationResponse mapToResponse(Application application) {
        Vacancy vacancy = vacancyRepository.findById(application.getVacancyId()).orElseThrow();
        return new ApplicationResponse(
                application.getId(),
                application.getStatus().name(),
                vacancy.getTitle(),
                application.getApplicant().getName(),
                application.getStatus().equals(ApplicationStatus.PENDING_WITH_WARNING) ?
                        "The resume partially meets the requirements of the vacancy" : null,
                application.getCreatedAt() != null ?
                        application.getCreatedAt() : LocalDateTime.now(),
                application.getCoverLetter(),
                vacancy.getPendingLimit() - vacancy.getPendingCount()
        );
    }

    private boolean isResumeMatching(Resume resume, Vacancy vacancy) {
        if (vacancy.getKeywords() == null || vacancy.getKeywords().isBlank()) {
            return true;
        }

        Set<String> keywords = Arrays.stream(vacancy.getKeywords().split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (keywords.isEmpty()) return true;

        String resumeContent = resume.getContent().toLowerCase();
        long requiredMatches = (long) Math.ceil(keywords.size() * 0.7);

        long matches = keywords.stream()
                .filter(keyword -> {
                    String pattern = "\\b" + Pattern.quote(keyword.toLowerCase()) + "\\b";
                    return Pattern.compile(pattern).matcher(resumeContent).find();
                })
                .count();

        return matches >= requiredMatches;
    }
}