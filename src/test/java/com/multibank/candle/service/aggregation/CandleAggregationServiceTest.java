package com.multibank.candle.service.aggregation;

import com.multibank.candle.domain.BidAskEvent;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.entity.CandleEntity;
import com.multibank.candle.repository.CandleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CandleAggregationServiceTest {

    private CandleAggregationService service;
    private CandleRepository repository;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CandleRepository.class);
        service = new CandleAggregationService(repository);
    }

    @Test
    void shouldOnlyUpdateWhenCandleExists() {

        when(repository.updateCandle(anyString(), any(), anyLong(), anyDouble()))
                .thenReturn(1);

        BidAskEvent event =
                new BidAskEvent("BTC-USD", 100, 102, System.currentTimeMillis());

        service.onEvent(event);

        verify(repository, times(Interval.values().length))
                .updateCandle(anyString(), any(), anyLong(), anyDouble());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldInsertWhenUpdateReturnsZero() {

        when(repository.updateCandle(anyString(), any(), anyLong(), anyDouble()))
                .thenReturn(0);

        BidAskEvent event =
                new BidAskEvent("BTC-USD", 100, 100, System.currentTimeMillis());

        service.onEvent(event);

        verify(repository, times(Interval.values().length))
                .updateCandle(anyString(), any(), anyLong(), anyDouble());
        verify(repository, times(Interval.values().length))
                .save(any(CandleEntity.class));
    }
    @Test
    void shouldRetryUpdateWhenInsertFailsWithDataIntegrityViolation() {

        when(repository.updateCandle(anyString(), any(), anyLong(), anyDouble()))
                .thenReturn(0); // force insert path

        when(repository.save(any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate"));

        BidAskEvent event =
                new BidAskEvent("BTC-USD", 100, 100, System.currentTimeMillis());

        service.onEvent(event);

        // first update
        verify(repository, atLeast(Interval.values().length))
                .updateCandle(anyString(), any(), anyLong(), anyDouble());
    }
    @Test
    void shouldNotCallRepositoryWhenEventInvalid() {

        BidAskEvent invalidEvent =
                new BidAskEvent("", 100, 100, System.currentTimeMillis());

        service.onEvent(invalidEvent);
        verifyNoInteractions(repository);
    }
    @Test
    void shouldRethrowUnexpectedException() {

        when(repository.updateCandle(anyString(), any(), anyLong(), anyDouble()))
                .thenThrow(new RuntimeException("DB failure"));

        BidAskEvent event =
                new BidAskEvent("BTC-USD", 100, 100, System.currentTimeMillis());

        assertThrows(RuntimeException.class,
                () -> service.onEvent(event));
    }
    @Test
    void shouldHandleNullEventGracefully() {

        service.onEvent(null);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldHandleNullSymbolEventGracefully() {

        BidAskEvent event = new BidAskEvent(null, 100, 100, System.currentTimeMillis());

        service.onEvent(event);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldHandleNegativeBidEventGracefully() {

        BidAskEvent event = new BidAskEvent("BTC-USD", -1, 100, System.currentTimeMillis());

        service.onEvent(event);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldHandleZeroAskEventGracefully() {

        BidAskEvent event = new BidAskEvent("BTC-USD", 100, 0, System.currentTimeMillis());

        service.onEvent(event);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldHandleZeroBidEventGracefully() {

        BidAskEvent event = new BidAskEvent("BTC-USD", 0, 100, System.currentTimeMillis());

        service.onEvent(event);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldHandleNegativeAskEventGracefully() {

        BidAskEvent event = new BidAskEvent("BTC-USD", 100, -1, System.currentTimeMillis());

        service.onEvent(event);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldHandleWhenAskLessThanBidEventGracefully() {

        BidAskEvent event = new BidAskEvent("BTC-USD", 100, 90, System.currentTimeMillis());

        service.onEvent(event);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldHandleWhenTimestampIsNegative() {

        BidAskEvent event = new BidAskEvent("BTC-USD", 100, 200, -100L);

        service.onEvent(event);
        verifyNoInteractions(repository);
    }
}
