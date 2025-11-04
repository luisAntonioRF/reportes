package com.app.reporte.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class DataSourceConfig {


    // ----------- DATAMART ------------
    @Bean(name = "datamartDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.datamart")
    public DataSource datamartDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "datamartNamedJdbc")
    public NamedParameterJdbcTemplate datamartNamedJdbc(@Qualifier("datamartDataSource") DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }

    // ----------- MO ------------------
    @Bean(name = "moDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mo")
    public DataSource moDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "moNamedJdbc")
    public NamedParameterJdbcTemplate moNamedJdbc(@Qualifier("moDataSource") DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }

    // ----------- WH ------------------
    @Bean(name = "whDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.wh")
    public DataSource whDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "whNamedJdbc")
    public NamedParameterJdbcTemplate whNamedJdbc(@Qualifier("whDataSource") DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }
    
    @Bean(name = "whJdbcTemplate")
    public JdbcTemplate whJdbcTemplate(@Qualifier("whDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
	
}
