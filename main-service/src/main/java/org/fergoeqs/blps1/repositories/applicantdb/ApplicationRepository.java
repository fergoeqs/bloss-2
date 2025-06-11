package org.fergoeqs.blps1.repositories.applicantdb;

import org.fergoeqs.blps1.models.applicantdb.Application;
import org.fergoeqs.blps1.models.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Page<Application> findByVacancyId(Long vacancyId, Pageable pageable);
    List<Application> findByStatusAndCreatedAtBefore(ApplicationStatus status, LocalDateTime createdAt);

    Optional<Application> findByIssueKey(String issueKey);
}