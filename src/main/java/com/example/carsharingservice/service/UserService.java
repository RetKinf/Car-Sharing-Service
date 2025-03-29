package com.example.carsharingservice.service;

import com.example.carsharingservice.dto.role.RoleRequestDto;
import com.example.carsharingservice.dto.user.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.user.UserRequestDto;
import com.example.carsharingservice.dto.user.UserResponseDto;
import com.example.carsharingservice.dto.user.UserResponseWithoutRolesDto;
import com.example.carsharingservice.exception.RegistrationException;
import com.example.carsharingservice.model.User;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserResponseWithoutRolesDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserResponseDto updateRole(RoleRequestDto requestDto, Long id);

    UserResponseWithoutRolesDto getCurrentUserDto(Authentication authentication);

    UserResponseWithoutRolesDto updateCurrentUser(
            UserRequestDto userRequestDto,
            Authentication authentication
    );

    boolean existsById(Long id);

    User getCurrentUser(Authentication authentication);
}
