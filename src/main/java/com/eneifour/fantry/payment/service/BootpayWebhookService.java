package com.eneifour.fantry.payment.service;

import com.eneifour.fantry.auction.service.AuctionService;
import com.eneifour.fantry.order.service.OrderService;
import com.eneifour.fantry.payment.domain.Payment;
import com.eneifour.fantry.payment.domain.PaymentStatus;
import com.eneifour.fantry.payment.domain.vo.PaymentUpdateData;
import com.eneifour.fantry.payment.exception.NotFoundPaymentException;
import com.eneifour.fantry.payment.infrastructure.bootpay.BootpayReceiptDto;
import com.eneifour.fantry.payment.infrastructure.bootpay.converter.BootpayReceiptConverter;
import com.eneifour.fantry.payment.repository.PaymentRepository;
import com.eneifour.fantry.payment.util.OrderUpdateHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Bootpay Webhook 이벤트를 처리하는 서비스입니다.
 * <p>
 * Bootpay로부터 전송되는 다양한 결제 이벤트(완료, 취소, 실패 등)를 수신하여
 * 로컬 데이터베이스의 결제 정보를 동기화합니다.
 * </p>
 *
 * <p>
 * 주요 처리 이벤트:
 * <ul>
 *   <li>PAYMENT_COMPLETED - 결제 완료</li>
 *   <li>PAYMENT_CANCELLED - 결제 취소</li>
 *   <li>PAYMENT_PARTIAL_CANCELLED - 부분 취소</li>
 *   <li>PAYMENT_CONFIRM_FAILED - 결제 승인 실패</li>
 *   <li>PAYMENT_CANCEL_FAILED - 결제 취소 실패</li>
 *   <li>PAYMENT_REQUEST_FAILED - 결제 요청 실패</li>
 *   <li>PAYMENT_EXPIRED - 결제 만료</li>
 * </ul>
 * </p>
 *
 * @see BootpayReceiptDto
 * @see PaymentStatus
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BootpayWebhookService {
    private final PaymentRepository paymentRepository;
    private final GhostPaymentService ghostPaymentService;
    private final OrderService orderService;
    private final AuctionService auctionService;
    private final OrderUpdateHelper orderUpdateHelper;
    private final BootpayReceiptConverter bootpayReceiptConverter;

    /**
     * 결제 완료 Webhook을 처리합니다.
     *
     * @param paymentUpdateData Bootpay로부터 전달받은 영수증 정보
     */
    @Transactional
    public void onPaymentComplete(PaymentUpdateData paymentUpdateData) {
        log.info("결제 완료 : {}", paymentUpdateData);
        Payment payment = paymentRepository.findByOrderId(paymentUpdateData.getOrderId())
                .orElseThrow(NotFoundPaymentException::new);

        try {
            processPaymentVerification(payment, paymentUpdateData);
            if (payment.getStatus() == PaymentStatus.COMPLETE) {
                orderUpdateHelper.purchase(payment);
            }
        } catch (Exception e) {
            log.error("completeError", e);
        }
    }

    /**
     * 결제 취소 Webhook을 처리합니다.
     * <p>
     * 이미 VOID 상태인 경우 처리하지 않습니다.
     * </p>
     *
     * @param paymentUpdateData Bootpay로부터 전달받은 영수증 정보
     */
    @Transactional
    public void onPaymentCancelled(PaymentUpdateData paymentUpdateData) {
        log.info("결제 취소 : {}", paymentUpdateData);
        Payment payment = paymentRepository.findByOrderId(paymentUpdateData.getOrderId())
                .orElseThrow(NotFoundPaymentException::new);

        if (payment.getStatus() == PaymentStatus.CANCELED) {
            return;
        }
        payment.update(paymentUpdateData);
        if (payment.getStatus() == PaymentStatus.CANCELED) {
            orderUpdateHelper.cancel(paymentUpdateData);
        }
    }

    /**
     * 부분 결제 취소 Webhook을 처리합니다.
     * <p>
     * 이미 CANCELLED 상태인 경우 처리하지 않습니다.
     * </p>
     *
     * @param paymentUpdateData Bootpay로부터 전달받은 영수증 정보
     */
    @Transactional
    public void onPaymentPartialCancelled(PaymentUpdateData paymentUpdateData) {
        log.info("부분 결제 취소 : {}", paymentUpdateData);
        Payment payment = paymentRepository.findByOrderId(paymentUpdateData.getOrderId())
                .orElseThrow(NotFoundPaymentException::new);
        if (payment.getStatus() == PaymentStatus.RETURNED) {
            return;
        }
        payment.update(paymentUpdateData);
        if (payment.getStatus() == PaymentStatus.RETURNED) {
            orderUpdateHelper.refund(paymentUpdateData);
        }
    }

    /**
     * 결제 승인 실패 Webhook을 처리합니다.
     *
     * @param bootpayReceiptDto Bootpay로부터 전달받은 영수증 정보
     */
    public void onPaymentConfirmFailed(BootpayReceiptDto bootpayReceiptDto) {
        log.info("결제 승인 실패 : {}", bootpayReceiptDto);
    }

    /**
     * 결제 취소 실패 Webhook을 처리합니다.
     *
     * @param bootpayReceiptDto Bootpay로부터 전달받은 영수증 정보
     */
    public void onPaymentCancelFailed(BootpayReceiptDto bootpayReceiptDto) {
        log.info("결제 취소 실패 : {}", bootpayReceiptDto);
    }

    /**
     * 결제 요청 실패 Webhook을 처리합니다.
     *
     * @param bootpayReceiptDto Bootpay로부터 전달받은 영수증 정보
     */
    public void onPaymentRequestFailed(BootpayReceiptDto bootpayReceiptDto) {
        log.info("결제 요청 실패 : {}", bootpayReceiptDto);
    }

    /**
     * 결제 만료 Webhook을 처리합니다.
     *
     * @param data Bootpay로부터 전달받은 데이터
     */
    @Transactional
    public void onPaymentExpired(Map<String, Object> data) {
        log.info("결제 만료 : {}", data);
    }

    /**
     * 알 수 없는 Webhook 이벤트를 처리합니다.
     *
     * @param data Bootpay로부터 전달받은 데이터
     */
    public void onError(Map<String, Object> data) {
        log.info("에러 : {}", data);
    }

    /**
     * 결제 검증 및 업데이트 공통 로직
     *
     * @param payment    검증할 결제 엔티티
     * @param updateData Bootpay 영수증 정보
     */
    @Transactional
    public void processPaymentVerification(Payment payment, PaymentUpdateData updateData) {
        if (payment.getStatus() == PaymentStatus.COMPLETE) {
            return;
        }
        payment.update(updateData);
        if (payment.getPaymentId() != null) {
            if (payment.isVerify(updateData)) {
                payment.markComplete();
            } else {
                ghostPaymentService.createGhostPayment(updateData.getReceiptId());
            }
        }

        // 저장
        paymentRepository.save(payment);
    }
}
