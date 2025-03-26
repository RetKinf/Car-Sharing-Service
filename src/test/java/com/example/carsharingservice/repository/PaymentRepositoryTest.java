package com.example.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.PaymentStatus;
import com.example.carsharingservice.model.PaymentType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {
    private static final String SESSION_ID = "Session ID 1234";
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Find payments by user ID")
    @Sql(scripts = {
            "classpath:database/cars/add-model-s-to-cars-table.sql",
            "classpath:database/users/add-john-to-users-table.sql",
            "classpath:database/rentals/add-rental-to-rentals-table.sql",
            "classpath:database/payments/add-payment-to-payments-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/remove-all-payments.sql",
            "classpath:database/rentals/remove-all-rentals.sql",
            "classpath:database/cars/remove-all-cars.sql",
            "classpath:database/users-roles/remove-john-roles.sql",
            "classpath:database/users/remove-john-from-users.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByUserId_WithValidData_ReturnListPayment() {
        List<Payment> payments = paymentRepository.findByUserId(2L);
        Payment actual = payments.get(0);
        assertEquals(1L, actual.getId());
        assertEquals(PaymentType.PAYMENT, actual.getType());
        assertEquals(PaymentStatus.PAID, actual.getStatus());
        assertEquals(SESSION_ID, actual.getSessionId());
        assertEquals(1L, actual.getRental().getId());
        assertEquals(2L, actual.getUser().getId());
    }

    @Test
    @DisplayName("Find payment by session ID")
    @Sql(scripts = {
            "classpath:database/cars/add-model-s-to-cars-table.sql",
            "classpath:database/users/add-john-to-users-table.sql",
            "classpath:database/rentals/add-rental-to-rentals-table.sql",
            "classpath:database/payments/add-payment-to-payments-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/remove-all-payments.sql",
            "classpath:database/rentals/remove-all-rentals.sql",
            "classpath:database/cars/remove-all-cars.sql",
            "classpath:database/users/remove-john-from-users.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findBySessionId_WithValidData_ReturnListPayment() {
        Optional<Payment> paymentOptional = paymentRepository.findBySessionId(SESSION_ID);
        Payment actual = paymentOptional.orElse(null);
        assertNotNull(actual);
        assertEquals(1L, actual.getId());
        assertEquals(PaymentType.PAYMENT, actual.getType());
        assertEquals(PaymentStatus.PAID, actual.getStatus());
        assertEquals(SESSION_ID, actual.getSessionId());
        assertEquals(1L, actual.getRental().getId());
        assertEquals(2L, actual.getUser().getId());
    }
}
