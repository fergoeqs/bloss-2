package org.fergoeqs.blps1.repositories;

import org.fergoeqs.blps1.models.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}