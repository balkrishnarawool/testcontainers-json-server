package com.balarawool.testcontainers.jsonserver;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WeatherStatsServiceIntegrationTest.TestConfiguration.class
)
public class WeatherStatsServiceIntegrationTest {

    private static GenericContainer jsonServer = new GenericContainer("clue/json-server")
            .withClasspathResourceMapping("db.json","/data/db.json", BindMode.READ_WRITE)
            .withExposedPorts(80)
            .waitingFor(getWaitStrategy());

    private static WaitAllStrategy getWaitStrategy() {
        return new WaitAllStrategy().withStrategy(Wait.forHttp("/").forStatusCode(200));
    }

    @DynamicPropertySource
    public static void setPostManagerApiConfig(DynamicPropertyRegistry registry) {
        jsonServer.start();

        registry.add("weatherservice.host", jsonServer::getContainerIpAddress);
        registry.add("weatherservice.port", jsonServer::getFirstMappedPort);
    }

    @Autowired
    private WeatherStatsService weatherStatsService;

    @Test
    public void shouldGetMockedWeatherData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        WeatherStatsService.WeatherStats weatherStats = weatherStatsService.getWeatherStats();

        assertEquals(32, weatherStats.getMax());
        assertEquals(25, weatherStats.getMin());
    }

    @EnableAutoConfiguration // Important because testcontainers does some auto-configuration for GenericContainer
    @ComponentScan({"com.balarawool.testcontainers.jsonserver"})
    @Configuration
    static class TestConfiguration {
    }

}
