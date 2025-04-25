package org.fergoeqs.blps1.repositories;

import org.fergoeqs.blps1.models.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
}