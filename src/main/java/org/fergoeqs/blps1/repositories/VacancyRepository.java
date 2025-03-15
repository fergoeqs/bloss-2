package org.fergoeqs.blps1.repositories;

import org.fergoeqs.blps1.models.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
}