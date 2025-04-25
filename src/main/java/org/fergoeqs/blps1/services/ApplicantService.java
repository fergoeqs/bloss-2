package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.dto.ApplicantRequest;
import org.fergoeqs.blps1.dto.ApplicantResponse;
import org.fergoeqs.blps1.dto.ResumeResponse;
import org.fergoeqs.blps1.exceptions.ResourceNotFoundException;
import org.fergoeqs.blps1.models.Applicant;
import org.fergoeqs.blps1.models.Resume;
import org.fergoeqs.blps1.repositories.ApplicantRepository;
import org.fergoeqs.blps1.repositories.ResumeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Service
public class ApplicantService {

    private final ApplicantRepository applicantRepository;

    private final ResumeRepository resumeRepository;

    public ApplicantService(ApplicantRepository applicantRepository, ResumeRepository resumeRepository) {
        this.applicantRepository = applicantRepository;
        this.resumeRepository = resumeRepository;
    }

    @Transactional
    public ApplicantResponse createApplicant(ApplicantRequest request) {
        Applicant applicant = new Applicant();
        applicant.setName(request.name());
        applicant.setContactInfo(request.contactInfo());

        Applicant saved = applicantRepository.save(applicant);
        return new ApplicantResponse(
                saved.getId(),
                saved.getName(),
                saved.getContactInfo()
        );
    }

    @Transactional
    public void deleteApplicant(Long id) {
        applicantRepository.deleteById(id);
    }

    public Optional<Applicant> getApplicantById(Long id) {
        return applicantRepository.findById(id);
    }

    @Transactional
    public Resume addResume(Long applicantId, Resume resume) {
        Optional<Applicant> applicantOpt = applicantRepository.findById(applicantId);
        if (applicantOpt.isEmpty()) {
            throw new RuntimeException("Applicant not found");
        }
        Applicant applicant = applicantOpt.get();
        resume.setApplicant(applicant);
        return resumeRepository.save(resume);
    }


    public Page<ResumeResponse> getResumesByApplicantId(Long applicantId, Pageable pageable) {
        if (!applicantRepository.existsById(applicantId)) {
            throw new ResourceNotFoundException("Applicant not found");
        }
        return resumeRepository.findByApplicantId(applicantId, pageable)
                .map(resume -> new ResumeResponse(resume.getId(), resume.getContent()));
    }
}