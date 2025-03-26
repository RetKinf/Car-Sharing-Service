package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.api.telegram.TelegramNotificationService;
import com.example.carsharingservice.dto.rental.CreateRentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.example.carsharingservice.dto.rental.RentalReturnDateDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.InvalidDataException;
import com.example.carsharingservice.mapper.RentalMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.RentalRepository;
import com.example.carsharingservice.service.CarService;
import com.example.carsharingservice.service.RentalService;
import com.example.carsharingservice.service.UserService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final UserService userService;
    private final CarService carService;
    private final RentalMapper rentalMapper;
    private final TelegramNotificationService notificationService;

    @Transactional
    @Override
    public RentalResponseDto rent(
            CreateRentalRequestDto createRentalRequestDto,
            Authentication authentication
    ) {
        if (!createRentalRequestDto.returnDate().isAfter(LocalDate.now())) {
            throw new InvalidDataException("Return date must be after Today");
        }
        Car car = carService.findCarById(createRentalRequestDto.carId());
        carService.updateInventory(car, false);
        Rental rental = rentalMapper.toModel(createRentalRequestDto);
        rental.setRentalDate(LocalDate.now());
        User user = userService.getCurrentUser(authentication);
        rental.setUser(user);
        Rental save = rentalRepository.save(rental);
        save.setCar(car);
        RentalResponseDto rentalDto = rentalMapper.toDto(save);
        notificationService.sendMessage("New rental was created:\n" + rental);
        return rentalDto;
    }

    @Override
    public List<RentalResponseDto> findByUserIdAndActive(RentalRequestDto rentalRequestDto) {
        if (!userService.existsById(rentalRequestDto.userId())) {
            throw new EntityNotFoundException(
                    String.format("User with id %s not found", rentalRequestDto.userId())
            );
        }
        return rentalRepository.findByUserIdAndActiveIs(
                        rentalRequestDto.userId(),
                        rentalRequestDto.isActive())
                .stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public RentalResponseDto findRentalDtoById(Long id) {
        Rental rental = findRentalById(id);
        return rentalMapper.toDto(rental);
    }

    @Transactional
    @Override
    public RentalResponseDto returnCar(RentalReturnDateDto requestDto, Long id) {
        Rental rental = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Rental with id %s not found", id))
        );
        if (!rental.isActive()) {
            throw new InvalidDataException(String.format("Rental with id %s is not active", id));
        }
        if (rental.getReturnDate().isAfter(requestDto.actualReturnDate())) {
            throw new InvalidDataException(
                    "The actual return date cannot be earlier than the return date"
            );
        }
        rental.setActualReturnDate(requestDto.actualReturnDate());
        rental.setActive(false);
        carService.updateInventory(rental.getCar(), true);
        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    @Override
    public Rental findRentalById(Long id) {
        return rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Rental with id %s not found", id))
        );
    }

    @Override
    public void checkOverdueRentals() {
        LocalDate today = LocalDate.now();
        List<Rental> overdueRentals = rentalRepository.findOverdueRentals(today);
        if (overdueRentals.isEmpty()) {
            notificationService.sendMessage("Don't have any overdue rentals");
        } else {
            StringBuilder message = new StringBuilder().append("Found ")
                    .append(overdueRentals.size())
                    .append(" overdue rentals");
            for (Rental rental : overdueRentals) {
                message.append(rental.toString())
                        .append("\n");
                notificationService.sendMessage(message.toString());
            }
        }
    }
}
