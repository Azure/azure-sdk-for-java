package com.azure.spring.integration.eventhub.metrics;

import com.azure.spring.integration.eventhub.api.EventHubOperation;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class EventHubMetrics implements MeterBinder {

    private EventHubOperation eventHubOperation;

    public EventHubMetrics(EventHubOperation eventHubOperation) {
        this.eventHubOperation = eventHubOperation;
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {

    }


}
