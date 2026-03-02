package com.multibank.candle.service.aggregation;

import com.multibank.candle.domain.BidAskEvent;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.entity.CandleEntity;
import com.multibank.candle.repository.CandleRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * Service responsible for aggregating incoming {@link BidAskEvent}
 * into OHLC {@link com.multibank.candle.entity.CandleEntity} structures and store into candle table
 *
 * @author Bijaya Bhaskar Swain
 */
@Service
@Transactional
public class CandleAggregationService {

    private final CandleRepository candleRepository;

    public CandleAggregationService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    /**
     * Processes an incoming market data event and updates candle table
     * corresponding OHLC candles across all supported intervals.
     *
     * @param event the incoming bid/ask market event
     */
    public void onEvent(BidAskEvent event) {
        // calculate price
        double price = (event.bid() + event.ask()) / 2;

        // Update for all the intervals
        for (Interval interval : Interval.values()) {
            processInterval(event, interval, price);
        }
    }

    private void processInterval(BidAskEvent event, Interval interval, double price) {

        long bucket = getBucketStart(event.timestamp(), interval);

        int updated = candleRepository.updateCandle(event.symbol(), interval, bucket, price);

        if (updated == 0) {
            insertNewCandle(event.symbol(), interval, bucket, price);
        }
    }

    private void insertNewCandle(String symbol, Interval interval, long bucket, double price) {
        try {
            CandleEntity entity = buildNewCandle(symbol, interval, bucket, price);
            candleRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            // Another thread inserted concurrently
            // Retry update to avoid race condition
            candleRepository.updateCandle(symbol, interval, bucket, price);
        }
    }

    private CandleEntity buildNewCandle(String symbol, Interval interval, long bucket, double price) {

        CandleEntity entity = new CandleEntity();
        entity.setSymbol(symbol);
        entity.setInterval(interval);
        entity.setBucketTime(bucket);
        entity.setOpen(price);
        entity.setHigh(price);
        entity.setLow(price);
        entity.setClose(price);
        entity.setVolume(1);

        return entity;

    }

    /**
     * Calculates the start time of the bucket for a given timestamp
     * and interval.
     *
     * @param epochMillis the event timestamp in milliseconds
     * @param interval    the aggregation interval
     * @return the bucket start time in UNIX seconds
     */
    private long getBucketStart(long epochMillis, Interval interval) {
        long second = epochMillis / 1000;
        return (second / interval.getSeconds()) * interval.getSeconds();
    }


}
