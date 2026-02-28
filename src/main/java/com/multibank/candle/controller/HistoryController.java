package com.multibank.candle.controller;

import com.multibank.candle.domain.HistoryResponse;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.service.query.CandleQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing historical candle data API.
 * <p>
 * Endpoint:
 * <pre>
 * GET /history?symbol=BTC-USD&interval=ONE_MIN&from=...&to=...
 * </pre>
 * <p>
 * @author Bijaya Bhaskar Swain
 */
@RestController
@RequestMapping("/history")
public class HistoryController {
    private final CandleQueryService queryService;

    /**
     * Constructor for historyController
     * @param queryService CandleQueryService
     */
    public HistoryController(CandleQueryService queryService){
        this.queryService = queryService;
    }

    /**
     * Retrieves historical candle data for a given symbol and interval.
     * @param symbol trading symbol (e.g., BTC-USD)
     * @param interval candle interval
     * @param from start time in UNIX seconds (inclusive)
     * @param to end time in UNIX seconds (inclusive)
     * @return {@link HistoryResponse}
     */
    @GetMapping
    public HistoryResponse getHistory(@RequestParam String symbol,
                                      @RequestParam Interval interval,
                                      @RequestParam long from,
                                      @RequestParam long to){
        if(from > to){
            throw  new IllegalArgumentException("from timestamp must be <= to timestamp");
        }
        return queryService.getHistory(symbol, interval, from, to);
    }
}
