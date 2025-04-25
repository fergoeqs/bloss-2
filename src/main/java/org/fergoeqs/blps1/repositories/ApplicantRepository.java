package org.fergoeqs.blps1.repositories;

import org.fergoeqs.blps1.models.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
}