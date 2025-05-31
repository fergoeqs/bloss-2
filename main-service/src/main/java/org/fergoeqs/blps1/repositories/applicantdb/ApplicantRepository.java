package org.fergoeqs.blps1.repositories.applicantdb;

import org.fergoeqs.blps1.models.applicantdb.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
}