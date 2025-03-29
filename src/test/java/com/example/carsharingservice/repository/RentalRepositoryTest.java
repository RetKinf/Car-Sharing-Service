package com.example.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.carsharingservice.model.Rental;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RentalRepositoryTest {
    private static final LocalDate RENTAL_RENTAL_DATE = LocalDate.now();
    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("Find rentals by user ID")
    @Sql(scripts = {
            "classpath:database/cars/add-model-s-to-cars-table.sql",
            "classpath:database/users/add-john-to-users-table.sql",
            "classpath:database/rentals/add-rental-to-rentals-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/rentals/remove-all-rentals.sql",
            "classpath:database/cars/remove-all-cars.sql",
            "classpath:database/users/remove-john-from-users.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByUserIdAndActiveIs_WithExistsRental_ReturnListOfRental() {
        List<Rental> actualList = rentalRepository.findByUserIdAndActiveIs(2L, true);
        Rental actual = actualList.get(0);
        assertEquals(1L, actual.getId());
        assertEquals(RENTAL_RENTAL_DATE, actual.getRentalDate());
        assertEquals(RENTAL_RENTAL_DATE.plusDays(2), actual.getReturnDate());
        assertEquals(1L, actual.getCar().getId());
        assertEquals(2L, actual.getUser().getId());
    }

    @Test
    @DisplayName("Find overdue rentals")
    @Sql(scripts = {
            "classpath:database/cars/add-model-s-to-cars-table.sql",
            "classpath:database/users/add-john-to-users-table.sql",
            "classpath:database/rentals/add-overdue-rental-to-rentals-table.sql",
            "classpath:database/rentals/add-rental-to-rentals-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/rentals/remove-all-rentals.sql",
            "classpath:database/cars/remove-all-cars.sql",
            "classpath:database/users/remove-john-from-users.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findOverdueRentals_WithOverdueRental_ReturnListOfRental() {
        List<Rental> actual = rentalRepository.findOverdueRentals(LocalDate.now());
        assertEquals(1, actual.size());
        assertNotNull(actual.get(0));
    }
}
