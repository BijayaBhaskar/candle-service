package com.multibank.candle.controller;

import com.multibank.candle.domain.HistoryResponse;
import com.multibank.candle.domain.Interval;
import com.multibank.candle.service.query.CandleQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(HistoryControllerTest.TestConfig.class)
public class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandleQueryService queryService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        CandleQueryService candleQueryService() {
            return Mockito.mock(CandleQueryService.class);
        }
    }

    @Test
    void shouldReturnHistorySuccessfully() throws Exception {

        HistoryResponse response = new HistoryResponse(
                "ok",
                List.of(1000L),
                List.of(100.0),
                List.of(110.0),
                List.of(90.0),
                List.of(105.0),
                List.of(3L)
        );

        when(queryService.getHistory("BTC-USD",
                Interval.ONE_SEC, 0L, 2000L))
                .thenReturn(response);

        mockMvc.perform(get("/history")
                        .param("symbol", "BTC-USD")
                        .param("interval", "ONE_SEC")
                        .param("from", "0")
                        .param("to", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s").value("ok"))
                .andExpect(jsonPath("$.t[0]").value(1000))
                .andExpect(jsonPath("$.o[0]").value(100.0))
                .andExpect(jsonPath("$.h[0]").value(110.0))
                .andExpect(jsonPath("$.l[0]").value(90.0))
                .andExpect(jsonPath("$.c[0]").value(105.0))
                .andExpect(jsonPath("$.v[0]").value(3));
    }

    @Test
    void shouldReturnValidationErrorForBlankSymbol() throws Exception {

        mockMvc.perform(get("/history")
                        .param("symbol", "")
                        .param("interval", "ONE_SEC")
                        .param("from", "0")
                        .param("to", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnValidationErrorForNegativeFrom() throws Exception {

        mockMvc.perform(get("/history")
                        .param("symbol", "BTC-USD")
                        .param("interval", "ONE_SEC")
                        .param("from", "-1")
                        .param("to", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenFromGreaterThanTo() throws Exception {

        mockMvc.perform(get("/history")
                        .param("symbol", "BTC-USD")
                        .param("interval", "ONE_SEC")
                        .param("from", "200")
                        .param("to", "100"))
                .andExpect(status().isBadRequest());
    }
}
