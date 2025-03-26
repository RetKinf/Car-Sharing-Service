package com.example.carsharingservice.service;

import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.dto.car.CreateCarRequestDto;
import com.example.carsharingservice.model.Car;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto save(CreateCarRequestDto requestDto);

    List<CarResponseDto> findAll(Pageable pageable);

    CarResponseDto findCarDtoById(Long id);

    CarResponseDto update(CarRequestDto carRequestDto, Long id);

    void updateInventory(Car car, boolean increase);

    void delete(Long id);

    Car findCarById(Long id);
}
