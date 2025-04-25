package org.fergoeqs.blps1.models;


import jakarta.persistence.*;
import lombok.Data;

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
}