package com.nomadas.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@TestPropertySource(properties = {
        "app.demo-data.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:flyway_baseline_it;MODE=MySQL;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "app.demo-admin.username=admin",
        "app.demo-admin.email=admin@nomadas.local",
        "app.demo-admin.password=admin12345"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FlywayBaselineIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoadsAndCreatesBaselineSchema() {
        long migrations = jdbcTemplate.queryForObject(
                "select count(*) from \"flyway_schema_history\"",
                Long.class
        );
        assertThat(migrations).isGreaterThanOrEqualTo(1);

        for (String table : new String[] {
                "users", "hotels", "drivers", "buses", "trips", "bookings",
                "companions", "internal_credentials"
        }) {
            long exists = jdbcTemplate.queryForObject(
                    "select count(*) from information_schema.tables " +
                            "where lower(table_schema) = 'public' and lower(table_name) = lower(?)",
                    Long.class,
                    table
            );
            assertThat(exists)
                    .as("expected table '%s' to exist after baseline migration", table)
                    .isEqualTo(1L);
        }
    }
}
