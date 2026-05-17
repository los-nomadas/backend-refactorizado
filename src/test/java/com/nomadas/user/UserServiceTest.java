package com.nomadas.user;

import com.nomadas.exception.ConflictException;
import com.nomadas.exception.ResourceNotFoundException;
import com.nomadas.user.dto.UserCreateRequest;
import com.nomadas.user.dto.UserResponse;
import com.nomadas.user.dto.UserUpdateRequest;
import com.nomadas.user.repository.UserRepository;
import com.nomadas.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void create_shouldCreateUser() {
        UserCreateRequest request = new UserCreateRequest(
                "David",
                "Navarro",
                "12345678A",
                "david@example.com",
                "600123123",
                LocalDate.parse("1990-04-15")
        );

        UserResponse created = userService.create(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.firstName()).isEqualTo("David");
        assertThat(created.dni()).isEqualTo("12345678A");
        assertThat(created.birthDate()).isEqualTo(LocalDate.parse("1990-04-15"));
    }

    @Test
    void create_shouldThrowConflict_whenEmailExists() {
        userService.create(new UserCreateRequest(
                "David",
                "Navarro",
                "12345678A",
                "david@example.com",
                "600123123",
                LocalDate.parse("1990-04-15")
        ));

        assertThatThrownBy(() -> userService.create(new UserCreateRequest(
                "Ana",
                "Garcia",
                "87654321B",
                "david@example.com",
                "600000000",
                LocalDate.parse("1995-01-01")
        ))).isInstanceOf(ConflictException.class).hasMessage("Email already exists");
    }

    @Test
    void update_shouldThrowNotFound_whenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.update(999L, new UserUpdateRequest(
                "David",
                "Navarro",
                "12345678A",
                "david@example.com",
                "600123123",
                LocalDate.parse("1990-04-15")
        ))).isInstanceOf(ResourceNotFoundException.class).hasMessage("User not found");
    }
}

