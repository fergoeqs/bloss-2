package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.exceptions.ResourceNotFoundException;
import org.fergoeqs.blps1.models.Applicant;
import org.fergoeqs.blps1.models.Application;
import org.fergoeqs.blps1.models.Resume;
import org.fergoeqs.blps1.models.Vacancy;
import org.fergoeqs.blps1.repositories.ApplicantRepository;
import org.fergoeqs.blps1.repositories.ApplicationRepository;
import org.fergoeqs.blps1.repositories.ResumeRepository;
import org.fergoeqs.blps1.repositories.VacancyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    private final ApplicationRepository applicationRepository;

    private final VacancyRepository vacancyRepository;

    private final ApplicantRepository applicantRepository;

    private final ResumeRepository resumeRepository;

    public ApplicationService(ApplicationRepository applicationRepository, VacancyRepository vacancyRepository, ApplicantRepository applicantRepository, ResumeRepository resumeRepository) {
        this.applicationRepository = applicationRepository;
        this.vacancyRepository = vacancyRepository;
        this.applicantRepository = applicantRepository;
        this.resumeRepository = resumeRepository;
    }

    public Application createApplication(Long vacancyId, Long applicantId, Long resumeId, String coverLetter) {
        Application application = new Application();

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found with id: " + vacancyId));
        application.setVacancy(vacancy);

        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found with id: " + applicantId));
        application.setApplicant(applicant);

        if (vacancy.isResumeRequired()) {
            if (resumeId == null) {
                application.setStatus("Resume Required");
                return applicationRepository.save(application);
            }
            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));
            if (!resume.getApplicant().getId().equals(applicantId)) {
                throw new RuntimeException("Resume does not belong to this applicant");
            }
            application.setResume(resume);

            if (!isResumeMatchingVacancy(resume, vacancy)) {
                application.setStatus("Resume Not Matching");
                return applicationRepository.save(application);
            }
        }

        if (vacancy.isCoverLetterRequired()) {
            if (coverLetter == null || coverLetter.trim().isEmpty()) {
                application.setStatus("Cover Letter Required");
                return applicationRepository.save(application);
            }
            application.setCoverLetter(coverLetter);
        } else {
            application.setCoverLetter(coverLetter);
        }

        application.setStatus("Sent to Employer");
        return applicationRepository.save(application);
    }

    private boolean isResumeMatchingVacancy(Resume resume, Vacancy vacancy) {
        if (vacancy.getKeywords() == null || vacancy.getKeywords().trim().isEmpty()) {
            return true;
        }

        Set<String> vacancyKeywords = Arrays.stream(vacancy.getKeywords().toLowerCase().split("\\s*,\\s*"))
                .collect(Collectors.toSet());
        Set<String> resumeWords = Arrays.stream(resume.getContent().toLowerCase().split("\\s+"))
                .collect(Collectors.toSet());

        return vacancyKeywords.stream().anyMatch(resumeWords::contains);
    }

    public List<Application> getApplicationsByVacancyId(Long vacancyId) {
        return applicationRepository.findByVacancyId(vacancyId);
    }

    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    public Application acceptApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        application.setStatus("Accepted");
        application = applicationRepository.save(application);

        logger.info("Notification: Applicant {} has been invited for an interview for vacancy '{}'. Contact: {}",
                application.getApplicant().getName(),
                application.getVacancy().getTitle(),
                application.getApplicant().getContactInfo());

        return application;
    }

    public Application rejectApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        application.setStatus("Rejected");
        application = applicationRepository.save(application);

        logger.info("Notification: Applicant {} has been rejected for vacancy '{}'. Contact: {}",
                application.getApplicant().getName(),
                application.getVacancy().getTitle(),
                application.getApplicant().getContactInfo());

        return application;
    }
}