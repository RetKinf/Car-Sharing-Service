package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.role.RoleRequestDto;
import com.example.carsharingservice.dto.user.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.user.UserRequestDto;
import com.example.carsharingservice.dto.user.UserResponseDto;
import com.example.carsharingservice.dto.user.UserResponseWithoutRolesDto;
import com.example.carsharingservice.exception.DataNotFoundException;
import com.example.carsharingservice.exception.RegistrationException;
import com.example.carsharingservice.mapper.UserMapper;
import com.example.carsharingservice.model.Role;
import com.example.carsharingservice.model.RoleName;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.RoleRepository;
import com.example.carsharingservice.repository.UserRepository;
import com.example.carsharingservice.service.UserService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseWithoutRolesDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (existsByEmail(requestDto.email())) {
            throw new RegistrationException(String.format(
                    "User with email %s already exists",
                    requestDto.email())
            );
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName(RoleName.CUSTOMER).orElseThrow(
                () -> new DataNotFoundException(
                        String.format("Role %s not found", RoleName.CUSTOMER)
                )
        );
        user.setRoles(Set.of(role));
        User saveDUser = userRepository.save(user);
        return userMapper.toDtoWithoutRoles(saveDUser);
    }

    @Override
    public UserResponseDto updateRole(RoleRequestDto requestDto, Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %s not found", id))
        );
        Set<Role> roles = new HashSet<>();
        for (RoleName roleName : requestDto.roleName()) {
            roles.add(roleRepository.findByName(roleName).orElseThrow(
                    () -> new DataNotFoundException(String.format("Role %s not found", roleName))
                    )
            );
        }
        user.setRoles(roles);
        User save = userRepository.save(user);
        return userMapper.toDto(save);
    }

    @Override
    public UserResponseWithoutRolesDto getCurrentUserDto(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return userMapper.toDtoWithoutRoles(user);
    }

    @Override
    public UserResponseWithoutRolesDto updateCurrentUser(
            UserRequestDto userRequestDto,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new DataNotFoundException(String.format(
                        "User with email %s not found",
                        authentication.getName())
                )
        );
        if (userRequestDto.getEmail() != null) {
            user.setEmail(userRequestDto.getEmail());
        }
        if (userRequestDto.getFirstName() != null
                && !userRequestDto.getFirstName().isEmpty()) {
            user.setFirstName(userRequestDto.getFirstName());
        }
        if (userRequestDto.getLastName() != null
                && !userRequestDto.getLastName().isEmpty()) {
            user.setLastName(userRequestDto.getLastName());
        }
        return userMapper.toDtoWithoutRoles(userRepository.save(user));
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new DataNotFoundException(
                        String.format("User with email %s not found", authentication.getName())
                )
        );
    }

    private boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
