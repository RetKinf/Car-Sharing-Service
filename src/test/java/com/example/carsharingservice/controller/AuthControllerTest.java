package com.example.carsharingservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingservice.dto.user.UserLoginRequestDto;
import com.example.carsharingservice.dto.user.UserLoginResponseDto;
import com.example.carsharingservice.dto.user.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.user.UserResponseWithoutRolesDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class AuthControllerTest {
    protected static MockMvc mockMvc;
    private static final String USER_EMAIL = "john@mail.com";
    private static final String USER_PASSWORD = "Secure123";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Doe";
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @AfterAll
    static void afterAll(
            @Autowired DataSource dataSource
    ) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/users-roles/remove-john-roles.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/users/remove-john-from-users.sql")
            );
        }
    }

    @Test
    @DisplayName("Register new user")
    @Sql(scripts = {
            "classpath:database/users-roles/remove-john-roles.sql",
            "classpath:database/users/remove-john-from-users.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void register_ValidUserRegistrationRequestDto_UserResponseWithoutRolesDto()
            throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PASSWORD,
                USER_PASSWORD
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                        post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();
        UserResponseWithoutRolesDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseWithoutRolesDto.class
        );
        assertNotNull(actual);
        assertEquals(USER_EMAIL, actual.email());
        assertEquals(USER_FIRST_NAME, actual.firstName());
        assertEquals(USER_LAST_NAME, actual.lastName());
    }

    @Test
    @DisplayName("Login with existing user")
    @Sql(scripts = {
            "classpath:database/users/add-john-to-users-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/users-roles/remove-john-roles.sql",
            "classpath:database/users/remove-john-from-users.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void login_ExistentUser_Token() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                USER_EMAIL,
                USER_PASSWORD
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        UserLoginResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserLoginResponseDto.class
        );
        assertNotNull(actual);
    }

    @Test
    @DisplayName("Login with non-existent user - should return 404 Not Found")
    public void login_NonExistentUser_NotFoundStatus() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                USER_EMAIL,
                USER_PASSWORD
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
