package com.eneifour.fantry.payment.domain;

import com.eneifour.fantry.common.domain.BaseAuditingEntity;
import com.eneifour.fantry.payment.domain.vo.PaymentUpdateData;
import com.eneifour.fantry.payment.exception.CreatePaymentFailedException;
import com.eneifour.fantry.payment.exception.PaymentAmountMismatchException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "payment")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Payment extends BaseAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;
    @Column(name = "receipt_id", unique = true)
    private String receiptId;
    @Column(name = "order_id", unique = true)
    private String orderId;
    @Column(name = "price")
    private Integer price;
    @Column(name = "cancelled_price")
    private Integer cancelledPrice;
    @Column(name = "order_name")
    private String orderName;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;
    @Column(name = "pg")
    private String pg;
    @Column(name = "method")
    private String method;
    @Column(name = "currency")
    private String currency;
    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;
    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    @Column(name = "receipt_url")
    private String receiptUrl;
    @Column(name = "bootpay_status")
    @Enumerated(value = EnumType.STRING)
    private BootPayStatus bootpayStatus = BootPayStatus.PAYMENT_WAITING;
    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    PaymentStatus status = PaymentStatus.VERIFYING;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_info", columnDefinition = "json")
    private Map<String, Object> paymentInfo;
    @Version
    @Column(name = "version")
    Long version = 0L;

    public void validateAmount(Integer price) {
        if (this.price - (int) price != 0) {
            throw new PaymentAmountMismatchException();
        }
    }

    public static Payment create(String orderId, Integer price) throws CreatePaymentFailedException {
        return Payment.builder()
                .orderId(orderId)
                .price(price)
                .status(PaymentStatus.VERIFYING)
                .build();
    }

    public void update(PaymentUpdateData paymentUpdateData) {
        this.receiptId = paymentUpdateData.getReceiptId();
        this.cancelledPrice = paymentUpdateData.getCancelledPrice();
        this.orderName = paymentUpdateData.getOrderName();
        this.metadata = paymentUpdateData.getMetadata();
        this.pg = paymentUpdateData.getPg();
        this.method = paymentUpdateData.getMethod();
        this.currency = paymentUpdateData.getCurrency();
        this.requestedAt = paymentUpdateData.getRequestedAt();
        this.purchasedAt = paymentUpdateData.getPurchasedAt();
        this.cancelledAt = paymentUpdateData.getCancelledAt();
        this.receiptUrl = paymentUpdateData.getReceiptUrl();
        this.bootpayStatus = paymentUpdateData.getBootPayStatus();
        this.paymentInfo = paymentUpdateData.getPaymentInfo();

        if (this.bootpayStatus == BootPayStatus.PAYMENT_COMPLETED && this.cancelledPrice > 0) {
            this.status = PaymentStatus.RETURNED;
        } else if (this.bootpayStatus == BootPayStatus.PAYMENT_CANCELLED) {
            this.status = PaymentStatus.CANCELED;
        }
    }

    public boolean isVerify(PaymentUpdateData paymentUpdateData) {
        return this.receiptId.equals(paymentUpdateData.getReceiptId())
                && this.orderId.equals(paymentUpdateData.getOrderId())
                && this.orderName.equals(paymentUpdateData.getOrderName());
    }

    public void markComplete() {
        this.status = PaymentStatus.COMPLETE;
    }
}