package com.example.carsharingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.carsharingservice.api.stipe.StripeService;
import com.example.carsharingservice.context.PaymentContext;
import com.example.carsharingservice.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingservice.dto.payment.PaymentDto;
import com.example.carsharingservice.exception.DataNotFoundException;
import com.example.carsharingservice.mapper.PaymentMapper;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.PaymentStatus;
import com.example.carsharingservice.model.PaymentType;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.PaymentRepository;
import com.example.carsharingservice.service.impl.PaymentServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class PaymentServiceTest {
    private static final LocalDate RENTAL_RENT_DATE = LocalDate.now();
    private static final LocalDate RENTAL_RETURN_DATE = RENTAL_RENT_DATE.plusDays(2);
    private static final String SESSION_ID = "sessionId";
    @Mock
    private PaymentContext paymentContext;
    @Mock
    private StripeService stripeService;
    @Mock
    private RentalService rentalService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private UserService userService;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Verify findByUserId method")
    public void findByUserId_WithValidUserId_ReturnsListOfPaymentDto() {
        Long userId = 1L;
        Long paymentId = 1L;
        Long rentalId = 1L;
        User user = new User().setId(userId);
        Payment payment = new Payment()
                .setId(paymentId)
                .setStatus(PaymentStatus.PENDING)
                .setType(PaymentType.PAYMENT)
                .setAmount(BigDecimal.valueOf(100.0))
                .setUser(user);
        PaymentDto expected = new PaymentDto(
                paymentId,
                PaymentType.PAYMENT.toString(),
                rentalId,
                BigDecimal.valueOf(100.0),
                null,
                null
        );
        when(userService.existsById(userId)).thenReturn(true);
        when(paymentRepository.findByUserId(userId)).thenReturn(List.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(expected);
        List<PaymentDto> actual = paymentService.findByUserId(userId);
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
        verify(userService).existsById(userId);
        verify(paymentRepository).findByUserId(userId);
        verify(paymentMapper).toDto(payment);
        verifyNoMoreInteractions(userService, paymentRepository, paymentMapper);
    }

    @Test
    @DisplayName("Verify findByUserId method with non-existent user ID")
    public void findByUserId_WithNonExistentUserId_ThrowsEntityNotFoundException() {
        Long userId = 999L;
        when(userService.existsById(userId)).thenReturn(false);
        Exception exception = assertThrows(
                DataNotFoundException.class,
                () -> paymentService.findByUserId(userId)
        );
        String expected = String.format("User with id %s not found", userId);
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(userService).existsById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Verify create method")
    public void create_WithValidCreatePaymentRequestDto_ReturnsPaymentDto() {
        Long rentalId = 1L;
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto(
                rentalId,
                PaymentType.PAYMENT
        );
        Rental rental = new Rental()
                .setId(rentalId)
                .setRentalDate(RENTAL_RENT_DATE)
                .setReturnDate(RENTAL_RETURN_DATE)
                .setActualReturnDate(RENTAL_RETURN_DATE)
                .setActive(false);
        User user = new User()
                .setId(1L);
        Payment payment = new Payment()
                .setId(1L)
                .setStatus(PaymentStatus.PENDING)
                .setType(PaymentType.PAYMENT)
                .setAmount(BigDecimal.valueOf(100.0))
                .setUser(user);
        PaymentDto expected = new PaymentDto(
                1L,
                PaymentStatus.PENDING.toString(),
                rentalId,
                BigDecimal.valueOf(100.0),
                null,
                SESSION_ID
        );

        when(rentalService.findRentalById(requestDto.rentalId())).thenReturn(rental);
        when(paymentMapper.toModel(requestDto)).thenReturn(payment);
        when(paymentContext.calculateAmount(rental, payment.getType()))
                .thenReturn(BigDecimal.valueOf(100.0));
        when(stripeService.createCheckout(payment)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expected);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        PaymentDto actual = paymentService.create(requestDto, authentication);
        assertEquals(expected, actual);
        verify(rentalService).findRentalById(requestDto.rentalId());
        verify(paymentMapper).toModel(requestDto);
        verify(paymentContext).calculateAmount(rental, payment.getType());
        verify(stripeService).createCheckout(payment);
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toDto(payment);
        verifyNoMoreInteractions(
                rentalService,
                paymentMapper,
                paymentContext,
                stripeService,
                paymentRepository
        );
    }
}
