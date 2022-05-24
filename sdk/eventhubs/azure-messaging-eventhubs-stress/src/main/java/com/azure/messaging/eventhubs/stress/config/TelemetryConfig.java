package com.azure.messaging.eventhubs.stress.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelemetryConfig {
    @Bean
    public TelemetryClient createTelemetryClient() {
        return new TelemetryClient();
    }

}
