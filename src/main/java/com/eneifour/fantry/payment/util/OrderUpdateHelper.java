package com.eneifour.fantry.payment.util;

import com.eneifour.fantry.auction.domain.Auction;
import com.eneifour.fantry.auction.domain.SaleStatus;
import com.eneifour.fantry.auction.dto.AuctionDetailResponse;
import com.eneifour.fantry.auction.repository.AuctionRepository;
import com.eneifour.fantry.auction.service.AuctionService;
import com.eneifour.fantry.order.domain.Order;
import com.eneifour.fantry.order.dto.OrderRequest;
import com.eneifour.fantry.order.service.OrderService;
import com.eneifour.fantry.payment.domain.Payment;
import com.eneifour.fantry.payment.domain.vo.PaymentUpdateData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderUpdateHelper {
    private final OrderService orderService;
    private final AuctionService auctionService;
    private final AuctionRepository auctionRepository;

    @Transactional
    public void purchase(Payment payment) {
        Map<String, Object> metaData = payment.getMetadata();
        if (metaData.get("auctionInfo") == null) {
            return;
        }
        if (metaData.get("deliveryInfo") == null) {
            return;

        }
        if (metaData.get("userInfo") == null) {
            return;

        }
        Map<String, Object> auctionInfo = (Map<String, Object>) metaData.get("auctionInfo");
        Map<String, Object> shippingInfo = (Map<String, Object>) metaData.get("deliveryInfo");
        Map<String, Object> userInfo = (Map<String, Object>) metaData.get("userInfo");
        if (auctionInfo.get("auctionId") == null) {
            return;
        }
        AuctionDetailResponse auction = auctionService.findByAuctionId((Integer) auctionInfo.get("auctionId"));
        String shippingAddress = shippingInfo.get("address") + " " + shippingInfo.get("detailAddress");
        if (auction.getSaleType().toLowerCase(Locale.ROOT).equals("auction")) {
            Order order = orderService.findByAuctionId(auction.getAuctionId());

            log.info("ordersResponse : {}", order);
            orderService.completeAuctionPayment(shippingAddress, order.getOrderId(), payment.getPaymentId());
        } else {
            Order order = null;
            try {
                order = orderService.findByAuctionId(auction.getAuctionId());
            } catch (Exception ignore) {
            }
            if (order != null) {
                return;
            }
            Optional<Auction> response = auctionRepository.findByIdForUpdate(auction.getAuctionId());
            response.ifPresent(value ->
            {
                if (value.getSaleStatus() == SaleStatus.SOLD) {
                    return;
                }

                if (value.getSaleStatus() == SaleStatus.ACTIVE || value.getSaleStatus() == SaleStatus.REACTIVE) {
                    value.closeAsSold((int) auctionInfo.get("itemPrice"));
                }
            });
            OrderRequest orderRequest = new OrderRequest(auction.getAuctionId(), (int) userInfo.get("memberId"), (int) auctionInfo.get("itemPrice"), payment.getPaymentId(), shippingAddress);
            orderService.createInstantBuyOrder(orderRequest);
        }
    }

    @Transactional
    public void refund(PaymentUpdateData paymentUpdateData) {
        Map<String, Object> metaData = paymentUpdateData.getMetadata();
        if (metaData == null) {
            return;
        }
        if (metaData.get("auctionInfo") == null) {
            return;
        }
        Map<String, Object> auctionInfo = (Map<String, Object>) metaData.get("auctionInfo");
        Order order = orderService.findByAuctionId((int) auctionInfo.get("auctionId"));
        orderService.completeRefund(order.getOrderId());
    }

    @Transactional
    public void cancel(PaymentUpdateData paymentUpdateData) {
        try {
            Map<String, Object> metaData = paymentUpdateData.getMetadata();
            if (metaData == null) {
                return;
            }
            if (metaData.get("auctionInfo") == null) {
                return;
            }
            Map<String, Object> auctionInfo = (Map<String, Object>) metaData.get("auctionInfo");
            Order order = orderService.findByAuctionId((int) auctionInfo.get("auctionId"));
            orderService.cancel(order.getOrderId());
        } catch (Exception e) {
            log.error("주문 취소 에러 : {}", e.getMessage());
        }
    }
}
