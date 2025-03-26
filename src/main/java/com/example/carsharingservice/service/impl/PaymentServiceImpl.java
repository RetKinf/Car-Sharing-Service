package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.api.stipe.StripeService;
import com.example.carsharingservice.api.telegram.TelegramNotificationService;
import com.example.carsharingservice.context.PaymentContext;
import com.example.carsharingservice.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingservice.dto.payment.PaymentDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.InvalidDataException;
import com.example.carsharingservice.mapper.PaymentMapper;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.PaymentStatus;
import com.example.carsharingservice.model.PaymentType;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.PaymentRepository;
import com.example.carsharingservice.service.PaymentService;
import com.example.carsharingservice.service.RentalService;
import com.example.carsharingservice.service.UserService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final TelegramNotificationService notificationService;
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final PaymentMapper paymentMapper;
    private final RentalService rentalService;
    private final StripeService stripeService;
    private final PaymentContext paymentContext;

    @Override
    public List<PaymentDto> findByUserId(Long id) {
        if (!userService.existsById(id)) {
            throw new EntityNotFoundException(
                    String.format("User with id %s not found", id)
            );
        }
        return paymentRepository.findByUserId(id).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public PaymentDto create(
            CreatePaymentRequestDto createPaymentRequestDto,
            Authentication authentication
    ) {
        Rental rental = rentalService.findRentalById(createPaymentRequestDto.rentalId());
        if (rental.isActive()) {
            throw new InvalidDataException("Rental is still active");
        }
        if (rental.getActualReturnDate() == null) {
            throw new InvalidDataException("Rental has no actual return date");
        }
        if (rental.getActualReturnDate().isAfter(rental.getReturnDate())
                && createPaymentRequestDto.type() == PaymentType.PAYMENT) {
            throw new InvalidDataException("Incorrect payment type");
        }
        Payment payment = paymentMapper.toModel(createPaymentRequestDto);
        payment.setStatus(PaymentStatus.PENDING);
        User user = userService.getCurrentUser(authentication);
        payment.setUser(user);
        BigDecimal amount = paymentContext.calculateAmount(rental, payment.getType());
        payment.setAmount(amount);
        Payment checkout = stripeService.createCheckout(payment);
        return paymentMapper.toDto(paymentRepository.save(checkout));
    }

    @Transactional
    @Override
    public void completePayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Payment with id %s not found", sessionId)
                )
        );
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        notificationService.sendMessage("Payment has been paid!\n" + payment);
    }
}
