package org.fergoeqs.blps1.models.employerdb;


import jakarta.persistence.*;
import lombok.Data;
import org.fergoeqs.blps1.models.enums.Role;

@Entity
@Table(name = "employers", schema = "employer_schema")
@Data
public class Employer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "contact_info", nullable = false)
    private String contactInfo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}