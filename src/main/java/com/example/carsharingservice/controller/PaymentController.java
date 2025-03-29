package com.example.carsharingservice.controller;

import com.example.carsharingservice.api.stipe.StripeService;
import com.example.carsharingservice.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingservice.dto.payment.PaymentDto;
import com.example.carsharingservice.exception.StripePaymentException;
import com.example.carsharingservice.service.PaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final StripeService stripeService;

    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Get payments by user ID")
    @GetMapping
    public List<PaymentDto> findByUserId(@RequestParam("user_id") Long id) {
        return paymentService.findByUserId(id);
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Create a new payment")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PaymentDto create(
            @RequestBody @Valid CreatePaymentRequestDto createPaymentRequestDto,
            Authentication authentication
    ) {
        return paymentService.create(createPaymentRequestDto, authentication);
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Handle successful payment")
    @GetMapping("/success")
    public String success(@RequestParam("session_id") String sessionId) {
        try {
            boolean isPaid = stripeService.checkSessionStatus(sessionId);
            if (isPaid) {
                paymentService.completePayment(sessionId);
                return "Payment successful!";
            } else {
                return "Payment failed or pending.";
            }
        } catch (StripeException e) {
            throw new StripePaymentException("Failed to retrieve session information", e);
        }
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Handle canceled payment")
    @GetMapping("/cancel")
    public String cancel() {
        return "Payment can be made later";
    }
}
