package com.multibank.candle.domain;

import java.util.List;

/**
 * Response structure for historical candle API.
 * @param s status of the response
 * @param t list of candle start time in UNIX seconds
 * @param o list of open prices
 * @param h list of high prices
 * @param l list of low prices
 * @param c list of close prices
 * @param v list of volumes
 */
public record HistoryResponse(
        String s,
        List<Long> t,
        List<Double> o,
        List<Double> h,
        List<Double> l,
        List<Double> c,
        List<Long> v
){}
