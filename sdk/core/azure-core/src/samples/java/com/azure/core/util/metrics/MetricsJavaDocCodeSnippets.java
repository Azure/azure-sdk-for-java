// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AttributesBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;

import java.time.Instant;

/**
 * Contains code snippets for {@link Meter} showing how to use it in Azure client libraries.
 */
public class MetricsJavaDocCodeSnippets {
    final MeterProvider meterProvider = new MicrometerMeterProvider();
    final Meter defaultMeter = meterProvider.createMeter("azure-core-metrics-samples", "1.0.0", null);
    /**
     * Code snippet for {@link MeterProvider#createMeter(String, String, MetricsOptions)}}
     */
    public void createMeter() {
        // BEGIN: com.azure.core.util.metrics.MeterProvider.createMeter
        MetricsOptions metricsOptions = new MicrometerMetricsOptions()
            .setRegistry(new LoggingMeterRegistry());

        Meter meter = meterProvider.createMeter("azure-core", "1.0.0", metricsOptions);
        // END: com.azure.core.util.metrics.MeterProvider.createMeter
    }

    /**
     * Code snippet for {@link Meter#createLongCounter(String, String, String)}}
     */
    public void createCounter() {
        Context currentContext = Context.NONE;
        // BEGIN: com.azure.core.util.metrics.Meter.longCounter
        AttributesBuilder attributes = defaultMeter.createAttributesBuilder()
            .add("endpoint", "http://service-endpoint.azure.com")
            .add("error", true);

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
        AttributesBuilder attributes = defaultMeter.createAttributesBuilder()
            .add("endpoint", "http://service-endpoint.azure.com")
            .add("error", true);

        LongCounter activeHttpConnections = defaultMeter.createLongUpDownCounter("az.core.http.active.connections",
            "Number of active HTTP connections", null);

        // on connection initialized:
        activeHttpConnections.add(1, attributes, currentContext);

        // on connection closed:
        activeHttpConnections.add(-1, attributes, currentContext);
        // END: com.azure.core.util.metrics.Meter.upDownCounter
    }

    /**
     * Code snippet for {@link Meter#createLongHistogram(String, String, String)}}
     */
    public void createHistogram() {
        Context currentContext = Context.NONE;
        // BEGIN: com.azure.core.util.metrics.Meter.longHistogram

        // Meter and instruments should be created along with service client instance and retained for the client
        // lifetime for optimal performance
        Meter meter = meterProvider
            .createMeter("azure-core", "1.0.0", new MetricsOptions());

        LongHistogram amqpLinkDuration = meter
            .createLongHistogram("az.core.amqp.link.duration", "AMQP link response time.", "ms");

        AttributesBuilder attributes = meter.createAttributesBuilder()
            .add("endpoint", "http://service-endpoint.azure.com");

        // when measured operation starts, record the measurement
        Instant start = Instant.now();

        doThings();

        // optionally check if meter is operational for the best performance
        if (amqpLinkDuration.isEnabled()) {
            amqpLinkDuration.record(Instant.now().toEpochMilli() - start.toEpochMilli(), attributes, currentContext);
        }
        // END: com.azure.core.util.metrics.Meter.longHistogram
    }

    /**
     * Code snippet for {@link Meter#createLongCounter(String, String, String)}}
     */
    public void createCounterWithErrorFlag() {
        Context currentContext = Context.NONE;

        // BEGIN: com.azure.core.util.metrics.Meter.longCounter#errorFlag

        // Create attributes for possible error codes. Can be done lazily once specific error code is received.
        AttributesBuilder successAttributes = defaultMeter.createAttributesBuilder()
            .add("endpoint", "http://service-endpoint.azure.com")
            .add("error", true);

        AttributesBuilder errorAttributes =  defaultMeter.createAttributesBuilder()
            .add("endpoint", "http://service-endpoint.azure.com")
            .add("error", false);

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
}
