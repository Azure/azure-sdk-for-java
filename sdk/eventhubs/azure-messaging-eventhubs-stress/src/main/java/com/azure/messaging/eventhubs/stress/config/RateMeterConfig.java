package com.azure.messaging.eventhubs.stress.config;

import com.azure.messaging.eventhubs.stress.util.Constants;
import com.azure.messaging.eventhubs.stress.util.ScenarioOptions;
import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateMeterConfig {
    @Autowired
    private TelemetryClient telemetryClient;

    @Autowired
    private ScenarioOptions options;

    @Bean
    public RateMeter createRateMeter() {
        return new RateMeter(telemetryClient, Duration.ofSeconds(
            Integer.parseInt(options.get(Constants.METRIC_INTERVAL_SEC, "60")))
        );
    }
}
