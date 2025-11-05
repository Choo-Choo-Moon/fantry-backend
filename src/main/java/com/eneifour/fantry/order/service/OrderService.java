package com.eneifour.fantry.order.service;

import com.eneifour.fantry.auction.domain.Auction;
import com.eneifour.fantry.auction.exception.AuctionException;
import com.eneifour.fantry.auction.exception.ErrorCode;
import com.eneifour.fantry.auction.repository.AuctionRepository;
import com.eneifour.fantry.member.domain.Member;
import com.eneifour.fantry.member.repository.MemberRepository;
import com.eneifour.fantry.order.domain.Order;
import com.eneifour.fantry.order.domain.OrderStatus;
import com.eneifour.fantry.order.dto.OrderRequest;
import com.eneifour.fantry.order.dto.OrderResponse;
import com.eneifour.fantry.order.exception.OrderException;
import com.eneifour.fantry.order.repository.OrderRepository;
import com.eneifour.fantry.payment.domain.Payment;
import com.eneifour.fantry.payment.domain.PaymentErrorCode;
import com.eneifour.fantry.payment.exception.PaymentException;
import com.eneifour.fantry.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;


    // =============================================
    // 1. 주문 조회 (Read)
    // =============================================

    //전체 Orders List 조회
    // paging 처리
    // 파라미터에 status 및 member 여부에 따라 해당 Record 조회 , 없다면 전체 조회
    @Transactional
    public Page<OrderResponse> searchOrders(Pageable pageable, Integer memberId, OrderStatus orderStatus) {

        Page<Order> orderList;

        if (memberId != null && orderStatus != null) {
            orderList = orderRepository.findByMember_MemberIdAndOrderStatus(pageable, memberId, orderStatus);
        } else if (orderStatus != null) {
            orderList = orderRepository.findByOrderStatus(pageable, orderStatus);
        } else if (memberId != null) {
            orderList = orderRepository.findByMember_MemberId(pageable, memberId);
        } else {
            orderList = orderRepository.findAll(pageable);
        }

        return orderList.map(order -> {
            OrderResponse orderResponse = OrderResponse.from(order);
            if (order.getShippingAddress() != null) {
                orderResponse.setShippingAddress(order.getShippingAddress());
            }

            if (order.getDeliveredAt() != null) {
                orderResponse.setDeliveredAt(order.getDeliveredAt());
            }

            if (order.getCancelledAt() != null) {
                orderResponse.setCancelledAt(order.getCancelledAt());
            }

            if (order.getPayment() != null) {
                orderResponse.setPaidAt(order.getPayment().getPurchasedAt());
            }

            return orderResponse;
        });
    }

    //주문 단건 조회
    @Transactional
    public OrderResponse findOne(int ordersId) {

        Order order = orderRepository.findOrderDetailByOrderId(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));

        OrderResponse orderResponse = OrderResponse.from(order);

        if (order.getShippingAddress() != null) {
            orderResponse.setShippingAddress(order.getShippingAddress());
        }

        if (order.getDeliveredAt() != null) {
            orderResponse.setDeliveredAt(order.getDeliveredAt());
        }

        if (order.getCancelledAt() != null) {
            orderResponse.setCancelledAt(order.getCancelledAt());
        }

        if (order.getPayment() != null) {
            orderResponse.setPaidAt(order.getPayment().getPurchasedAt());
        }


        return orderResponse;
    }

    //auction_id 와 일치하는 주문 단건 조회
    public Order findByAuctionId(int auctionId) {
        Order order = orderRepository.findByAuction_AuctionId(auctionId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));

        return order;
    }

    // =============================================
    // 2. 주문 생성/삭제 (Write)
    // =============================================

    //일반 판매 상품 1건 구매 및 결제 완료
    @Transactional
    public OrderResponse createInstantBuyOrder(OrderRequest orderRequest) {
        Auction auction = auctionRepository.findById(orderRequest.getAuctionId())
                .orElseThrow(() -> new AuctionException(ErrorCode.AUCTION_NOT_FOUND));
        Member buyer = memberRepository.findById(orderRequest.getBuyerId())
                .orElseThrow(() -> new OrderException(ErrorCode.MEMBER_NOT_FOUND));
        Payment payment = paymentRepository.findByPaymentId(orderRequest.getPaymentId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        //DTO와 조회된 엔티티를 바탕으로 Orders 엔티티 생성

        Order.OrderBuilder builder = Order.builder()
                .auction(auction)
                .member(buyer)
                .price(orderRequest.getPrice())
                .payment(payment)
                .orderStatus(OrderStatus.PAID);
        if (orderRequest.getShippingAddress() != null) {
            builder.shippingAddress(orderRequest.getShippingAddress());
        }
        Order order = builder.build();
        Order savedOrder = orderRepository.save(order);

        return findOne(savedOrder.getOrderId());
    }

    // =============================================
    // 3. 주문 상태 변경 (Update)
    // =============================================

    //낙찰된 주문건 결제 완료 처리
    @Transactional
    public void completeAuctionPayment(String shippingAddress, int ordersId, int paymentId) {
        Order order = orderRepository.findById(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        order.completePayment(shippingAddress, payment);
    }

    //배송 준비중 처리
    @Transactional
    public void prepareShipment(int ordersId) {
        Order order = orderRepository.findById(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.prepareShipment();
    }

    //배송중 처리
    @Transactional
    public void ship(int ordersId) {
        Order order = orderRepository.findById(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.ship();
    }

    //배송 완료 처리
    @Transactional
    public void markAsDelivered(int ordersId) {
        Order order = orderRepository.findById(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.markAsDelivered();
    }

    //구매 확정 처리
    @Transactional
    public void confirmPurchase(int ordersId) {
        Order order = orderRepository.findById(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.confirmPurchase();
    }

    //취소 요청 처리
    @Transactional
    public void requestCancel(int ordersId) {
        Order order = orderRepository.findById(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.requestCancel();
    }

    //취소 완료 처리
    @Transactional
    public void cancel(int ordersId) {
        Order order = orderRepository.findById(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.cancel();
    }

    //환불 요청 처리
    @Transactional
    public void requestRefund(int ordersId) {
        Order order = orderRepository.findById(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.requestRefund();
    }

    //환불 완료 처리
    @Transactional
    public void completeRefund(int ordersId) {
        Order order = orderRepository.findById(ordersId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.completeRefund();
    }

}
