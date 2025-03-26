package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.dto.car.CreateCarRequestDto;
import com.example.carsharingservice.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;

    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Create a new car")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponseDto save(@RequestBody @Valid CreateCarRequestDto requestDto) {
        return carService.save(requestDto);
    }

    @Operation(summary = "Get all cars with pagination")
    @GetMapping
    public List<CarResponseDto> findAll(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @Operation(summary = "Get car by ID")
    @GetMapping("/{id}")
    public CarResponseDto findById(@PathVariable Long id) {
        return carService.findCarDtoById(id);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Update car details")
    @PatchMapping("/{id}")
    public CarResponseDto update(
            @RequestBody @Valid CarRequestDto carRequestDto,
            @PathVariable Long id
    ) {
        return carService.update(carRequestDto, id);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Delete car by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        carService.delete(id);
    }
}
