package com.smartorder.order.validator;

import com.smartorder.exception.ErrorDetail;
import com.smartorder.exception.ValidationException;
import com.smartorder.order.request.OrderRequest;
import com.smartorder.util.ValidationUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderValidator {

    public void validate(OrderRequest request) {

        List<ErrorDetail> errors = new ArrayList<>();

        // Validate userId
        if (ValidationUtil.isNullOrEmpty(request.getUserId())) {
            errors.add(ErrorDetail.builder()
                    .field("userId")
                    .message("User ID is required")
                    .rejectedValue("null")
                    .build());
        } else if (ValidationUtil.exceedsMaxLength(request.getUserId(), 50)) {
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
        } else if (ValidationUtil.exceedsMaxLength(request.getUserName(), 100)) {
            errors.add(ErrorDetail.builder()
                    .field("userName")
                    .message("User name must not exceed 100 characters")
                    .rejectedValue(request.getUserName())
                    .build());
        }

        // Validate itemCount
        if (ValidationUtil.isNullOrZero(request.getItemCount())) {
            errors.add(ErrorDetail.builder()
                    .field("itemCount")
                    .message("Item count is required and must be at least 1")
                    .rejectedValue(String.valueOf(request.getItemCount()))
                    .build());
        } else if (ValidationUtil.exceedsMaxCount(request.getItemCount(), 1000)) {
            errors.add(ErrorDetail.builder()
                    .field("itemCount")
                    .message("Item count cannot exceed 1000")
                    .rejectedValue(String.valueOf(request.getItemCount()))
                    .build());
        }

        // Validate amount
        if (ValidationUtil.isNullOrZero(request.getAmount())) {
            errors.add(ErrorDetail.builder()
                    .field("amount")
                    .message("Amount is required and must be greater than 0")
                    .rejectedValue(String.valueOf(request.getAmount()))
                    .build());
        } else if (ValidationUtil.exceedsMaxAmount(request.getAmount(), 999999.99)) {
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
        } else if (!ValidationUtil.isValidCurrency(request.getCurrency())) {
            errors.add(ErrorDetail.builder()
                    .field("currency")
                    .message("Currency must be SEK, USD or EUR")
                    .rejectedValue(request.getCurrency())
                    .build());
        }

        // Validate description length if provided
        if (!ValidationUtil.isNullOrEmpty(request.getDescription()) &&
                ValidationUtil.exceedsMaxLength(request.getDescription(), 500)) {
            errors.add(ErrorDetail.builder()
                    .field("description")
                    .message("Description must not exceed 500 characters")
                    .rejectedValue(request.getDescription())
                    .build());
        }

        // Throw if any errors found
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}