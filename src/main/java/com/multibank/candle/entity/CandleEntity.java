package com.multibank.candle.entity;

import com.multibank.candle.domain.Interval;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "candle")
@Getter
@Setter
public class CandleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    @Enumerated(EnumType.STRING)
    private Interval interval;
    @Column(name = "bucket_time")
    private Long bucketTime;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
}
