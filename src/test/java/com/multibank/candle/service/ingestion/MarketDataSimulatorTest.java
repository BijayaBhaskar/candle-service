package com.multibank.candle.service.ingestion;

import com.multibank.candle.domain.BidAskEvent;
import com.multibank.candle.service.aggregation.CandleAggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class MarketDataSimulatorTest {
    private CandleAggregationService aggregationService;
    private MarketDataSimulator simulator;

    @BeforeEach
    public void setup(){
        aggregationService = Mockito.mock(CandleAggregationService.class);
        simulator = new MarketDataSimulator(aggregationService);
    }

    @Test
    void shouldGenerateAndSendEvents() {

        simulator.generateMarketData();
        verify(aggregationService, atLeastOnce())
                .onEvent(any());
    }

    @Test
    void shouldGenerateEventWithValidPrices() {

        simulator.generateMarketData();

        ArgumentCaptor<BidAskEvent> captor =
                ArgumentCaptor.forClass(BidAskEvent.class);

        verify(aggregationService, atLeastOnce())
                .onEvent(captor.capture());

        var event = captor.getValue();

        assertNotNull(event.symbol());
        assertTrue(event.ask() >= event.bid());
        assertTrue(event.timestamp() > 0);
    }
}
