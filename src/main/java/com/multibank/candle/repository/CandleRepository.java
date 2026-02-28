package com.multibank.candle.repository;

import com.multibank.candle.domain.Interval;
import com.multibank.candle.entity.CandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
