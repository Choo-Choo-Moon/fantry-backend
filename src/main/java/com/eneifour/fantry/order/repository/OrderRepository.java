package com.eneifour.fantry.order.repository;

import com.eneifour.fantry.order.domain.Order;
import com.eneifour.fantry.order.domain.OrderStatus;
import com.eneifour.fantry.payment.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Integer> {

    Optional<Order> findOrderDetailByOrderId(@Param("orderId") int orderId);

    Page<Order> findByMember_MemberIdAndOrderStatus(Pageable pageable, int memberId, OrderStatus orderStatus);
    Page<Order> findByMember_MemberId(Pageable pageable, int memberId);
    Page<Order> findByOrderStatus(Pageable pageable, OrderStatus orderStatus);
    Optional<Order> findByPayment(Payment payment);


    Optional<Order> findByAuction_AuctionId(int auctionId);
    List<Order> findByMember_MemberId(int memberId);

    /**
     * 특정 상태와 업데이트 시간을 기준으로 주문을 조회합니다. (정산 대상 조회용)
     * @param orderStatus 조회할 주문 상태
     * @param cutoffDate 기준 시간
     * @return 주문 목록
     */
    List<Order> findByOrderStatusAndUpdatedAtBefore(OrderStatus orderStatus, LocalDateTime cutoffDate);

    /**
     * 특정 상태와 생성 시간 범위를 기준으로 주문을 조회합니다.
     * @param orderStatus 조회할 주문 상태
     * @param startDate 시작 시간
     * @param endDate 종료 시간
     * @return 주문 목록
     */
    List<Order> findByOrderStatusAndCreatedAtBetween(OrderStatus orderStatus, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT new map(" +
            "   count(o) as totalOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.PENDING_PAYMENT then 1 else 0 end), 0L) as pendingPaymentOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.PAID then 1 else 0 end), 0L) as paidOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.PREPARING_SHIPMENT then 1 else 0 end), 0L) as preparingShipmentOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.SHIPPED then 1 else 0 end), 0L) as shippedOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.DELIVERED then 1 else 0 end), 0L) as deliveredOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.CONFIRMED then 1 else 0 end), 0L) as confirmedOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.CANCEL_REQUESTED then 1 else 0 end), 0L) as cancelRequestedOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.CANCELLED then 1 else 0 end), 0L) as cancelledOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.REFUND_REQUESTED then 1 else 0 end), 0L) as refundRequestedOrders, " +
            "   COALESCE(sum(case when o.orderStatus = com.eneifour.fantry.order.domain.OrderStatus.REFUNDED then 1 else 0 end), 0L) as refundedOrders) " +
            "FROM Order o")
    Map<String, Long> countOrderByStatus();

    long countByOrderStatus(OrderStatus orderStatus);

    @Query("SELECT SUM(o.payment.price) FROM Order o WHERE o.orderStatus = :orderStatus")
    BigDecimal sumPriceByOrderStatus(@Param("orderStatus") OrderStatus orderStatus);
}
