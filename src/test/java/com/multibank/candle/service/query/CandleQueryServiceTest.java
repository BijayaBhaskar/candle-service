package com.multibank.candle.service.query;

import com.multibank.candle.domain.HistoryResponse;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.entity.CandleEntity;
import com.multibank.candle.repository.CandleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class CandleQueryServiceTest {

    private CandleRepository repository;
    private CandleQueryService queryService;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(CandleRepository.class);
        queryService = new CandleQueryService(repository);
    }

    @Test
    void shouldReturnSortedHistoryWithinRange() {

        CandleEntity candle1 = new CandleEntity();
        candle1.setId(1L);
        candle1.setSymbol("BTC-USD");
        candle1.setInterval(Interval.ONE_SEC);
        candle1.setBucketTime(1000L);
        candle1.setOpen(100);
        candle1.setHigh(110);
        candle1.setLow(90);
        candle1.setClose(105);
        candle1.setVolume(3);

        CandleEntity candle2 = new CandleEntity();
        candle2.setId(2L);
        candle2.setSymbol("BTC-USD");
        candle2.setInterval(Interval.ONE_SEC);
        candle2.setBucketTime(2000L);
        candle2.setOpen(200);
        candle2.setHigh(210);
        candle2.setLow(190);
        candle2.setClose(205);
        candle2.setVolume(5);

        List<CandleEntity> entities = List.of(candle1, candle2);
        when(repository
                .findBySymbolAndIntervalAndBucketTimeBetweenOrderByBucketTimeAsc(
                        "BTC-USD",
                        Interval.ONE_SEC,
                        0L,
                        5000L))
                .thenReturn(entities);

        HistoryResponse response = queryService.getHistory("BTC-USD", Interval.ONE_SEC, 0L, 5000L);
        assertEquals("ok", response.s());
        assertEquals(2, response.t().size());

        // verify sorting
        assertTrue(response.t().get(0) < response.t().get(1));

        // verify mapping correctness
        assertEquals(100, response.o().get(0));
        assertEquals(110, response.h().get(0));
        assertEquals(90, response.l().get(0));
        assertEquals(105, response.c().get(0));
        assertEquals(3, response.v().get(0));
    }

    @Test
    void shouldReturnEmptyResponseWhenDataIsNotAvailable() {

        when(repository
                .findBySymbolAndIntervalAndBucketTimeBetweenOrderByBucketTimeAsc(
                        "BTC-USD",
                        Interval.ONE_SEC,
                        0L,
                        5000L))
                .thenReturn(null);

        HistoryResponse response = queryService.getHistory("BTC-USD", Interval.ONE_SEC, 0L, 5000L);
        assertEquals("ok", response.s());
        assertTrue(response.o().isEmpty());
        assertTrue(response.h().isEmpty());
        assertTrue(response.l().isEmpty());
        assertTrue(response.c().isEmpty());
        assertTrue(response.v().isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFromIsGreaterThanTo() {

        assertThrows(IllegalArgumentException.class,
                () -> queryService.getHistory("BTC-USD", Interval.ONE_SEC, 100L, 50L));

        verifyNoInteractions(repository);
    }
}
