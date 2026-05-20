package com.nomadas.config;

import com.nomadas.auth.model.InternalRole;
import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.entity.InternalCredential;
import com.nomadas.user.model.User;
import com.nomadas.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@Profile({"dev", "e2e"})
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataConfig {

    @Value("${app.demo-admin.username}")
    private String demoAdminUsername;

    @Value("${app.demo-admin.email}")
    private String demoAdminEmail;

    @Value("${app.demo-admin.password}")
    private String demoAdminPassword;

    @Bean
    public CommandLineRunner demoDataInitializer(
            InternalCredentialRepository internalCredentialRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> seedDemoData(internalCredentialRepository, userRepository, passwordEncoder);
    }

    private void seedDemoData(
            InternalCredentialRepository internalCredentialRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        if (internalCredentialRepository.count() > 0) {
            return;
        }

        User demoCustomer = userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("Nomadas")
                .dni("00000000A")
                .email(demoAdminEmail)
                .phone("600000000")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build());

        internalCredentialRepository.save(InternalCredential.builder()
                .username(demoAdminUsername)
                .email(demoAdminEmail)
                .passwordHash(passwordEncoder.encode(demoAdminPassword))
                .role(InternalRole.ADMIN)
                .active(true)
                .user(demoCustomer)
                .build());
    }
}
