package com.eneifour.fantry.payment.infrastructure.bootpay;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
public class BootPayDto {
    @JsonAlias("event")
    private String event;
    @JsonAlias("data")
    private BootpayReceiptDto bootpayReceiptDto;
}
