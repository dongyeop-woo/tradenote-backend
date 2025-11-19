package com.share.share.web.dto;

import com.share.share.trade.Trade;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TradeDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateTradeRequest {
        private LocalDate date;
        private String stock;
        private String position;
        private String result;
        private Long profit;
        private String chartImage;
        private String profitReason;
        private String lossReason;

        public Trade toEntity() {
            Trade trade = new Trade();
            trade.setDate(date);
            trade.setStock(stock);
            trade.setPosition(position);
            trade.setResult(result);
            trade.setProfit(profit);
            trade.setChartImage(chartImage);
            trade.setProfitReason(profitReason);
            trade.setLossReason(lossReason);
            return trade;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TradeResponse {
        private Long id;
        private LocalDate date;
        private String stock;
        private String position;
        private String result;
        private Long profit;
        private String chartImage;
        private String profitReason;
        private String lossReason;
        private OffsetDateTime createdAt;

        public static TradeResponse from(Trade trade) {
            return TradeResponse.builder()
                    .id(trade.getId())
                    .date(trade.getDate())
                    .stock(trade.getStock())
                    .position(trade.getPosition())
                    .result(trade.getResult())
                    .profit(trade.getProfit())
                    .chartImage(trade.getChartImage())
                    .profitReason(trade.getProfitReason())
                    .lossReason(trade.getLossReason())
                    .createdAt(trade.getCreatedAt())
                    .build();
        }
    }
}


