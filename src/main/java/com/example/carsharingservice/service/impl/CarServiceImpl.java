package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.dto.car.CreateCarRequestDto;
import com.example.carsharingservice.exception.AlreadyExistsException;
import com.example.carsharingservice.exception.DataNotFoundException;
import com.example.carsharingservice.exception.InvalidDataException;
import com.example.carsharingservice.mapper.CarMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.repository.CarRepository;
import com.example.carsharingservice.service.CarService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarResponseDto save(CreateCarRequestDto requestDto) {
        if (carRepository.existsByModelAndBrand(requestDto.model(), requestDto.brand())) {
            throw new AlreadyExistsException(
                    String.format(
                            "Car %s %s is already exists",
                            requestDto.brand(),
                            requestDto.model()
                    )
            );
        }
        Car car = carMapper.toModel(requestDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public List<CarResponseDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable).stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    public CarResponseDto findCarDtoById(Long id) {
        Car car = findCarById(id);
        return carMapper.toDto(car);
    }

    public CarResponseDto update(CarRequestDto carRequestDto, Long id) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(String.format("Car with id %s not found", id))
        );
        if (carRequestDto.getDailyFee() != null) {
            car.setDailyFee(carRequestDto.getDailyFee());
        }
        if (carRequestDto.getInventory() != null) {
            car.setInventory(carRequestDto.getInventory());
        }
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public void updateInventory(Car car, boolean increase) {
        int newInventory = increase ? car.getInventory() + 1 : car.getInventory() - 1;
        if (newInventory < 0) {
            throw new InvalidDataException(String.format(
                    "Car with ID %s has no inventory",
                    car.getId()
            ));
        }
        car.setInventory(newInventory);
        carRepository.save(car);
    }

    @Override
    public void delete(Long id) {
        carRepository.deleteById(id);
    }

    @Override
    public Car findCarById(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(String.format("Car with id %s not found", id))
        );
    }
}
