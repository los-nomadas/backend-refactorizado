package com.nomadas.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 1, max = 100)
    private String firstName;

    @NotBlank
    @Size(min = 1, max = 100)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^[0-9]{8}[A-Z]$")
    @Column(unique = true)
    private String dni;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^[0-9]{9,}$")
    private String phone;

    @NotNull
    @PastOrPresent
    private LocalDate birthDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
