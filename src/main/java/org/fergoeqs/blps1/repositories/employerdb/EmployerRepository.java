package org.fergoeqs.blps1.repositories.employerdb;

import org.fergoeqs.blps1.models.employerdb.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
}