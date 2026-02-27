package com.multibank.candle.domain;

public enum Interval {
    ONE_SEC(1),
    FIVE_SEC(5),
    ONE_MIN(60),
    FIFTEEN_MIN(900),
    ONE_HOUR(3600);

    private final long seconds;

    Interval(long seconds) {
        this.seconds = seconds;
    }

    public long getSeconds() {
        return seconds;
    }
}
