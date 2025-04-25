package org.fergoeqs.blps1.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.postgresql.xa.PGXADataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DataSourceConfig {

    private PGXADataSource createXaDataSource() {
        PGXADataSource ds = new PGXADataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/blpsdb");
        ds.setUser("blps");
        ds.setPassword("blps");
        return ds;
    }

    @Bean(name = "applicantDataSource")
    @Primary
    public DataSource applicantDataSource() {
        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(createXaDataSource());
        xaDataSource.setUniqueResourceName("applicantDS");
        xaDataSource.setPoolSize(5);

        Properties props = new Properties();
        props.setProperty("currentSchema", "applicant_schema");
        xaDataSource.setXaProperties(props);

        return xaDataSource;
    }

    @Bean(name = "employerDataSource")
    public DataSource employerDataSource() {
        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(createXaDataSource());
        xaDataSource.setUniqueResourceName("employerDS");
        xaDataSource.setPoolSize(5);

        Properties props = new Properties();
        props.setProperty("currentSchema", "employer_schema");
        xaDataSource.setXaProperties(props);

        return xaDataSource;
    }

    @Bean(name = "securityDataSource")
    public DataSource securityDataSource() {
        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(createXaDataSource());
        xaDataSource.setUniqueResourceName("securityDS");
        xaDataSource.setPoolSize(5);

        Properties props = new Properties();
        props.setProperty("currentSchema", "security_schema");
        xaDataSource.setXaProperties(props);

        return xaDataSource;
    }
}