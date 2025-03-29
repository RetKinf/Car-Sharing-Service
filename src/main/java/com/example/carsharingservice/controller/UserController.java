package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.role.RoleRequestDto;
import com.example.carsharingservice.dto.user.UserRequestDto;
import com.example.carsharingservice.dto.user.UserResponseDto;
import com.example.carsharingservice.dto.user.UserResponseWithoutRolesDto;
import com.example.carsharingservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(("/users"))
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('MANAGER')")
    @Operation(summary = "Update user role")
    @PutMapping("/{id}/role")
    public UserResponseDto updateRole(
            @RequestBody @Valid RoleRequestDto requestDto,
            @PathVariable Long id
    ) {
        return userService.updateRole(requestDto, id);
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Get current user details")
    @GetMapping("/me")
    public UserResponseWithoutRolesDto getCurrentUser(Authentication authentication) {
        return userService.getCurrentUserDto(authentication);
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Update current user details")
    @PatchMapping("/me")
    public UserResponseWithoutRolesDto updateCurrentUser(
            @RequestBody UserRequestDto userRequestDto,
            Authentication authentication
    ) {
        return userService.updateCurrentUser(userRequestDto, authentication);
    }
}
