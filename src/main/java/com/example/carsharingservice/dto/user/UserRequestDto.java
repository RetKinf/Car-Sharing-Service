package com.example.carsharingservice.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class UserRequestDto {
    @Email
    private String email;
    private String firstName;
    private String lastName;
}
