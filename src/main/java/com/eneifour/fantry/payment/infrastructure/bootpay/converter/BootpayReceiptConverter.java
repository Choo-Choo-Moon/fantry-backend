package com.eneifour.fantry.payment.infrastructure.bootpay.converter;

import com.eneifour.fantry.payment.domain.BootPayStatus;
import com.eneifour.fantry.payment.domain.vo.PaymentUpdateData;
import com.eneifour.fantry.payment.infrastructure.bootpay.BankDataDto;
import com.eneifour.fantry.payment.infrastructure.bootpay.BootpayReceiptDto;
import com.eneifour.fantry.payment.infrastructure.bootpay.CardDataDto;
import com.eneifour.fantry.payment.infrastructure.bootpay.VirtualBankDataDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BootpayReceiptConverter {
    private final ObjectMapper objectMapper;

    public PaymentUpdateData toPaymentUpdateData(BootpayReceiptDto bootpayReceiptDto) {
        PaymentUpdateData.PaymentUpdateDataBuilder builder = PaymentUpdateData.builder()
                .receiptId(bootpayReceiptDto.getReceiptId())
                .cancelledPrice(bootpayReceiptDto.getCancelledPrice())
                .orderId(bootpayReceiptDto.getOrderId())
                .orderName(bootpayReceiptDto.getOrderName())
                .metadata(bootpayReceiptDto.getMetadata())
                .pg(bootpayReceiptDto.getPg())
                .method(bootpayReceiptDto.getMethod())
                .currency(bootpayReceiptDto.getCurrency())
                .requestedAt(bootpayReceiptDto.getRequestedAt().toLocalDateTime())
                .purchasedAt(bootpayReceiptDto.getPurchasedAt().toLocalDateTime())
                .receiptUrl(bootpayReceiptDto.getReceiptUrl())
                .bootPayStatus(BootPayStatus.fromCode(bootpayReceiptDto.getStatus()));
        if (bootpayReceiptDto.getCancelledAt() != null) {
            builder.cancelledAt(bootpayReceiptDto.getCancelledAt().toLocalDateTime());
        }
        Object paymentData = getObject(bootpayReceiptDto);
        Map<String, Object> paymentInfo = convertPaymentInfo(paymentData);
        builder.paymentInfo(paymentInfo);

        return builder.build();
    }

    private Map<String, Object> convertPaymentInfo(Object paymentData) {
        Map<String, Object> paymentInfo = null;
        if (paymentData != null) {
            paymentInfo = objectMapper.convertValue(paymentData, new TypeReference<>() {
            });
        }

        return paymentInfo;
    }

    private Object getObject(BootpayReceiptDto bootpayReceiptDto) {
        Object paymentData = null;
        CardDataDto cardDataDto = bootpayReceiptDto.getCardDataDto();
        BankDataDto bankDataDto = bootpayReceiptDto.getBankDataDto();
        VirtualBankDataDto virtualBankDataDto = bootpayReceiptDto.getVirtualBankDataDto();
        if (cardDataDto != null) {
            paymentData = cardDataDto;
        } else if (bankDataDto != null) {
            paymentData = bankDataDto;
        } else if (virtualBankDataDto != null) {
            paymentData = virtualBankDataDto;
        }
        return paymentData;
    }
}
