package com.example.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.carsharingservice.model.Role;
import com.example.carsharingservice.model.RoleName;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Find Role by name")
    public void findByName_WithExistsRole_ReturnRole() {
        Role expected = new Role()
                .setId(1L)
                .setName(RoleName.MANAGER);
        Optional<Role> actual = roleRepository.findByName(RoleName.MANAGER);
        assertEquals(expected, actual.orElse(null));
    }
}
