package org.fergoeqs.blps1.models.applicantdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.fergoeqs.blps1.models.employerdb.Vacancy;
import org.fergoeqs.blps1.models.enums.ApplicationStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications", schema = "applicant_schema")
@Data
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vacancy_id")
    private Long vacancyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Resume resume;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "issue_key")
    private String issueKey;

    @Column(name = "last_status_change")
    private LocalDateTime lastStatusChange;


}