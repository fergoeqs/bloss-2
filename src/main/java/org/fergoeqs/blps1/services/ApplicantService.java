package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.models.Applicant;
import org.fergoeqs.blps1.models.Resume;
import org.fergoeqs.blps1.repositories.ApplicantRepository;
import org.fergoeqs.blps1.repositories.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicantService {

    private final ApplicantRepository applicantRepository;

    private final ResumeRepository resumeRepository;

    public ApplicantService(ApplicantRepository applicantRepository, ResumeRepository resumeRepository) {
        this.applicantRepository = applicantRepository;
        this.resumeRepository = resumeRepository;
    }

    public Applicant createApplicant(Applicant applicant) {
        return applicantRepository.save(applicant);
    }

    public Optional<Applicant> getApplicantById(Long id) {
        return applicantRepository.findById(id);
    }

    public Resume addResume(Long applicantId, Resume resume) {
        Optional<Applicant> applicantOpt = applicantRepository.findById(applicantId);
        if (applicantOpt.isEmpty()) {
            throw new RuntimeException("Applicant not found");
        }
        Applicant applicant = applicantOpt.get();
        resume.setApplicant(applicant);
        return resumeRepository.save(resume);
    }

    public List<Resume> getResumesByApplicantId(Long applicantId) {
        Optional<Applicant> applicantOpt = applicantRepository.findById(applicantId);
        if (applicantOpt.isEmpty()) {
            throw new RuntimeException("Applicant not found");
        }
        return applicantOpt.get().getResumes();
    }
}