package com.example.carsharingservice.service;

import com.example.carsharingservice.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingservice.dto.payment.PaymentDto;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface PaymentService {
    List<PaymentDto> findByUserId(Long id);

    PaymentDto create(
            CreatePaymentRequestDto createPaymentRequestDto,
            Authentication authentication
    );

    void completePayment(String sessionId);
}
