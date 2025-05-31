package org.fergoeqs.blps1.repositories.applicantdb;

import org.fergoeqs.blps1.models.applicantdb.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Page<Resume> findByApplicantId(Long applicantId, Pageable pageable);
}