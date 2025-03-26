package com.example.carsharingservice.dto.role;

import com.example.carsharingservice.model.RoleName;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record RoleRequestDto(
        @NotNull
        Set<RoleName> roleName
) {
}
