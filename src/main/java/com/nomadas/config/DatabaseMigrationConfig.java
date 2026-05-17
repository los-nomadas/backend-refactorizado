package com.nomadas.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
public class DatabaseMigrationConfig {

    @Bean(initMethod = "migrate")
    @ConditionalOnMissingBean(Flyway.class)
    public Flyway flyway(
            DataSource dataSource,
            @Value("${spring.flyway.locations:classpath:db/migration/{vendor}}") String[] locations,
            @Value("${spring.flyway.baseline-on-migrate:false}") boolean baselineOnMigrate,
            @Value("${spring.flyway.clean-disabled:true}") boolean cleanDisabled
    ) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(baselineOnMigrate)
                .cleanDisabled(cleanDisabled)
                .load();
    }

    @Bean
    public static EntityManagerFactoryDependsOnPostProcessor entityManagerFactoryDependsOnFlyway() {
        return new EntityManagerFactoryDependsOnPostProcessor(Flyway.class);
    }
}
