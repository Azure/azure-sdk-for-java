package com.azure.spring.integration.eventhub.metrics;

import com.azure.spring.integration.eventhub.api.EventHubOperation;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicrometerListener implements EventHubOperation.Listener {

    private final MeterRegistry meterRegistry;

    private final List<Tag> tags;

    private final Map<String, EventHubMetrics> metrics = new HashMap<>();

    private final EventHubOperation eventHubOperation;

    public MicrometerListener(MeterRegistry meterRegistry, EventHubOperation eventHubOperation) {
        this(meterRegistry, eventHubOperation, Collections.emptyList());
    }

    public MicrometerListener(MeterRegistry meterRegistry, EventHubOperation eventHubOperation, List<Tag> tags) {
        this.meterRegistry = meterRegistry;
        this.eventHubOperation = eventHubOperation;
        this.tags = tags;
    }

    @Override
    public void eventHubTemplateCreated(String namespace, EventHubOperation eventHubOperation) {

        metrics.put(namespace, new EventHubMetrics(eventHubOperation));
        metrics.get(namespace).bindTo(meterRegistry);
    }

}
