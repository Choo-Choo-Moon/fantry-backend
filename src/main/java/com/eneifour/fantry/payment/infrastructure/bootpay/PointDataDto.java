package com.eneifour.fantry.payment.infrastructure.bootpay;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PointDataDto {
    @JsonAlias("tid")
    protected String tid;
}
