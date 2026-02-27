package com.multibank.candle.service.aggregation;

import com.multibank.candle.domain.BidAskEvent;
import com.multibank.candle.domain.Candle;
import com.multibank.candle.domain.Interval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CandleAggregationServiceTest {

    private CandleAggregationService service;

    @BeforeEach
    void setUp() {
        service = new CandleAggregationService();
    }

    @Test
    public void shouldCreateCandleOnFirstEvent() {

        long timestamp = System.currentTimeMillis();

        BidAskEvent event = new BidAskEvent("BTC-USD", 100, 102, timestamp);
        service.onEvent(event);
        ConcurrentMap<Long, CandleBuilder> candles =
                service.getCandles("BTC-USD", Interval.ONE_SEC);
        assertFalse(candles.isEmpty());
    }

    @Test
    void shouldUpdateHighLowCloseCorrectly() {

        long timestamp = System.currentTimeMillis();

        service.onEvent(new BidAskEvent("BTC-USD", 100, 100, timestamp));
        service.onEvent(new BidAskEvent("BTC-USD", 110, 110, timestamp));
        service.onEvent(new BidAskEvent("BTC-USD", 90, 90, timestamp));

        ConcurrentMap<Long, CandleBuilder> candlesMap = service.getCandles("BTC-USD", Interval.ONE_SEC);

        assertEquals(1, candlesMap.size());
        CandleBuilder candleBuilder = candlesMap.values().iterator().next();

        Candle candle = candleBuilder.build();
        assertEquals(100, candle.open());
        assertEquals(110, candle.high());
        assertEquals(90, candle.low());
        assertEquals(90, candle.close());
        assertEquals(3, candle.volume());
    }

    @Test
    void shouldCreateCandlesForMultipleIntervals() {

        long timestamp = System.currentTimeMillis();

        service.onEvent(new BidAskEvent("BTC-USD", 100, 100, timestamp));

        var oneSec = service.getCandles("BTC-USD", Interval.ONE_SEC);

        var oneMin = service.getCandles("BTC-USD", Interval.ONE_MIN
        );

        assertFalse(oneSec.isEmpty());
        assertFalse(oneMin.isEmpty());
    }

    @Test
    void shouldHandleConcurrentUpdates() throws InterruptedException {
        long timestamp = System.currentTimeMillis();
        int threads = 10;
        int eventsPerThread = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    service.onEvent(new BidAskEvent("BTC-USD", 100, 100, timestamp));
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        var candles = service.getCandles("BTC-USD", Interval.ONE_SEC
        );
        assertEquals(1, candles.size());
        var candle = candles.values().iterator().next().build();
        assertEquals(threads * eventsPerThread, candle.volume());
    }

}
