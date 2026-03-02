package com.multibank.candle.service.aggregation;

import com.multibank.candle.domain.BidAskEvent;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.entity.CandleEntity;
import com.multibank.candle.repository.CandleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CandleAggregationServiceTest {

    private CandleAggregationService service;
    private CandleRepository repository;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CandleRepository.class);
        service = new CandleAggregationService(repository);
    }

    @Test
    public void shouldCreateCandleOnFirstEvent() {

        long timestamp = System.currentTimeMillis();

        // simulate update success
        when(repository.updateCandle(anyString(), any(), anyLong(), anyDouble())).thenReturn(1);

        BidAskEvent event = new BidAskEvent("BTC-USD", 100, 102, timestamp);
        service.onEvent(event);
        verify(repository, times(Interval.values().length))
                .updateCandle(anyString(), any(), anyLong(), anyDouble());

        verify(repository, never()).save(any());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldInsertNewCandleWhenUpdateReturnsZero() {

        // simulate no existing candle
        when(repository.updateCandle(anyString(), any(), anyLong(), anyDouble())).thenReturn(0);

        BidAskEvent event =
                new BidAskEvent("BTC-USD", 100, 100, System.currentTimeMillis());

        service.onEvent(event);
        verify(repository, times(Interval.values().length))
                .updateCandle(anyString(), any(), anyLong(), anyDouble());
        verify(repository, times(Interval.values().length)).save(any(CandleEntity.class));
        verifyNoMoreInteractions(repository);
    }

}
