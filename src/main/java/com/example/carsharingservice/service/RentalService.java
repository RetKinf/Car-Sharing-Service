package com.example.carsharingservice.service;

import com.example.carsharingservice.dto.rental.CreateRentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.example.carsharingservice.dto.rental.RentalReturnDateDto;
import com.example.carsharingservice.model.Rental;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface RentalService {
    RentalResponseDto rent(
            CreateRentalRequestDto createRentalRequestDto,
            Authentication authentication
    );

    List<RentalResponseDto> findByUserIdAndActive(RentalRequestDto rentalRequestDto);

    RentalResponseDto findRentalDtoById(Long id);

    RentalResponseDto returnCar(RentalReturnDateDto requestDto, Long id);

    Rental findRentalById(Long id);

    void checkOverdueRentals();
}
