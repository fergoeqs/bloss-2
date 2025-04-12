package org.fergoeqs.blps1.repositories;

import org.fergoeqs.blps1.models.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Page<Resume> findByApplicantId(Long applicantId, Pageable pageable);
}