package com.eneifour.fantry.payment.dto;

import com.eneifour.fantry.payment.domain.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCreateResponse {
    private String orderId;

    public static PaymentCreateResponse from(Payment payment) {
        return new PaymentCreateResponse(payment.getOrderId());
    }
}
