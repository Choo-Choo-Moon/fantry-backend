package com.eneifour.fantry.payment.dto;

import com.eneifour.fantry.payment.domain.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponse {
    private String orderId;
}
