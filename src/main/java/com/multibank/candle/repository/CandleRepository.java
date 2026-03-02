package com.multibank.candle.repository;

import com.multibank.candle.domain.Interval;
import com.multibank.candle.entity.CandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandleRepository extends JpaRepository<CandleEntity, Long> {

    List<CandleEntity> findBySymbolAndIntervalAndBucketTimeBetweenOrderByBucketTimeAsc(
            String symbol,
            Interval interval,
            Long from,
            Long to
    );


    @Modifying
    @Query(""" 
        UPDATE CandleEntity c
        SET c.high = CASE WHEN c.high < :price THEN :price ELSE c.high END,
            c.low = CASE WHEN c.low > :price THEN :price ELSE c.low END,
            c.close = :price,
            c.volume = c.volume + 1
        WHERE c.symbol = :symbol
        AND c.interval = :interval
        AND c.bucketTime = :bucket
        """)
    int updateCandle(String symbol, Interval interval, Long bucket, double price);
}
