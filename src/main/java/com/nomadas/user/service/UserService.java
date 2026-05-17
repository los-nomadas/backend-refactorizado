package com.nomadas.user.service;

import com.nomadas.user.dto.UserCreateRequest;
import com.nomadas.user.dto.UserResponse;
import com.nomadas.user.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {
    List<UserResponse> getAll();

    UserResponse getById(Long id);

    UserResponse getByUsername(String username);

    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    void delete(Long id);
}

