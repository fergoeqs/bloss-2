package org.fergoeqs.blps1.repositories;

import org.fergoeqs.blps1.models.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByVacancyId(Long vacancyId);
}