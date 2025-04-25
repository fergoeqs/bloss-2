package org.fergoeqs.blps1.repositories.applicantdb;

import org.fergoeqs.blps1.models.applicantdb.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Page<Application> findByVacancyId(Long vacancyId, Pageable pageable);
}