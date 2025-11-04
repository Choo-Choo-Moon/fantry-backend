package com.eneifour.fantry.payment.domain.vo;

import com.eneifour.fantry.payment.domain.BootPayStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
public class PaymentUpdateData {
    private final String receiptId;
    private final Integer cancelledPrice;
    private final String orderId;
    private final String orderName;
    private final Map<String, Object> metadata;
    private final String pg;
    private final String method;
    private final String currency;
    private final LocalDateTime requestedAt;
    private final LocalDateTime purchasedAt;
    private final LocalDateTime cancelledAt;
    private final String receiptUrl;
    private final BootPayStatus bootPayStatus;
    private final Map<String, Object> paymentInfo;
}
