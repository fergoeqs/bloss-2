package org.fergoeqs.blps1.controllers;

import jakarta.validation.Valid;
import org.fergoeqs.blps1.dto.ApplicantRequest;
import org.fergoeqs.blps1.dto.ApplicantResponse;
import org.fergoeqs.blps1.dto.ResumeResponse;
import org.fergoeqs.blps1.models.Applicant;
import org.fergoeqs.blps1.models.Resume;
import org.fergoeqs.blps1.services.ApplicantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applicants")
public class ApplicantController {

    private final ApplicantService applicantService;

    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    @PostMapping
    public ResponseEntity<ApplicantResponse> createApplicant(
            @Valid @RequestBody ApplicantRequest request) {
        ApplicantResponse response = applicantService.createApplicant(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Applicant> getApplicantById(@PathVariable Long id) {
        return applicantService.getApplicantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/resumes")
    public ResponseEntity<Resume> addResume(@PathVariable Long id, @Valid @RequestBody Resume resume) {
        Resume createdResume = applicantService.addResume(id, resume);
        return ResponseEntity.ok(createdResume);
    }

    @GetMapping("/{id}/resumes")
    public ResponseEntity<List<ResumeResponse>> getResumesByApplicantId(@PathVariable Long id) {
        List<ResumeResponse> resumes = applicantService.getResumesByApplicantId(id);
        return ResponseEntity.ok(resumes);
    }
}