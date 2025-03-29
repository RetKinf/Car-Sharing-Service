package com.example.carsharingservice.mapper;

import com.example.carsharingservice.config.MapperConfig;
import com.example.carsharingservice.dto.user.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.user.UserResponseDto;
import com.example.carsharingservice.dto.user.UserResponseWithoutRolesDto;
import com.example.carsharingservice.model.Role;
import com.example.carsharingservice.model.User;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegistrationRequestDto requestDto);

    UserResponseWithoutRolesDto toDtoWithoutRoles(User user);

    @Mapping(target = "roleNames", source = "roles")
    UserResponseDto toDto(User user);

    default List<String> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getName().toString())
                .toList();
    }
}
