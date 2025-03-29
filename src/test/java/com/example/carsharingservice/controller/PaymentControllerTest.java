package com.example.carsharingservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingservice.api.stipe.StripeService;
import com.example.carsharingservice.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingservice.dto.payment.PaymentDto;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.PaymentStatus;
import com.example.carsharingservice.model.PaymentType;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
public class PaymentControllerTest {
    protected static MockMvc mockMvc;
    private static final BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(100);
    private static final String PAYMENT_SESSION_ID = "Session ID 1234";
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
                            "database/payments/remove-all-payments.sql")
            );
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

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @DisplayName("Find payments by existing user ID")
    @Sql(scripts = {
            "classpath:database/rentals/add-rental-to-rentals-table.sql",
            "classpath:database/payments/add-payment-to-payments-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/remove-all-payments.sql",
            "classpath:database/rentals/remove-all-rentals.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByUserId_ExistsPayments_ListPayment() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/payments")
                                .param("user_id", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        PaymentDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PaymentDto[].class
        );
        assertNotNull(actual[0]);
        assertEquals(1, actual.length);
        assertEquals(
                PAYMENT_AMOUNT.stripTrailingZeros(),
                actual[0].amount().stripTrailingZeros()
        );
        assertEquals(PaymentType.PAYMENT.toString(), actual[0].type());
        assertNotNull(actual[0].sessionUrl());
        assertEquals(PAYMENT_SESSION_ID, actual[0].sessionId());
    }

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @DisplayName("Get payments by non-existent user ID - should return 404 Not Found")
    public void findByUserId_NonExistsPayment_ListPayment() throws Exception {
        mockMvc.perform(
                        get("/payments")
                                .param("user_id", "50")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @WithMockUser(username = "john@mail.com", authorities = {"CUSTOMER"})
    @Test
    @DisplayName("Create payment for rental")
    @Sql(scripts = {
            "classpath:database/rentals/add-paid-rental-to-rentals-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/remove-all-payments.sql",
            "classpath:database/rentals/remove-all-rentals.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void create_ValidCreatePaymentRequestDto_PaymentDto() throws Exception {
        Long rentalId = 1L;
        CreatePaymentRequestDto createPaymentRequestDto = new CreatePaymentRequestDto(
                rentalId,
                PaymentType.PAYMENT
        );
        String jsonRequest = objectMapper.writeValueAsString(createPaymentRequestDto);
        MvcResult result = mockMvc.perform(
                        post("/payments")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();
        PaymentDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PaymentDto.class
        );
        assertNotNull(actual);
        assertEquals(PaymentType.PAYMENT.toString(), actual.type());
        assertEquals(rentalId, actual.rentalId());
        assertEquals(
                PAYMENT_AMOUNT.stripTrailingZeros(),
                actual.amount().stripTrailingZeros()
        );
        assertNotNull(actual.sessionUrl());
        assertNotNull(actual.sessionId());
    }
}
