package org.fergoeqs.blps1.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vacancies")
@Data
public class Vacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "resume_required", nullable = false)
    private boolean resumeRequired;

    @Column(name = "cover_letter_required", nullable = false)
    private boolean coverLetterRequired;

    @Column(columnDefinition = "TEXT")
    private String keywords;
}