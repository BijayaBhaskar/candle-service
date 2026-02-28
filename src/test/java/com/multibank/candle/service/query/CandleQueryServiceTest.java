package com.multibank.candle.service.query;

import com.multibank.candle.domain.HistoryResponse;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.service.aggregation.CandleAggregationService;
import com.multibank.candle.service.aggregation.CandleBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class CandleQueryServiceTest {

    private CandleAggregationService aggregationService;
    private CandleQueryService queryService;

    @BeforeEach
    void setup() {
        aggregationService = Mockito.mock(CandleAggregationService.class);
        queryService = new CandleQueryService(aggregationService);
    }

    @Test
    void shouldReturnSortedHistoryWithinRange() {

        ConcurrentHashMap<Long, CandleBuilder> map =
                new ConcurrentHashMap<>();
        map.put(2000L, new CandleBuilder(2000L, 100));
        map.put(1000L, new CandleBuilder(1000L, 200));

        when(aggregationService.getCandles("BTC-USD", Interval.ONE_SEC))
                .thenReturn(map);

        HistoryResponse response =
                queryService.getHistory("BTC-USD", Interval.ONE_SEC, 0, 5000);

        assertEquals("ok", response.s());
        assertEquals(2, response.t().size());
        // check sort
        assertTrue(response.t().get(0) < response.t().get(1));
    }
}
