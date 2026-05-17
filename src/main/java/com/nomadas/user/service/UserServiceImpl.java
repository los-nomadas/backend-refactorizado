package com.nomadas.user.service;

import com.nomadas.exception.ConflictException;
import com.nomadas.exception.ResourceNotFoundException;
import com.nomadas.user.dto.UserCreateRequest;
import com.nomadas.user.dto.UserResponse;
import com.nomadas.user.dto.UserUpdateRequest;
import com.nomadas.user.model.User;
import com.nomadas.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    @Override
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }
        if (userRepository.existsByDni(request.dni())) {
            throw new ConflictException("DNI already exists");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dni(request.dni())
                .email(request.email())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .build();

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.findByEmail(request.email())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("Email already exists");
                });

        userRepository.findByDni(request.dni())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("DNI already exists");
                });

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDni(request.dni());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setBirthDate(request.birthDate());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDni(),
                user.getEmail(),
                user.getPhone(),
                user.getBirthDate()
        );
    }
}

