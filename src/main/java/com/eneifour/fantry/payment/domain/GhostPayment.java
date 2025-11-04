package com.eneifour.fantry.payment.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "ghost_payment",indexes = @Index(name = "idx_ghost_payment_status", columnList = "status"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GhostPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ghost_payment_id")
    private Integer ghostPaymentId;
    @Column(name = "receipt_id", updatable = false)
    private String receiptId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GhostPaymentStatus status;

    public static GhostPayment create(String receiptId){
        return GhostPayment.builder()
                .receiptId(receiptId)
                .status(GhostPaymentStatus.CANCEL_RESERVATION)
                .build();
    }

    public void cancelSuccess(){
        this.status = GhostPaymentStatus.CANCEL_SUCCESS;
    }

    public void cancelFailure(){
        this.status = GhostPaymentStatus.CANCEL_FAILED;
    }
}
