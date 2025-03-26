package com.example.carsharingservice.dto.user;

public record UserResponseWithoutRolesDto(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}
