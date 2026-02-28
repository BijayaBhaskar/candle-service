package com.multibank.candle.service.ingestion;

import com.multibank.candle.domain.BidAskEvent;
import com.multibank.candle.service.aggregation.CandleAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 *
 */
@Component
public class MarketDataSimulator {
    private static final Logger log =
            LoggerFactory.getLogger(MarketDataSimulator.class);

    private final CandleAggregationService aggregationService;
    private final Random random = new Random();

    private final List<String> symbols =
            List.of("BTC-USD", "ETH-USD", "EUR-USD");

    public MarketDataSimulator(CandleAggregationService aggregationService){
        this.aggregationService = aggregationService;
    }

    /**
     * Generate random market data in every 500ms
     */
    @Scheduled(fixedDelay = 500)
    public void generateMarketData(){
        long now = System.currentTimeMillis();

        for(String symbol: symbols){
            double basePrice = getBasePrice(symbol);

            double bid = basePrice + random.nextDouble(-10, 10);
            double ask = bid + random.nextDouble(0.1, 1.0);
            BidAskEvent event =
                    new BidAskEvent(symbol, bid, ask, now);

            aggregationService.onEvent(event);
            log.debug("Generated tick: {}", event);
        }

    }

    private double getBasePrice(String symbol) {
        return switch (symbol){
            case "BTC-USD" -> 30000;
            case "ETH-USD" -> 2000;
            case "EUR-USD" -> 1.1;
            default-> 100;
        };
    }


}
