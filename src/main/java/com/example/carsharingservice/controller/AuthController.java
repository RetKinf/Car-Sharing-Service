package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.user.UserLoginRequestDto;
import com.example.carsharingservice.dto.user.UserLoginResponseDto;
import com.example.carsharingservice.dto.user.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.user.UserResponseWithoutRolesDto;
import com.example.carsharingservice.security.AuthService;
import com.example.carsharingservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/registration")
    @Operation(summary = "Register a new user")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseWithoutRolesDto register(
            @RequestBody @Valid UserRegistrationRequestDto requestDto
    ) {
        return userService.register(requestDto);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return login details")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authService.authenticate(requestDto);
    }
}
