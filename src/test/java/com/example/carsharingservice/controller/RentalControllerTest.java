package com.example.carsharingservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingservice.dto.rental.CreateRentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class RentalControllerTest {
    protected static MockMvc mockMvc;
    private static final LocalDate RENTAL_RETURN_DATE = LocalDate.now().plusDays(2);
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/cars/add-model-s-to-cars-table.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/users/add-john-to-users-table.sql")
            );
        }
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
                            "database/rentals/remove-all-rentals.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/cars/remove-all-cars.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/users/remove-john-from-users.sql")
            );
        }
    }

    @WithMockUser(username = "john@mail.com", authorities = {"CUSTOMER"})
    @Test
    @DisplayName("Create rental with valid data")
    @Sql(scripts = {
            "classpath:database/rentals/remove-all-rentals.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void rent_ValidCreateRentalRequestDto_CarResponseDto()
            throws Exception {
        Long carId = 1L;
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                RENTAL_RETURN_DATE,
                carId
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        RentalResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), RentalResponseDto.class
        );
        assertNotNull(actual);
        assertEquals(LocalDate.now(), actual.getRentalDate());
        assertEquals(RENTAL_RETURN_DATE, actual.getReturnDate());
        assertEquals(carId, actual.getCar().id());
    }

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @DisplayName("Find active rentals by user ID")
    @Sql(scripts = {
            "classpath:database/rentals/add-rental-to-rentals-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/rentals/remove-all-rentals.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByUserIdAndActive_ValidCreateRentalRequestDto_RentalResponseDto()
            throws Exception {
        Long userId = 2L;
        RentalRequestDto requestDto = new RentalRequestDto(
                userId, true
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                        get("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        RentalResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalResponseDto[].class
        );
        assertNotNull(actual);
        assertEquals(1, actual.length);
        assertEquals(LocalDate.now(), actual[0].getRentalDate());
        assertEquals(RENTAL_RETURN_DATE, actual[0].getReturnDate());
        assertEquals(1, actual[0].getCar().id());
        assertEquals(userId, actual[0].getUserId());
    }

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @DisplayName("Find rentals for non-existent user - should return 404 Not Found")
    public void findByUserIdAndActive_NonExistentUserId_NotFoundStatus()
            throws Exception {
        Long userId = 3L;
        RentalRequestDto requestDto = new RentalRequestDto(
                userId, true
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        mockMvc.perform(
                        get("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
