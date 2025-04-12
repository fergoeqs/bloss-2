package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.dto.ApplicationRequest;
import org.fergoeqs.blps1.dto.ApplicationResponse;
import org.fergoeqs.blps1.exceptions.ResourceNotFoundException;
import org.fergoeqs.blps1.models.*;
import org.fergoeqs.blps1.models.enums.ApplicationStatus;
import org.fergoeqs.blps1.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final ApplicantRepository applicantRepository;
    private final ResumeRepository resumeRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              VacancyRepository vacancyRepository,
                              ApplicantRepository applicantRepository,
                              ResumeRepository resumeRepository) {
        this.applicationRepository = applicationRepository;
        this.vacancyRepository = vacancyRepository;
        this.applicantRepository = applicantRepository;
        this.resumeRepository = resumeRepository;
    }

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) throws Exception {
        Application application = new Application();

        Vacancy vacancy = vacancyRepository.findById(request.vacancyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
        Applicant applicant = applicantRepository.findById(request.applicantId())
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found"));

        application.setVacancy(vacancy);
        application.setApplicant(applicant);

        String warning = null;
        if (vacancy.isResumeRequired()) {
            Resume resume;
            if (applicant.getResumes() == null || applicant.getResumes().isEmpty()) {
                application.setStatus(ApplicationStatus.RESUME_REQUIRED);
                throw new Exception("Cover letter required");
            } else {
                if (applicant.getResumes().size() > 1) {
                    if (request.resumeId() == null) {
                        resume = applicant.getResumes().get(0);
                    } else {
                        resume = resumeRepository.findById(request.resumeId())
                                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

                        if (!resume.getApplicant().getId().equals(applicant.getId())) {
                            throw new Exception("Resume doesn't belong to applicant");
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
            throw new Exception("Cover letter required");
        } else {
            application.setCoverLetter(request.coverLetter());
        }

        if (application.getStatus() == null) {
            application.setStatus(warning != null ?
                    ApplicationStatus.PENDING_WITH_WARNING :
                    ApplicationStatus.PENDING);
        }

        Application saved = applicationRepository.save(application);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteApplication(Long applicationId){
        applicationRepository.deleteById(applicationId);
    }

    public Page<ApplicationResponse> getApplicationsByVacancyId(Long vacancyId, Pageable pageable) {
        return applicationRepository.findByVacancyId(vacancyId, pageable)
                .map(this::mapToResponse);
    }

    public Optional<ApplicationResponse> getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Transactional
    public ApplicationResponse acceptApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        application.setStatus(ApplicationStatus.ACCEPTED);
        Application updated = applicationRepository.save(application);

        logger.info("Applicant {} accepted for vacancy {}",
                updated.getApplicant().getName(),
                updated.getVacancy().getTitle());

        return mapToResponse(updated);
    }

    @Transactional
    public ApplicationResponse rejectApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        application.setStatus(ApplicationStatus.REJECTED);
        Application updated = applicationRepository.save(application);

        logger.info("Applicant {} rejected for vacancy {}",
                updated.getApplicant().getName(),
                updated.getVacancy().getTitle());

        return mapToResponse(updated);
    }

    private ApplicationResponse mapToResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getStatus().name(),
                application.getVacancy().getTitle(),
                application.getApplicant().getName(),
                application.getStatus().equals(ApplicationStatus.PENDING_WITH_WARNING) ?
                        "The resume partially meets the requirements of the vacancy" : null,
                application.getCreatedAt() != null ?
                        application.getCreatedAt() : LocalDateTime.now(),
                application.getCoverLetter()
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

    @Transactional
    public ApplicationResponse addCoverLetter(Long applicationId, String coverLetter) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        application.setCoverLetter(coverLetter);
        application.setStatus(ApplicationStatus.PENDING);
        Application updated = applicationRepository.save(application);
        return mapToResponse(updated);
    }
}