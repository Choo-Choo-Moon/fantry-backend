package com.eneifour.fantry.order.controller;

import com.eneifour.fantry.order.domain.OrderStatus;
import com.eneifour.fantry.order.dto.OrderRequest;
import com.eneifour.fantry.order.dto.OrderResponse;
import com.eneifour.fantry.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 관련 API를 제공하는 컨트롤러입니다.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * 주문 목록을 조건에 따라 페이징하여 조회합니다.
     * <p>회원 ID(memberId)나 주문 상태(orderStatus)를 지정하여 필터링할 수 있습니다.
     *
     * @param pageable    페이징 정보 (페이지 번호, 페이지 크기 등).
     * @param memberId    조회할 회원의 ID (선택 사항).
     * @param orderStatus 조회할 주문의 상태 (선택 사항).
     * @return 페이징 처리된 주문 목록.
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) Integer memberId,
            @RequestParam(required = false) OrderStatus orderStatus) {
        log.info("Request to search orders with memberId: {} and orderStatus: {}", memberId, orderStatus);
        Page<OrderResponse> orders = orderService.searchOrders(pageable, memberId, orderStatus);
        return ResponseEntity.ok(orders);
    }

    /**
     * 특정 주문의 상세 정보를 조회합니다.
     *
     * @param ordersId 조회할 주문의 ID.
     * @return 주문 상세 정보.
     */
    @GetMapping("/{ordersId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("ordersId") int ordersId) {
        log.info("Request to get order detail for ordersId: {}", ordersId);
        OrderResponse order = orderService.findOne(ordersId);
        return ResponseEntity.ok(order);
    }

    /**
     * 즉시 구매 상품에 대한 새로운 주문을 생성합니다.
     *
     * @param request 주문 생성에 필요한 데이터.
     * @return 생성된 주문 정보.
     */
    @PostMapping("/instant-buy")
    public ResponseEntity<?> createInstantBuyOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Request to create instant-buy order for auctionId: {}", request.getAuctionId());
        OrderResponse createdOrder = orderService.createInstantBuyOrder(request);
        return ResponseEntity.ok(createdOrder);
    }

    /**
     * 경매에서 낙찰된 주문에 대한 결제를 처리합니다.
     *
     * @param ordersId        결제 처리할 주문의 ID.
     * @param shippingAddress 배송 주소.
     * @param paymentId       결제 ID.
     * @return 작업 성공 메시지.
     */
    @PatchMapping("/{ordersId}/payment")
    public ResponseEntity<String> completeAuctionPayment(
            @PathVariable int ordersId,
            @RequestParam String shippingAddress,
            @RequestParam int paymentId) {
        log.info("Request to complete payment for orderId: {}", ordersId);
        orderService.completeAuctionPayment(shippingAddress, ordersId, paymentId);
        return ResponseEntity.ok("Order ID " + ordersId + " payment has been completed.");
    }

    /**
     * 주문 상태를 '배송 준비중'으로 변경합니다.
     *
     * @param ordersId 처리할 주문의 ID.
     * @return 작업 성공 메시지.
     */
    @PatchMapping("/{ordersId}/status/prepare-shipment")
    public ResponseEntity<String> prepareShipment(@PathVariable int ordersId) {
        log.info("Request to prepare shipment for orderId: {}", ordersId);
        orderService.prepareShipment(ordersId);
        return ResponseEntity.ok("Order ID " + ordersId + " is preparing for shipment.");
    }

    /**
     * 주문 상태를 '배송중'으로 변경합니다.
     *
     * @param ordersId 처리할 주문의 ID.
     * @return 작업 성공 메시지.
     */
    @PatchMapping("/{ordersId}/status/ship")
    public ResponseEntity<String> shipOrder(@PathVariable int ordersId) {
        log.info("Request to ship orderId: {}", ordersId);
        orderService.ship(ordersId);
        return ResponseEntity.ok("Order ID " + ordersId + " is now shipping.");
    }

    /**
     * 주문 상태를 '배송 완료'로 변경합니다.
     *
     * @param ordersId 처리할 주문의 ID.
     * @return 작업 성공 메시지.
     */
    @PatchMapping("/{ordersId}/status/delivered")
    public ResponseEntity<String> markAsDelivered(@PathVariable int ordersId) {
        log.info("Request to mark orderId {} as delivered", ordersId);
        orderService.markAsDelivered(ordersId);
        return ResponseEntity.ok("Order ID " + ordersId + " has been delivered.");
    }

    /**
     * 주문 상태를 '구매 확정'으로 변경합니다.
     *
     * @param ordersId 처리할 주문의 ID.
     * @return 작업 성공 메시지.
     */
    @PatchMapping("/{ordersId}/status/confirmed")
    public ResponseEntity<String> confirmPurchase(@PathVariable int ordersId) {
        log.info("Request to confirm purchase for orderId: {}", ordersId);
        orderService.confirmPurchase(ordersId);
        return ResponseEntity.ok("Order ID " + ordersId + " purchase confirmed.");
    }

    /**
     * 주문에 대한 '취소 요청'을 처리합니다.
     *
     * @param ordersId 처리할 주문의 ID.
     * @return 작업 성공 메시지.
     */
    @PatchMapping("/{ordersId}/status/cancel-requested")
    public ResponseEntity<String> requestCancel(@PathVariable int ordersId) {
        log.info("Request to cancel orderId: {}", ordersId);
        orderService.requestCancel(ordersId);
        return ResponseEntity.ok("Cancellation request for order ID " + ordersId + " has been submitted.");
    }

    /**
     * 주문을 '취소 완료' 상태로 변경합니다.
     *
     * @param ordersId 처리할 주문의 ID.
     * @return 작업 성공 메시지.
     */
    @PatchMapping("/{ordersId}/status/cancelled")
    public ResponseEntity<String> cancelOrder(@PathVariable int ordersId) {
        log.info("Confirm cancellation for orderId: {}", ordersId);
        orderService.cancel(ordersId);
        return ResponseEntity.ok("Order ID " + ordersId + " has been cancelled.");
    }

    /**
     * 주문에 대한 '환불 요청'을 처리합니다.
     *
     * @param ordersId 처리할 주문의 ID.
     * @return 작업 성공 메시지.
     */
    @PatchMapping("/{ordersId}/status/refund-requested")
    public ResponseEntity<String> requestRefund(@PathVariable int ordersId) {
        log.info("Request to refund orderId: {}", ordersId);
        orderService.requestRefund(ordersId);
        return ResponseEntity.ok("Refund request for order ID " + ordersId + " has been submitted.");
    }

    /**
     * 주문을 '환불 완료' 상태로 변경합니다.
     *
     * @param ordersId 처리할 주문의 ID.
     * @return 작업 성공 메시지.
     */
    @PatchMapping("/{ordersId}/status/refunded")
    public ResponseEntity<String> completeRefund(@PathVariable int ordersId) {
        log.info("Confirm refund for orderId: {}", ordersId);
        orderService.completeRefund(ordersId);
        return ResponseEntity.ok("Order ID " + ordersId + " has been refunded.");
    }
}
