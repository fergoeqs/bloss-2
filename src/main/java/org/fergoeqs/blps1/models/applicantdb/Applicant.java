package org.fergoeqs.blps1.models.applicantdb;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "applicants", schema = "applicant_schema")
@Data
public class Applicant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_info", nullable = false)
    private String contactInfo;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Resume> resumes;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mail", unique = true)
    @Email(message = "Email should be in format like email@example.com")
    private String mail;
}