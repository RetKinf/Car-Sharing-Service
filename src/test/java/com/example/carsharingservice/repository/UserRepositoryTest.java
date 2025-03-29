package com.example.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.carsharingservice.model.Role;
import com.example.carsharingservice.model.RoleName;
import com.example.carsharingservice.model.User;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    private static final String USER_EMAIL = "admin@mail.com";
    private static final String USER_PASSWORD
            = "$2a$10$rnjsWIrqAG7b7MjPXR1Kfu52XkUdnQ0Fhq4sIzc9oCBeyrmofh1PW";
    private static final String USER_FIRST_NAME = "Vladlen";
    private static final String USER_LAST_NAME = "Soloviov";

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Find user by email")
    public void findByEmail_WithValidEmail_ReturnsTrue() {
        Role role = new Role()
                .setId(1L)
                .setName(RoleName.MANAGER);
        User expected = new User()
                .setId(1L)
                .setEmail(USER_EMAIL)
                .setPassword(USER_PASSWORD)
                .setFirstName(USER_FIRST_NAME)
                .setLastName(USER_LAST_NAME)
                .setRoles(Set.of(role));
        Optional<User> actual = userRepository.findByEmail(USER_EMAIL);
        User actualUser = actual.orElse(null);
        assertEquals(expected, actualUser);
    }

    @Test
    @DisplayName("Check if user exists by email")
    public void existsByEmail_WithExistEmail_ReturnsTrue() {
        boolean actual = userRepository.existsByEmail(USER_EMAIL);
        assertTrue(actual);
    }
}
