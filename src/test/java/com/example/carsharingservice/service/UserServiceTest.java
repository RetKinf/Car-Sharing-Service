package com.example.carsharingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.carsharingservice.dto.role.RoleRequestDto;
import com.example.carsharingservice.dto.user.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.user.UserRequestDto;
import com.example.carsharingservice.dto.user.UserResponseDto;
import com.example.carsharingservice.dto.user.UserResponseWithoutRolesDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.RegistrationException;
import com.example.carsharingservice.mapper.UserMapper;
import com.example.carsharingservice.model.Role;
import com.example.carsharingservice.model.RoleName;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.RoleRepository;
import com.example.carsharingservice.repository.UserRepository;
import com.example.carsharingservice.service.impl.UserServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_PASSWORD = "password";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Doe";
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Register new user")
    void registerUser_WithValidUserRegistrationRequestDto_ReturnUserResponseDto()
            throws RegistrationException {
        Long userId = 1L;
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PASSWORD,
                USER_PASSWORD
        );
        User user = new User()
                .setId(userId)
                .setEmail(requestDto.email())
                .setPassword(requestDto.password())
                .setFirstName(requestDto.firstName())
                .setLastName(requestDto.lastName());
        Role role = new Role();
        role.setName(RoleName.CUSTOMER);
        UserResponseWithoutRolesDto expected = new UserResponseWithoutRolesDto(
                userId,
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME
        );
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn(USER_PASSWORD);
        when(roleRepository.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDtoWithoutRoles(user)).thenReturn(expected);
        UserResponseWithoutRolesDto actual = userService.register(requestDto);
        assertEquals(expected, actual);
        verify(userRepository).existsByEmail(requestDto.email());
        verify(userMapper).toModel(requestDto);
        verify(passwordEncoder).encode(user.getPassword());
        verify(roleRepository).findByName(RoleName.CUSTOMER);
        verify(userRepository).save(user);
        verify(userMapper).toDtoWithoutRoles(user);
        verifyNoMoreInteractions(
                userRepository,
                roleRepository,
                userMapper,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("Register user with existing email should throw exception")
    void registerUser_EmailAlreadyExists_ThrowsException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PASSWORD,
                USER_PASSWORD
        );
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(true);
        Exception exception = assertThrows(
                RegistrationException.class,
                () -> userService.register(requestDto)
        );
        String expected = String.format(
                "User with email %s already exists",
                 requestDto.email());
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    @DisplayName("Update user roles")
    void updateRole_WithValidRoleRequestDto_updateRoles() {
        Long userId = 1L;
        User user = new User()
                .setId(userId)
                .setEmail(USER_EMAIL)
                .setFirstName(USER_FIRST_NAME)
                .setLastName(USER_LAST_NAME);
        Role managerRole = new Role();
        managerRole.setName(RoleName.MANAGER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.MANAGER)).thenReturn(Optional.of(managerRole));
        user.setRoles(Set.of(managerRole));
        when(userRepository.save(user)).thenReturn(user);
        UserResponseDto expected = new UserResponseDto(
                userId,
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                List.of(RoleName.MANAGER.toString())
        );
        when(userMapper.toDto(user)).thenReturn(expected);
        RoleRequestDto requestDto = new RoleRequestDto(
                Set.of(RoleName.MANAGER)
        );
        UserResponseDto actual = userService.updateRole(requestDto, userId);
        assertEquals(expected, actual);
        verify(userRepository).save(user);
        verify(roleRepository).findByName(RoleName.MANAGER);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper);
    }

    @Test
    @DisplayName("Update user roles when user not found should throw exception")
    void updateRole_UserNotFound_ThrowsException() {
        Long userId = 1L;
        RoleRequestDto requestDto = new RoleRequestDto(Set.of(RoleName.MANAGER));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateRole(requestDto, userId)
        );
        String expected = String.format("User with id %s not found", userId);
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Get current user")
    void getCurrentUser_WithExistingUser_ReturnUserDtoResponseWithoutRolesDto() {
        Long userId = 1L;
        String email = USER_EMAIL;
        User user = new User()
                .setEmail(email);
        UserResponseWithoutRolesDto expected = new UserResponseWithoutRolesDto(
                userId,
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME
        );
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toDtoWithoutRoles(user)).thenReturn(expected);
        UserResponseWithoutRolesDto actual = userService.getCurrentUserDto(authentication);
        assertEquals(expected, actual);
        verify(userRepository).findByEmail(email);
        verify(userMapper).toDtoWithoutRoles(user);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Get current user when user not found should throw exception")
    void getCurrentUser_UserDtoNotFound_ThrowsException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(USER_EMAIL);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getCurrentUserDto(authentication)
        );
        String expected = String.format("User with email %s not found", USER_EMAIL);
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(authentication, times(2)).getName();
        verify(userRepository).findByEmail(USER_EMAIL);
        verifyNoMoreInteractions(userRepository, authentication);
    }

    @Test
    @DisplayName("Check if user exists by ID")
    void existsById_WithExitsUser_ReturnTrue() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        assertTrue(userService.existsById(userId));
    }

    @Test
    @DisplayName("Update current user successfully")
    void updateCurrentUser_Success() {
        Long userId = 1L;
        User user = new User()
                .setId(userId)
                .setEmail(USER_EMAIL)
                .setFirstName(USER_FIRST_NAME)
                .setLastName(USER_LAST_NAME);
        UserRequestDto requestDto = new UserRequestDto()
                .setEmail(USER_EMAIL)
                .setFirstName(USER_FIRST_NAME)
                .setLastName(USER_LAST_NAME);
        UserResponseWithoutRolesDto expected = new UserResponseWithoutRolesDto(
                userId,
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME
        );
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(USER_EMAIL);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDtoWithoutRoles(user)).thenReturn(expected);
        UserResponseWithoutRolesDto actual = userService.updateCurrentUser(
                requestDto,
                authentication
        );
        assertEquals(expected, actual);
        verify(authentication).getName();
        verify(userRepository).findByEmail(USER_EMAIL);
        verify(userRepository).save(user);
        verify(userMapper).toDtoWithoutRoles(user);
        verifyNoMoreInteractions(userRepository, userMapper, authentication);
    }
}

