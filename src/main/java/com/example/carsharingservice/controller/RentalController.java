package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.rental.CreateRentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.example.carsharingservice.dto.rental.RentalReturnDateDto;
import com.example.carsharingservice.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@EnableScheduling
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Rent a car")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RentalResponseDto rent(
            @RequestBody @Valid CreateRentalRequestDto createRentalRequestDto,
            Authentication authentication
    ) {
        return rentalService.rent(createRentalRequestDto, authentication);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Get rentals by user ID and active status")
    @GetMapping
    public List<RentalResponseDto> findByUserIdAndActive(
            @RequestBody RentalRequestDto rentalRequestDto
    ) {
        return rentalService.findByUserIdAndActive(rentalRequestDto);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Get rental by ID")
    @GetMapping("/{id}")
    public RentalResponseDto findById(@PathVariable Long id) {
        return rentalService.findRentalDtoById(id);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Return a rented car")
    @PostMapping("/{id}")
    public RentalResponseDto returnCar(
            @RequestBody @Valid RentalReturnDateDto requestDto,
            @PathVariable Long id
    ) {
        return rentalService.returnCar(requestDto, id);
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkOverdueRentals() {
        rentalService.checkOverdueRentals();
    }
}
