package com.multibank.candle.service.aggregation;

import com.multibank.candle.domain.BidAskEvent;
import com.multibank.candle.domain.Interval;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * Service responsible for aggregating incoming {@link BidAskEvent}
 * into OHLC {@link com.multibank.candle.domain.Candle} structures.
 *
 * @author Bijaya Bhaskar Swain
 */
@Service
public class CandleAggregationService {

    /**
     * In-memory storage for candle
     * Symbol -> Interval -> Time -> CandleBuilder
     */
    private final ConcurrentMap<String,
            ConcurrentMap<Interval, ConcurrentMap<Long, CandleBuilder>>>
            store = new ConcurrentHashMap<>();

    /**
     * Processes an incoming market data event and updates
     * corresponding OHLC candles across all supported intervals.
     *
     * @param event the incoming bid/ask market event
     */
    public void onEvent(BidAskEvent event){
        // calculate price
        double price = (event.bid() + event.ask()) / 2;

        // update all Intervals
        for(Interval interval: Interval.values()){

            long bucketStart = getBucketStart(event.timestamp(), interval);

            // Get or Create intervalMap and candleMap
            ConcurrentMap<Interval, ConcurrentMap<Long, CandleBuilder>> intervalMap =
                    store.computeIfAbsent(event.symbol(), s -> new ConcurrentHashMap<>());

            ConcurrentMap<Long, CandleBuilder> candleMap =
                    intervalMap.computeIfAbsent(interval, i -> new ConcurrentHashMap<>());

            // Create or Update candle
            candleMap.compute(bucketStart, (time , existingCandle) ->{
                if(existingCandle == null){
                    return new CandleBuilder(bucketStart, price);
                }else {
                    existingCandle.update(price);
                    return existingCandle;
                }
            });

        }
    }

    /**
     * Calculates the start time of the bucket for a given timestamp
     * and interval.
     *
     * @param epochMillis the event timestamp in milliseconds
     * @param interval the aggregation interval
     * @return the bucket start time in UNIX seconds
     */
    private long getBucketStart(long epochMillis, Interval interval){
        long second = epochMillis / 1000;
        return (second/interval.getSeconds()) * interval.getSeconds();
    }

    /**
     * Returns all candles for a given symbol and interval.
     * If no candles exist, an empty map is returned.
     *
     * @param symbol the trading symbol (e.g., BTC-USD)
     * @param interval the time interval
     * @return map of bucketStartTime to CandleBuilder
     */
    public ConcurrentMap<Long, CandleBuilder> getCandles(String symbol, Interval interval){
        return store
                .getOrDefault(symbol, new ConcurrentHashMap<>())
                .getOrDefault(interval, new ConcurrentHashMap<>());
    }


}
