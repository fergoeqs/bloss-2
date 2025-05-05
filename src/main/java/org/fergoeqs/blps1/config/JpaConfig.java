package org.fergoeqs.blps1.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JpaConfig {
    @Primary
    @Bean(name = "applicantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean applicantEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("applicantDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .jta(true)
                .packages("org.fergoeqs.blps1.models.applicantdb")
                .persistenceUnit("applicantPU")
                .properties(jpaProperties("applicant_schema"))
                .build();
    }

    @Bean(name = "employerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean employerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("employerDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .jta(true)
                .packages("org.fergoeqs.blps1.models.employerdb")
                .persistenceUnit("employerPU")
                .properties(jpaProperties("employer_schema"))
                .build();
    }

    @Bean(name = "securityEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean securityEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("securityDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .jta(true)
                .packages("org.fergoeqs.blps1.models.securitydb")
                .persistenceUnit("securityPU")
                .properties(jpaProperties("security_schema"))
                .build();
    }

    private Map<String, Object> jpaProperties(String schema) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        return props;
    }

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.fergoeqs.blps1.repositories.applicantdb",
            entityManagerFactoryRef = "applicantEntityManagerFactory")
    public static class ApplicantRepositoryConfig {
    }

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.fergoeqs.blps1.repositories.employerdb",
            entityManagerFactoryRef = "employerEntityManagerFactory")
    public static class EmployerRepositoryConfig {
    }

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.fergoeqs.blps1.repositories.securitydb",
            entityManagerFactoryRef = "securityEntityManagerFactory")
    public static class SecurityRepositoryConfig {

    }


}

