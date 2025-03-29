package org.fergoeqs.blps1.repositories;

import org.fergoeqs.blps1.models.Vacancy;
import org.fergoeqs.blps1.models.enums.VacancyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    List<Vacancy> findAllByStatus(VacancyStatus status);
}