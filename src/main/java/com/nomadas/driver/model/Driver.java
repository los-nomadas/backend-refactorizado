package com.nomadas.driver.model;

import com.nomadas.bus.model.Bus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

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

    @NotBlank
    @Column(unique = true)
    private String licenseNumber;

    @Pattern(regexp = "^[0-9]{9,}$")
    private String phone;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    private Boolean available;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.REMOVE)
    private List<Bus> buses;
}
