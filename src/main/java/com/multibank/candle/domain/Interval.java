package com.multibank.candle.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Interval {

    ONE_SEC("1s", 1),
    FIVE_SEC("5s",5),
    ONE_MIN("1m", 60),
    FIVE_MIN("5m", 300),
    FIFTEEN_MIN("15m",900),
    ONE_HOUR("1h",3600),
    TWO_HOUR("2h",7200);


    private final String code;
    @Getter
    private final long seconds;

    Interval(String code, long seconds) {
        this.code = code;
        this.seconds = seconds;
    }

    private static final Map<String, Interval> LOOKUP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(
                            i -> i.code.toLowerCase(),
                            i -> i));

    public static Interval fromCode(String code) {
        Interval interval = LOOKUP.get(code.toLowerCase());
        if (interval == null) {
            throw new IllegalArgumentException("Invalid interval: " + code);
        }
        return interval;
    }

}
