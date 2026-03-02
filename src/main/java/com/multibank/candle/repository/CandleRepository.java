package com.multibank.candle.repository;

import com.multibank.candle.domain.Interval;
import com.multibank.candle.entity.CandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for CandleEntity persistence operations.
 *
 * @author Bijaya Bhaskar Swain
 */
@Repository
public interface CandleRepository extends JpaRepository<CandleEntity, Long> {

    /**
     * Fetch historical candles ordered by bucket time ascending.
     * Used by History API.
     */
    List<CandleEntity> findBySymbolAndIntervalAndBucketTimeBetweenOrderByBucketTimeAsc(
            String symbol,
            Interval interval,
            Long from,
            Long to
    );


    /**
     * Atomic upsert operation.
     * Inserts a new candle if not exists,
     * otherwise updates high/low/close/volume atomically.
     * Concurrency-safe under high load.
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO candles ( symbol, interval, bucket_time, open, high, low, close, volume)
        VALUES (:symbol, :interval, :bucket, :price, :price, :price, :price, 1 )
        ON CONFLICT (symbol, interval, bucket_time)
        DO UPDATE SET
            high = GREATEST(candles.high, EXCLUDED.high),
            low = LEAST(candles.low, EXCLUDED.low),
            close = EXCLUDED.close,
            volume = candles.volume + 1
        """,
            nativeQuery = true)
    int updateCandle(String symbol, Interval interval, Long bucket, double price);
}
