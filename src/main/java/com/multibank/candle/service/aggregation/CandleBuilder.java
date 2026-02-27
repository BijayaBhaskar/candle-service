package com.multibank.candle.service.aggregation;

import com.multibank.candle.domain.Candle;

/**
 * Internal mutable helper class used during candle aggregation.
 * @author Bijaya Bhaskar Swain
 */
public class CandleBuilder {

    private final long time;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;

    public CandleBuilder(long time, double price){
        this.time = time;
        this.open = price;
        this.high = price;
        this.low = price;
        this.close = price;
        this.volume = 1;
    }

    public synchronized void update(double price){
        high = Math.max(high, price);
        low = Math.min(low, price);
        close = price;
        volume++;
    }

    public Candle build(){
        return new Candle(time, open, high, low, close, volume);
    }

}
