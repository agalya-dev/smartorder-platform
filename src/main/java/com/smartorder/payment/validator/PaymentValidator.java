package com.smartorder.payment.validator;

import com.smartorder.exception.ErrorDetail;
import com.smartorder.exception.ValidationException;
import com.smartorder.payment.request.PaymentRequest;
import com.smartorder.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentValidator {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentValidator.class);

    public void validate(PaymentRequest request) {

        log.info("Validating payment request for order: {}",
                request.getOrderId());

        List<ErrorDetail> errors = new ArrayList<>();

        // Validate orderId
        if (ValidationUtil.isNullOrEmpty(request.getOrderId())) {
            errors.add(ErrorDetail.builder()
                    .field("orderId")
                    .message("Order ID is required")
                    .rejectedValue("null")
                    .build());
        }

        // Validate userId
        if (ValidationUtil.isNullOrEmpty(request.getUserId())) {
            errors.add(ErrorDetail.builder()
                    .field("userId")
                    .message("User ID is required")
                    .rejectedValue("null")
                    .build());
        } else if (ValidationUtil.exceedsMaxLength(
                request.getUserId(), 50)) {
            errors.add(ErrorDetail.builder()
                    .field("userId")
                    .message("User ID must not exceed 50 characters")
                    .rejectedValue(request.getUserId())
                    .build());
        }

        // Validate userName
        if (ValidationUtil.isNullOrEmpty(request.getUserName())) {
            errors.add(ErrorDetail.builder()
                    .field("userName")
                    .message("User name is required")
                    .rejectedValue("null")
                    .build());
        }

        // Validate amount
        if (ValidationUtil.isNullOrZero(request.getAmount())) {
            errors.add(ErrorDetail.builder()
                    .field("amount")
                    .message("Amount is required and must be greater than 0")
                    .rejectedValue(String.valueOf(request.getAmount()))
                    .build());
        } else if (ValidationUtil.exceedsMaxAmount(
                request.getAmount(), 999999.99)) {
            errors.add(ErrorDetail.builder()
                    .field("amount")
                    .message("Amount cannot exceed SEK 999,999.99")
                    .rejectedValue(String.valueOf(request.getAmount()))
                    .build());
        }

        // Validate currency
        if (ValidationUtil.isNullOrEmpty(request.getCurrency())) {
            errors.add(ErrorDetail.builder()
                    .field("currency")
                    .message("Currency is required")
                    .rejectedValue("null")
                    .build());
        }

        // Validate paymentMethod
        if (ValidationUtil.isNullOrEmpty(
                request.getPaymentMethod())) {
            errors.add(ErrorDetail.builder()
                    .field("paymentMethod")
                    .message("Payment method is required")
                    .rejectedValue("null")
                    .build());
        } else if (!ValidationUtil.isValidPaymentMethod(
                request.getPaymentMethod())) {
            errors.add(ErrorDetail.builder()
                    .field("paymentMethod")
                    .message("Payment method must be CARD, SWISH or INVOICE")
                    .rejectedValue(request.getPaymentMethod())
                    .build());
        }

        if (!errors.isEmpty()) {
            log.warn("Payment validation failed with {} errors",
                    errors.size());
            throw new ValidationException(errors);
        }

        log.info("Payment validation passed for order: {}",
                request.getOrderId());
    }
}