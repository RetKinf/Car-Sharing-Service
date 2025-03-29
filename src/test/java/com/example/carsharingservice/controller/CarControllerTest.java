package com.example.carsharingservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.dto.car.CreateCarRequestDto;
import com.example.carsharingservice.model.CarType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
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
public class CarControllerTest {
    protected static MockMvc mockMvc;
    private static final String CAR_MODEL_1 = "Model S";
    private static final String CAR_BRAND_1 = "Tesla";
    private static final int CAR_INVENTORY_1 = 10;
    private static final BigDecimal CAR_DAILY_FEE_1 = BigDecimal.valueOf(50.00);
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

    @AfterEach
    void afterEach(
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
                            "database/cars/remove-all-cars.sql")
            );
        }
    }

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @DisplayName("Create a new car")
    public void createCar_validCreateCarRequestDto_ReturnCarResponseDto()
            throws Exception {
        CreateCarRequestDto requestDto = new CreateCarRequestDto(
                CAR_MODEL_1,
                CAR_BRAND_1,
                CarType.SEDAN,
                CAR_INVENTORY_1,
                CAR_DAILY_FEE_1
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        CarResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CarResponseDto.class
        );
        assertNotNull(actual);
        assertEquals(CAR_MODEL_1, actual.model());
        assertEquals(CAR_BRAND_1, actual.brand());
        assertEquals(CarType.SEDAN.toString(), actual.type());
        assertEquals(CAR_INVENTORY_1, actual.inventory());
        assertEquals(CAR_DAILY_FEE_1, actual.dailyFee());
    }

    @Test
    @DisplayName("Find all cars")
    @Sql(scripts = {
            "classpath:database/cars/add-model-s-to-cars-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void findAllCars_GivenCarsInCatalog_ReturnAllCarsResponseDto()
            throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/cars")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        CarResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarResponseDto[].class
        );
        assertNotNull(actual);
        assertEquals(CAR_MODEL_1, actual[0].model());
        assertEquals(CAR_BRAND_1, actual[0].brand());
        assertEquals(CarType.SEDAN.toString(), actual[0].type());
        assertEquals(CAR_INVENTORY_1, actual[0].inventory());
        assertEquals(
                CAR_DAILY_FEE_1.stripTrailingZeros(),
                actual[0].dailyFee().stripTrailingZeros()
        );
    }

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @DisplayName("Delete car by ID")
    @Sql(scripts = {
            "classpath:database/cars/add-model-s-to-cars-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void deleteCar_ValidCarId_ReturnNoContentStatus()
            throws Exception {
        mockMvc.perform(
                        delete("/cars/{id}", 1L)
                )
                .andExpect(status().isNoContent())
                .andReturn();
    }
}
