package com.example.carsharingservice.api.stipe;

import com.example.carsharingservice.exception.StripePaymentException;
import com.example.carsharingservice.model.Payment;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class StripeService {
    public StripeService(@Value("${stripe.api.key}") String apiKey) {
        Stripe.apiKey = apiKey;
    }

    @Transactional
    public Payment createCheckout(Payment payment) {
        SessionCreateParams.LineItem.PriceData.ProductData productData
                = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName("Car rental payment")
                .build();
        SessionCreateParams.LineItem.PriceData priceData
                = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("USD")
                .setUnitAmountDecimal(payment.getAmount().multiply(BigDecimal.valueOf(100)))
                .setProductData(productData)
                .build();
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(priceData)
                .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(
                        ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/payments/success")
                                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                                .build()
                                .toString()
                )
                .setCancelUrl(
                        ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/payments/cancel")
                                .build()
                                .toString()
                )
                .addLineItem(lineItem)
                .build();
        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            throw new StripePaymentException("Failed to create Stripe session", e) {
            };
        }
        payment.setSessionId(session.getId());
        try {
            payment.setSessionUrl(new URL(session.getUrl()));
        } catch (MalformedURLException e) {
            throw new StripePaymentException("Invalid session URL",e);
        }
        return payment;
    }

    public boolean checkSessionStatus(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        return "paid".equals(session.getPaymentStatus());
    }
}
