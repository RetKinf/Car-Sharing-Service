package com.example.carsharingservice.dto.user;

import com.example.carsharingservice.validation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Accessors(chain = true)
@FieldMatch(
        firstField = "password",
        secondField = "repeatPassword",
        message = "Password don't match"
)
public record UserRegistrationRequestDto(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        @Length(min = 8, max = 20)
        String password,
        @NotBlank
        @Length(min = 8, max = 20)
        String repeatPassword
) {
}
