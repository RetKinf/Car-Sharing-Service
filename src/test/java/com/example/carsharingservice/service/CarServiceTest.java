package com.example.carsharingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.dto.car.CreateCarRequestDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.InvalidDataException;
import com.example.carsharingservice.mapper.CarMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.model.CarType;
import com.example.carsharingservice.repository.CarRepository;
import com.example.carsharingservice.service.impl.CarServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    private static final String CAR_MODEL_1 = "Impala 1967";
    private static final String CAR_BRAND_1 = "Chevrolet";
    private static final int CAR_INVENTORY_1 = 10;
    private static final BigDecimal CAR_DAILY_FEE_1 = BigDecimal.valueOf(50);
    private static final String CAR_MODEL_2 = "911";
    private static final String CAR_BRAND_2 = "Porsche";
    private static final int CAR_INVENTORY_2 = 4;
    private static final BigDecimal CAR_DAILY_FEE_2 = BigDecimal.valueOf(100);
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;
    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Verify save() method")
    public void save_WithValidCreateCarRequestDto_ReturnCarResponseDto() {
        CreateCarRequestDto createCarRequestDto = new CreateCarRequestDto(
                CAR_MODEL_1,
                CAR_BRAND_1,
                CarType.SEDAN,
                CAR_INVENTORY_1,
                CAR_DAILY_FEE_1
        );
        Car car = new Car()
                .setModel(CAR_MODEL_1)
                .setBrand(CAR_BRAND_1)
                .setType(CarType.SEDAN)
                .setInventory(CAR_INVENTORY_1)
                .setDailyFee(CAR_DAILY_FEE_1);
        CarResponseDto expected = new CarResponseDto(
                1L,
                CAR_MODEL_1,
                CAR_BRAND_1,
                CarType.SEDAN.toString(),
                CAR_INVENTORY_1,
                CAR_DAILY_FEE_1
        );
        when(carMapper.toModel(createCarRequestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);
        CarResponseDto actual = carService.save(createCarRequestDto);
        assertEquals(expected, actual);
        verify(carRepository).save(car);
        verify(carRepository).existsByModelAndBrand(CAR_MODEL_1, CAR_BRAND_1);
        verify(carMapper).toModel(createCarRequestDto);
        verify(carMapper).toDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Verify findCarDtoById() method")
    public void findById_WithValidCarId_ReturnCarResponseDto() {
        Long carId = 1L;
        Car car = new Car()
                .setId(carId)
                .setModel(CAR_MODEL_1)
                .setBrand(CAR_BRAND_1)
                .setType(CarType.SEDAN)
                .setInventory(CAR_INVENTORY_1)
                .setDailyFee(CAR_DAILY_FEE_1);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        CarResponseDto expected = carService.findCarDtoById(carId);
        CarResponseDto actual = carMapper.toDto(car);
        assertEquals(expected, actual);
        verify(carRepository).findById(carId);
        verify(carMapper, times(2)).toDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName(
            "Verify that an exception is thrown "
            + "when trying to find a non-existent Car by the given ID"
    )
    public void findCarById_WithNonExistingCarId_ThrowsException() {
        Long carId = 100L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> carService.findCarById(carId)
        );
        String expected = String.format("Car with id %s not found", carId);
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(carRepository).findById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify findAll method")
    public void findAll_WithPageable_ReturnCarResponseDtos() {
        Long carId1 = 1L;
        Car car1 = new Car()
                .setId(carId1)
                .setModel(CAR_MODEL_1)
                .setBrand(CAR_BRAND_1)
                .setType(CarType.SEDAN)
                .setInventory(CAR_INVENTORY_1)
                .setDailyFee(CAR_DAILY_FEE_1);
        Long carId2 = 2L;
        Car car2 = new Car()
                .setId(carId2)
                .setModel(CAR_MODEL_2)
                .setBrand(CAR_BRAND_2)
                .setType(CarType.SEDAN)
                .setInventory(CAR_INVENTORY_2)
                .setDailyFee(CAR_DAILY_FEE_2);
        CarResponseDto carResponseDto1 = new CarResponseDto(
                carId1,
                CAR_MODEL_1,
                CAR_BRAND_1,
                CarType.SEDAN.toString(),
                CAR_INVENTORY_1,
                CAR_DAILY_FEE_1
        );
        CarResponseDto carResponseDto2 = new CarResponseDto(
                carId2,
                CAR_MODEL_2,
                CAR_BRAND_2,
                CarType.SEDAN.toString(),
                CAR_INVENTORY_2,
                CAR_DAILY_FEE_2
        );
        Pageable pageable = PageRequest.of(0, 10);
        List<Car> cars = List.of(car1, car2);
        Page<Car> page = new PageImpl<>(cars, pageable, cars.size());
        when(carRepository.findAll(pageable)).thenReturn(page);
        when(carMapper.toDto(car1)).thenReturn(carResponseDto1);
        when(carMapper.toDto(car2)).thenReturn(carResponseDto2);
        List<CarResponseDto> actual = carService.findAll(pageable);
        assertEquals(2, actual.size());
        assertEquals(carResponseDto1, actual.get(0));
        assertEquals(carResponseDto2, actual.get(1));
        verify(carRepository).findAll(pageable);
        verify(carMapper).toDto(car1);
        verify(carMapper).toDto(car2);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Verify update method")
    public void updateCar_WithValidCarRequestDtoAndCarId_ReturnCarResponseDto() {
        Long carId = 1L;
        Car car = new Car()
                .setId(carId)
                .setModel(CAR_MODEL_1)
                .setBrand(CAR_BRAND_1)
                .setType(CarType.SEDAN)
                .setInventory(CAR_INVENTORY_1)
                .setDailyFee(CAR_DAILY_FEE_1);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        car.setInventory(CAR_INVENTORY_2);
        car.setDailyFee(CAR_DAILY_FEE_2);
        when(carRepository.save(car)).thenReturn(car);
        CarResponseDto expected = new CarResponseDto(
                carId,
                CAR_MODEL_1,
                CAR_BRAND_1,
                CarType.SEDAN.toString(),
                CAR_INVENTORY_2,
                CAR_DAILY_FEE_2
        );
        when(carMapper.toDto(car)).thenReturn(expected);
        CarRequestDto carRequestDto = new CarRequestDto()
                .setInventory(CAR_INVENTORY_2)
                .setDailyFee(CAR_DAILY_FEE_2);
        CarResponseDto actual = carService.update(carRequestDto, carId);
        assertEquals(expected, actual);
        verify(carRepository).findById(carId);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Verify delete method")
    public void deleteCar_WithValidId_DeleteCar() {
        Long carId = 1L;
        doNothing().when(carRepository).deleteById(carId);
        carService.delete(carId);
        verify(carRepository).deleteById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify findCarById method")
    public void findCarById_WithExistingCarId_ReturnsCar() {
        Long carId = 1L;
        Car car = new Car()
                .setId(carId)
                .setModel(CAR_MODEL_1)
                .setBrand(CAR_BRAND_1)
                .setType(CarType.SEDAN)
                .setInventory(CAR_INVENTORY_1)
                .setDailyFee(CAR_DAILY_FEE_1);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        Car actual = carService.findCarById(carId);
        assertEquals(car, actual);
        verify(carRepository).findById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify findCarById method with non-existent carId throws EntityNotFoundException")
    public void findCarById_WithNonExistentCarId_ThrowsEntityNotFoundException() {
        Long carId = 999L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> carService.findCarById(carId)
        );
        String expected = String.format("Car with id %s not found", carId);
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(carRepository).findById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify updateInventory method with increase = true")
    public void updateInventory_WithIncrease_UpdatesInventory() {
        Long carId = 1L;
        Car car = new Car()
                .setId(carId)
                .setModel(CAR_MODEL_1)
                .setBrand(CAR_BRAND_1)
                .setType(CarType.SEDAN)
                .setInventory(CAR_INVENTORY_1)
                .setDailyFee(CAR_DAILY_FEE_1);
        when(carRepository.save(car)).thenReturn(car);
        carService.updateInventory(car, true);
        assertEquals(CAR_INVENTORY_1 + 1, car.getInventory());
        verify(carRepository).save(car);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify updateInventory method with increase false")
    public void updateInventory_WithDecrease_UpdatesInventory() {
        Long carId = 1L;
        Car car = new Car()
                .setId(carId)
                .setModel(CAR_MODEL_1)
                .setBrand(CAR_BRAND_1)
                .setType(CarType.SEDAN)
                .setInventory(CAR_INVENTORY_1)
                .setDailyFee(CAR_DAILY_FEE_1);
        when(carRepository.save(car)).thenReturn(car);
        carService.updateInventory(car, false);
        assertEquals(CAR_INVENTORY_1 - 1, car.getInventory());
        verify(carRepository).save(car);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify updateInventory method throws InvalidDataException")
    public void updateInventory_WithDecreaseAndZeroInventory_ThrowsInvalidDataException() {
        Long carId = 1L;
        Car car = new Car()
                .setId(carId)
                .setModel(CAR_MODEL_1)
                .setBrand(CAR_BRAND_1)
                .setType(CarType.SEDAN)
                .setInventory(0)
                .setDailyFee(CAR_DAILY_FEE_1);
        Exception exception = assertThrows(
                InvalidDataException.class,
                () -> carService.updateInventory(car, false)
        );
        String expected = String.format("Car with ID %s has no inventory", carId);
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verifyNoInteractions(carRepository);
    }
}
