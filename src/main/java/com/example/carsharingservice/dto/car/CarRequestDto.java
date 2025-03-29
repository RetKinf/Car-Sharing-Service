package com.example.carsharingservice.dto.car;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class CarRequestDto {
    @Positive
    private Integer inventory;
    @Positive
    private BigDecimal dailyFee;
}
