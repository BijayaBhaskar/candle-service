package com.multibank.candle.service.query;

import com.multibank.candle.domain.HistoryResponse;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.entity.CandleEntity;
import com.multibank.candle.repository.CandleRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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

    private final CandleRepository repository;

    /**
     * Constructor for CandleQueryService
     * @param repository CandleRepository
     */
    public CandleQueryService(CandleRepository repository){
        this.repository = repository;
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

        List<CandleEntity> candles = repository.findBySymbolAndIntervalAndBucketTimeBetweenOrderByBucketTimeAsc(symbol, interval, from, to);

        if(CollectionUtils.isEmpty(candles)){
            return emptyResponse();
        }


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
     * @param candles list of CandleEntity
     * @return formatted candle history response
     */
    private HistoryResponse buildResponse(List<CandleEntity> candles) {
        List<Long> t = new ArrayList<>();
        List<Double> o = new ArrayList<>();
        List<Double> h = new ArrayList<>();
        List<Double> l = new ArrayList<>();
        List<Double> c = new ArrayList<>();
        List<Long> v = new ArrayList<>();

        for (CandleEntity candle : candles) {
            t.add(candle.getBucketTime());
            o.add(candle.getOpen());
            h.add(candle.getHigh());
            l.add(candle.getLow());
            c.add(candle.getClose());
            v.add(candle.getVolume());
        }
        return new HistoryResponse("ok", t, o, h, l, c, v);
    }
}
