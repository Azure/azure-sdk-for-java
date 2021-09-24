package com.azure.spring.integration.eventhub.metrics.meter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class EventHubRecodeSendTotalMetric implements MeterBinder {

    private Counter recodeSendTotal;

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        recodeSendTotal = Counter.builder("azure.eventhub.recode.send.total")
                                 .description("The total number of records sent.")
                                 .register(meterRegistry);

    }

    public Counter getRecodeSendTotal() {
        return recodeSendTotal;
    }
}
