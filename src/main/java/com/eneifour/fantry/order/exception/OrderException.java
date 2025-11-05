package com.eneifour.fantry.order.exception;

import com.eneifour.fantry.auction.exception.BusinessException;
import com.eneifour.fantry.auction.exception.ErrorCode;

public class OrderException extends BusinessException {

    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
