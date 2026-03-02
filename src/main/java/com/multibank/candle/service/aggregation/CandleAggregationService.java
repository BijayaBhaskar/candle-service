package com.multibank.candle.service.aggregation;

import com.multibank.candle.domain.BidAskEvent;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.entity.CandleEntity;
import com.multibank.candle.repository.CandleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final Logger log =
            LoggerFactory.getLogger(CandleAggregationService.class);

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

        try{
            validateEvent(event);
            // calculate price
            double price = (event.bid() + event.ask()) / 2;

            // Update for all the intervals
            for (Interval interval : Interval.values()) {
                processInterval(event, interval, price);

                log.debug("Processed event for symbol={}, timestamp={}",
                        event.symbol(), event.timestamp());
            }
        }catch (IllegalArgumentException ex) {
            log.warn("Invalid event received: {}", ex.getMessage());
        } catch (Exception ex){
            log.error("Failed to process event for symbol={}",
                    event.symbol(), ex);
            throw ex;
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

    /**
     * Method to validate each field on bid event
     * @param event incoming bid event
     */
    private void validateEvent(BidAskEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("BidAskEvent must not be null");
        }
        if (event.symbol() == null || event.symbol().isBlank()) {
            throw new IllegalArgumentException("Symbol must not be null or blank");
        }
        if (event.bid() <= 0) {
            throw new IllegalArgumentException("Bid price must be greater than zero");
        }
        if (event.ask() <= 0) {
            throw new IllegalArgumentException("Ask price must be greater than zero");
        }
        if (event.ask() < event.bid()) {
            throw new IllegalArgumentException("Ask price cannot be lower than bid price");
        }
        if (event.timestamp() <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }


}
