package com.example.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CarRepositoryTest {
    private static final String CAR_MODEL = "Model S";
    private static final String CAR_BRAND = "Tesla";
    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Check if a car exists by model and brand")
    @Sql(scripts = {
            "classpath:database/cars/add-model-s-to-cars-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/cars/remove-all-cars.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void existsByModelAndBrand_WithExistsModelAndBrand_ReturnsTrue() {
        boolean actual = carRepository.existsByModelAndBrand(CAR_MODEL, CAR_BRAND);
        assertTrue(actual);
    }
}
