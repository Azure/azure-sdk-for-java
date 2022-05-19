package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.messaging.eventhubs.stress.util.Constants;
import com.azure.messaging.eventhubs.stress.util.RateMeter;
import com.azure.messaging.eventhubs.stress.util.ScenarioOptions;
import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.time.Duration;

public abstract class EventHubsScenario {
    @Autowired
    protected ScenarioOptions options;

    @Autowired
    private ApplicationContext applicationContext;

    protected TelemetryClient telemetryClient;

    protected RateMeter rateMeter;

    @Bean
    private TelemetryClient createTelemetryClient() {
        return new TelemetryClient();
    }

    @Bean
    private RateMeter createRateMeter() {
        return new RateMeter(applicationContext.getBean(TelemetryClient.class), Duration.ofSeconds(
                Integer.parseInt(options.get(Constants.METRIC_INTERVAL_SEC, "60")))
        );
    }

    @PostConstruct
    private void postConstruct() {
        this.rateMeter = applicationContext.getBean(RateMeter.class);
        this.telemetryClient = applicationContext.getBean(TelemetryClient.class);
    }

    public abstract void run();
}
