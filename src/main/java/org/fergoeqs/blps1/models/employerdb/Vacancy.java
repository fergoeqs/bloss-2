package org.fergoeqs.blps1.models.employerdb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.fergoeqs.blps1.models.applicantdb.Application;
import org.fergoeqs.blps1.models.enums.VacancyStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vacancies", schema = "employer_schema")
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

    @Column(nullable = false)
    private VacancyStatus status;

//    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private List<Application> applications = new ArrayList<>();

    @Column(name = "pending_limit", nullable = false)
    private Integer pendingLimit = 100;

    @Column(name = "pending_count", nullable = false)
    private Integer pendingCount = 0;

    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;
}