package com.multibank.candle.service.query;

import com.multibank.candle.domain.Candle;
import com.multibank.candle.domain.HistoryResponse;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.service.aggregation.CandleAggregationService;
import com.multibank.candle.service.aggregation.CandleBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for retrieving historical candle data
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Fetch aggregated candle data by symbol and interval</li>
 *     <li>Filter candles within the requested time range</li>
 *     <li>Sort candles chronologically</li>
 *     <li>Transform data into {@link HistoryResponse}</li>
 * </ul>
 * <p>
 * @author Bijaya Bhaskar Swain
 */
@Service
public class CandleQueryService {

    private final CandleAggregationService candleAggregationService;

    /**
     * Constructor for CandleQueryService
     * @param candleAggregationService CandleAggregationService
     */
    public CandleQueryService(CandleAggregationService candleAggregationService){
        this.candleAggregationService = candleAggregationService;
    }


    /**
     * Retrieves historical candle data for a given symbol and interval.
     *
     * @param symbol trading symbol (e.g., BTC-USD)
     * @param interval candle interval
     * @param from start time in UNIX seconds (inclusive)
     * @param to end time in UNIX seconds (inclusive)
     * @return {@link HistoryResponse}
     */
    public HistoryResponse getHistory(String symbol, Interval interval,
                                      long from, long to){

        Map<Long, CandleBuilder> map = candleAggregationService.getCandles(symbol, interval);

        if(map.isEmpty()){
            return emptyResponse();
        }

        List<Candle> candles = map.values().stream().map(CandleBuilder::build)
                .filter(c -> c.time() >= from && c.time() <= to)
                .sorted(Comparator.comparingLong(Candle::time))
                .toList();

        return buildResponse(candles);
    }

    /**
     * @return formatted empty history response
     */
    private HistoryResponse emptyResponse() {
        return new HistoryResponse("ok", List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of());
    }

    /**
     * This method build history response from list of candles
     * @param candles list of candles
     * @return formatted candle history response
     */
    private HistoryResponse buildResponse(List<Candle> candles) {
        List<Long> t = new ArrayList<>();
        List<Double> o = new ArrayList<>();
        List<Double> h = new ArrayList<>();
        List<Double> l = new ArrayList<>();
        List<Double> c = new ArrayList<>();
        List<Long> v = new ArrayList<>();

        for (Candle candle : candles) {
            t.add(candle.time());
            o.add(candle.open());
            h.add(candle.high());
            l.add(candle.low());
            c.add(candle.close());
            v.add(candle.volume());
        }
        return new HistoryResponse("ok", t, o, h, l, c, v);
    }
}
