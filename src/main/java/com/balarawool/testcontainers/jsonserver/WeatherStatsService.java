package com.balarawool.testcontainers.jsonserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class WeatherStatsService {

    @Value("${weatherservice.host}")
    private String host;
    @Value("${weatherservice.port}")
    private String port;

    private final RestTemplate restTemplate = new RestTemplateBuilder().build();

    public WeatherStats getWeatherStats() {
        final String url = "http://" + host + ":" + port + "/forecast24hours";

        final DayForecast dayForecast = restTemplate.getForEntity(url, DayForecast.class).getBody();
        final int max = dayForecast.getForecast().stream().map(HourForecast::getTemperature).max(Integer::compare).get();
        final int min = dayForecast.getForecast().stream().map(HourForecast::getTemperature).min(Integer::compare).get();

        return new WeatherStats(max, min);
    }

    @Data
    static class DayForecast {
        private List<HourForecast> forecast;
    }

    @Data
    static class HourForecast {
        private int offset;
        private int temperature;
    }

    @Data
    @AllArgsConstructor
    static class WeatherStats {
        private int max;
        private int min;
    }
}
