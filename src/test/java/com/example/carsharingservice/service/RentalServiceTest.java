package com.example.carsharingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.carsharingservice.api.telegram.TelegramNotificationService;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.dto.rental.CreateRentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.example.carsharingservice.exception.InvalidDataException;
import com.example.carsharingservice.mapper.RentalMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.model.CarType;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.RentalRepository;
import com.example.carsharingservice.service.impl.RentalServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    private static final LocalDate RENTAL_RENT_DATE = LocalDate.now();
    private static final LocalDate RENTAL_RETURN_DATE = RENTAL_RENT_DATE.plusDays(2);
    private static final String CAR_MODEL = "Model S";
    private static final String CAR_BRAND = "Tesla";
    private static final int CAR_INVENTORY = 5;
    private static final BigDecimal CAR_DAILY_FEE = BigDecimal.valueOf(50);
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private TelegramNotificationService notificationService;
    @Mock
    private UserService userService;
    @Mock
    private CarService carService;
    @Mock
    private RentalMapper rentalMapper;
    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Verify rent method with valid data")
    public void rent_WithValidCreateCarRequestDto_ReturnRentalResponseDto() {
        long carId = 1L;
        User user = new User()
                .setId(1L);
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                RENTAL_RETURN_DATE,
                carId
        );
        Car car = new Car()
                .setId(carId)
                .setModel(CAR_MODEL)
                .setBrand(CAR_BRAND)
                .setType(CarType.SEDAN)
                .setInventory(CAR_INVENTORY)
                .setDailyFee(CAR_DAILY_FEE);
        Rental rental = new Rental()
                .setRentalDate(LocalDate.now())
                .setReturnDate(requestDto.returnDate())
                .setCar(car);
        CarResponseDto carDto = new CarResponseDto(
                carId,
                CAR_MODEL,
                CAR_BRAND,
                CarType.SEDAN.toString(),
                CAR_INVENTORY,
                CAR_DAILY_FEE
        );
        RentalResponseDto expected = new RentalResponseDto()
                .setRentalDate(RENTAL_RENT_DATE)
                .setReturnDate(RENTAL_RETURN_DATE)
                .setCar(carDto);
        Authentication authentication = mock(Authentication.class);
        when(userService.getCurrentUser(authentication)).thenReturn(user);
        when(carService.findCarById(requestDto.carId())).thenReturn(car);
        when(rentalMapper.toModel(requestDto)).thenReturn(rental);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(expected);
        RentalResponseDto actual = rentalService.rent(requestDto, authentication);
        assertEquals(expected, actual);
        verify(userService).getCurrentUser(authentication);
        verify(carService).findCarById(requestDto.carId());
        verify(carService).updateInventory(car, false);
        verify(rentalMapper).toModel(requestDto);
        verify(rentalRepository).save(rental);
        verify(rentalMapper).toDto(rental);
        verify(notificationService).sendMessage(anyString());
        verifyNoMoreInteractions(
                carService,
                rentalMapper,
                rentalRepository,
                userService,
                notificationService
        );
    }

    @Test
    @DisplayName("Verify rent method with invalid return date")
    public void rent_WithInvalidReturnDate_ThrowsInvalidDataException() {
        long carId = 1L;
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                RENTAL_RENT_DATE,
                carId
        );
        Exception exception = assertThrows(
                InvalidDataException.class,
                () -> rentalService.rent(requestDto, mock(Authentication.class))
        );
        String expected = "Return date must be after Today";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verifyNoInteractions(
                carService,
                rentalMapper,
                rentalRepository,
                notificationService
        );
    }

    @Test
    @DisplayName("Verify findByUserIdAndActive method")
    public void findByUserIdAndActive_WithValidUserIdAndActive_ReturnListOfRentalResponseDto() {
        long userId = 1L;
        RentalRequestDto requestDto = new RentalRequestDto(userId, true);
        User user = new User().setId(userId);
        Rental rental = new Rental()
                .setRentalDate(RENTAL_RENT_DATE)
                .setReturnDate(RENTAL_RETURN_DATE)
                .setUser(user);
        RentalResponseDto expected = new RentalResponseDto()
                .setRentalDate(RENTAL_RENT_DATE)
                .setReturnDate(RENTAL_RETURN_DATE)
                .setUserId(userId);
        when(userService.existsById(userId)).thenReturn(true);
        when(rentalRepository.findByUserIdAndActiveIs(userId, requestDto.isActive()))
                .thenReturn(List.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expected);
        List<RentalResponseDto> actual = rentalService.findByUserIdAndActive(requestDto);
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
        verify(userService).existsById(requestDto.userId());
        verify(rentalRepository).findByUserIdAndActiveIs(
                requestDto.userId(),
                requestDto.isActive()
        );
        verify(rentalMapper).toDto(rental);
        verifyNoMoreInteractions(userService, rentalRepository, rentalMapper);
    }
}
