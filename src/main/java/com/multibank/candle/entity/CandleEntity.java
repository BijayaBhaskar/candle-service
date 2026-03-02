package com.multibank.candle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "candles",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"symbol", "interval", "bucket_time"}
        ),
        indexes = {
                @Index(
                        name = "idx_symbol_interval_bucket",
                        columnList = "symbol, interval, bucket_time"
                )
        }
)
@Getter
@Setter
public class CandleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    @Column(nullable = false, length = 20)
    private String interval;

    @Column(name = "bucket_time")
    private Long bucketTime;

    private double open;

    private double high;

    private double low;

    private double close;

    private long volume;
}
