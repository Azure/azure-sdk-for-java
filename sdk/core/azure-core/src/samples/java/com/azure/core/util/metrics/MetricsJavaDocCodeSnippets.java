// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains code snippets for {@link AzureMeter} showing how to use it in Azure client libraries.
 */
public class MetricsJavaDocCodeSnippets {
    final AzureMeterProvider meterProvider = new MicrometerMeterProvider();
    final AzureMeter defaultMeter = meterProvider.createMeter("azure-core-metrics-samples", "1.0.0", null);
    /**
     * Code snippet for {@link AzureMeterProvider#createMeter(String, String, MetricsOptions)}}
     */
    public void createMeter() {
        // BEGIN: com.azure.core.util.metrics.AzureMeterProvider.createMeter
        MetricsOptions metricsOptions = new MetricsOptions()
            .setProvider(new LoggingMeterRegistry());

        AzureMeter meter = meterProvider.createMeter("azure-core", "1.0.0", metricsOptions);
        // END: com.azure.core.util.metrics.AzureMeterProvider.createMeter
    }

    /**
     * Code snippet for {@link AzureMeter#createLongCounter(String, String, String, Map)}}
     */
    public void createCounter() {
        Context currentContext = Context.NONE;
        // BEGIN: com.azure.core.util.metrics.AzureMeter.longCounter
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("endpoint", "http://service-endpoint.azure.com");

        AzureLongCounter createdHttpConnections = defaultMeter.createLongCounter("az.core.http.connections",
            "Number of created HTTP connections", null, attributes);

        createdHttpConnections.add(1, currentContext);
        // END: com.azure.core.util.metrics.AzureMeter.longCounter
    }

    /**
     * Code snippet for {@link AzureMeter#createLongHistogram(String, String, String, Map)}}
     */
    public void createHistogram() {
        Context currentContext = Context.NONE;
        // BEGIN: com.azure.core.util.metrics.AzureMeter.longHistogram

        // Meter and instruments should be created along with service client instance and retained for the client
        // lifetime for optimal performance
        AzureMeter meter = AzureMeterProvider
            .getDefaultProvider()
            .createMeter("azure-core", "1.0.0", new MetricsOptions());

        AzureLongHistogram amqpLinkDuration = meter
            .createLongHistogram("az.core.amqp.link.duration", "AMQP link response time.", "ms",
                Collections.singletonMap("endpoint", "http://service-endpoint.azure.com"));

        // when measured operation starts, record the measurement
        Instant start = Instant.now();

        doThings();

        // optionally check if meter is operational for the best performance
        if (meter.isEnabled()) {
            amqpLinkDuration.record(Instant.now().toEpochMilli() - start.toEpochMilli(), currentContext);
        }
        // END: com.azure.core.util.metrics.AzureMeter.longHistogram
    }

    /**
     * Code snippet for {@link AzureMeter#createLongCounter(String, String, String, Map)}}
     */
    public void createCounterWithErrorFlag() {
        Context currentContext = Context.NONE;

        // BEGIN: com.azure.core.util.metrics.AzureMeter.longCounter#errorFlag

        // Create attributes with possible error status could be created upfront, usually along with client instance.
        Map<String, Object> successAttributes = getAttributes("http://service-endpoint.azure.com", false);
        Map<String, Object> errorAttributes = getAttributes("http://service-endpoint.azure.com", true);

        // Create instruments for possible error codes. Can be done lazily once specific error code is received.
        AzureLongCounter successfulHttpConnections = defaultMeter.createLongCounter("az.core.http.connections",
            "Number of created HTTP connections", null, successAttributes);

        AzureLongCounter failedHttpConnections = defaultMeter.createLongCounter("az.core.http.connections",
            "Number of created HTTP connections", null, errorAttributes);

        boolean success = false;
        try {
            success = doThings();
        } finally {
            if (success) {
                successfulHttpConnections.add(1, currentContext);
            } else {
                failedHttpConnections.add(1, currentContext);
            }
        }

        // END: com.azure.core.util.metrics.AzureMeter.longCounter#errorFlag
    }

    private static Map<String, Object> getAttributes(String endpoint, boolean error) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("endpoint", endpoint);
        attributes.put("error", error);

        return attributes;
    }

    private boolean doThings() {
        return true;
    }
}
