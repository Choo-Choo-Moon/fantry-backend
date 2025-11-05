package com.eneifour.fantry.order.dto;

import com.eneifour.fantry.order.domain.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    // 주문 기본 정보
    private int ordersId;
    private int price;
    private String shippingAddress; // 결제완료되면 update
    private String orderStatus;
    private LocalDateTime orderedAt;
    private LocalDateTime paidAt; // 결제완료되면 update
    private LocalDateTime deliveredAt; // 배송 완료되면 update
    private LocalDateTime cancelledAt; // 주문 취소되면 update

    // 연관된 판매 정보
    private int auctionId;
    private String itemName;
    private String saleType;

    // 연관된 구매자 정보
    private int buyerId;
    private String buyerName;
    private String tel;

    //결제 정보
    private String receiptUrl; //결제 완료되면 update

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .ordersId(order.getOrderId())
                .price(order.getPrice())
                .orderStatus(order.getOrderStatus().toString())
                .orderedAt(order.getCreatedAt())
                .auctionId(order.getAuction().getAuctionId())
                .itemName(order.getAuction().getProductInspection().getItemName())
                .saleType(order.getAuction().getSaleType().toString())
                .buyerId(order.getMember().getMemberId())
                .buyerName(order.getMember().getName())
                .tel(order.getMember().getTel())
                .build();
    }

}
