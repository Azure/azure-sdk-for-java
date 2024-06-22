// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import io.clientcore.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TelemetryAttributes;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Contains code snippets for {@link Meter} showing how to use it in Azure client libraries.
 */
public class MetricsJavaDocCodeSnippets {
    final MeterProvider meterProvider = MeterProvider.getDefaultProvider();
    final Meter defaultMeter = meterProvider.createMeter("azure-core-metrics-samples", "1.0.0", null);
    /**
     * Code snippet for {@link MeterProvider#createMeter(String, String, MetricsOptions)}}
     */
    public void createMeter() {
        // BEGIN: com.azure.core.util.metrics.MeterProvider.createMeter
        MetricsOptions metricsOptions = new MetricsOptions();

        Meter meter = MeterProvider.getDefaultProvider().createMeter("azure-core", "1.0.0", metricsOptions);
        // END: com.azure.core.util.metrics.MeterProvider.createMeter
    }

    /**
     * Code snippet for {@link Meter#createLongCounter(String, String, String)}}
     */
    public void createCounter() {
        Context currentContext = Context.NONE;
        // BEGIN: com.azure.core.util.metrics.Meter.longCounter
        TelemetryAttributes attributes = defaultMeter.createAttributes(new HashMap<String, Object>() {{
                put("endpoint", "http://service-endpoint.azure.com");
                put("status", "ok");
            }});

        LongCounter createdHttpConnections = defaultMeter.createLongCounter("az.core.http.connections",
            "Number of created HTTP connections", null);

        createdHttpConnections.add(1, attributes, currentContext);
        // END: com.azure.core.util.metrics.Meter.longCounter
    }

    /**
     * Code snippet for {@link Meter#createLongUpDownCounter(String, String, String)}
     */
    public void createUpDownCounter() {
        Context currentContext = Context.NONE;
        // BEGIN: com.azure.core.util.metrics.Meter.upDownCounter
        TelemetryAttributes attributes = defaultMeter.createAttributes(new HashMap<String, Object>() {{
                put("endpoint", "http://service-endpoint.azure.com");
                put("status", "ok");
            }});

        LongCounter activeHttpConnections = defaultMeter.createLongUpDownCounter("az.core.http.active.connections",
            "Number of active HTTP connections", null);

        // on connection initialized:
        activeHttpConnections.add(1, attributes, currentContext);

        // on connection closed:
        activeHttpConnections.add(-1, attributes, currentContext);
        // END: com.azure.core.util.metrics.Meter.upDownCounter
    }

    /**
     * Code snippet for {@link Meter#createLongGauge(String, String, String)}
     */
    public void createLongGauge() {
        AtomicLong sequenceNumber = new AtomicLong();

        // BEGIN: com.azure.core.util.metrics.Meter.longGauge
        TelemetryAttributes attributes = defaultMeter.createAttributes(new HashMap<String, Object>() {{
                put("endpoint", "http://service-endpoint.azure.com");
                put("container", "my-container");
            }});

        LongGauge latestSequenceNumber = defaultMeter.createLongGauge("az.eventhubs.consumer.sequence_number",
            "Sequence number of the latest event received from the broker.", null);

        AutoCloseable subscription = latestSequenceNumber.registerCallback(sequenceNumber::get, attributes);

        // update value when event is received
        sequenceNumber.set(getSequenceNumber());

        try {
            subscription.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // END: com.azure.core.util.metrics.Meter.longGauge
    }


    /**
     * Code snippet for {@link Meter#createDoubleHistogram(String, String, String)}}
     */
    public void createHistogram() {
        Context currentContext = Context.NONE;
        // BEGIN: com.azure.core.util.metrics.Meter.doubleHistogram

        // Meter and instruments should be created along with service client instance and retained for the client
        // lifetime for optimal performance
        Meter meter = meterProvider
            .createMeter("azure-core", "1.0.0", new MetricsOptions());

        DoubleHistogram amqpLinkDuration = meter
            .createDoubleHistogram("az.core.amqp.link.duration", "AMQP link response time.", "ms");

        TelemetryAttributes attributes = defaultMeter.createAttributes(
            Collections.singletonMap("endpoint", "http://service-endpoint.azure.com"));

        // when measured operation starts, record the measurement
        Instant start = Instant.now();

        doThings();

        // optionally check if meter is operational for the best performance
        if (amqpLinkDuration.isEnabled()) {
            amqpLinkDuration.record(Instant.now().toEpochMilli() - start.toEpochMilli(), attributes, currentContext);
        }
        // END: com.azure.core.util.metrics.Meter.doubleHistogram
    }

    /**
     * Code snippet for {@link Meter#createLongCounter(String, String, String)}}
     */
    public void createCounterWithErrorFlag() {
        Context currentContext = Context.NONE;

        // BEGIN: com.azure.core.util.metrics.Meter.longCounter#errorFlag

        // Create attributes for possible error codes. Can be done lazily once specific error code is received.
        TelemetryAttributes successAttributes = defaultMeter.createAttributes(new HashMap<String, Object>() {{
                put("endpoint", "http://service-endpoint.azure.com");
                put("error", true);
            }});

        TelemetryAttributes errorAttributes =  defaultMeter.createAttributes(new HashMap<String, Object>() {{
                put("endpoint", "http://service-endpoint.azure.com");
                put("error", false);
            }});

        LongCounter httpConnections = defaultMeter.createLongCounter("az.core.http.connections",
            "Number of created HTTP connections", null);

        boolean success = false;
        try {
            success = doThings();
        } finally {
            httpConnections.add(1, success ? successAttributes : errorAttributes, currentContext);
        }

        // END: com.azure.core.util.metrics.Meter.longCounter#errorFlag
    }

    private boolean doThings() {
        return true;
    }
    private long getSequenceNumber() {
        return 1;
    }
}
