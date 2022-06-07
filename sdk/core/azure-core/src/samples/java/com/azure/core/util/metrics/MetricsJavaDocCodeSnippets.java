// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AzureAttributeBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;

import java.time.Instant;

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
     * Code snippet for {@link AzureMeter#createLongCounter(String, String, String)}}
     */
    public void createCounter() {
        Context currentContext = Context.NONE;
        // BEGIN: com.azure.core.util.metrics.AzureMeter.longCounter
        AzureAttributeBuilder attributes = meterProvider.createAttributeBuilder()
            .add("endpoint", "http://service-endpoint.azure.com")
            .add("error", true);

        AzureLongCounter createdHttpConnections = defaultMeter.createLongCounter("az.core.http.connections",
            "Number of created HTTP connections", null);

        createdHttpConnections.add(1, attributes, currentContext);
        // END: com.azure.core.util.metrics.AzureMeter.longCounter
    }

    /**
     * Code snippet for {@link AzureMeter#createLongHistogram(String, String, String)}}
     */
    public void createHistogram() {
        Context currentContext = Context.NONE;
        // BEGIN: com.azure.core.util.metrics.AzureMeter.longHistogram

        // Meter and instruments should be created along with service client instance and retained for the client
        // lifetime for optimal performance
        AzureMeter meter = meterProvider
            .createMeter("azure-core", "1.0.0", new MetricsOptions());

        AzureLongHistogram amqpLinkDuration = meter
            .createLongHistogram("az.core.amqp.link.duration", "AMQP link response time.", "ms");

        AzureAttributeBuilder attributes = meterProvider.createAttributeBuilder()
            .add("endpoint", "http://service-endpoint.azure.com");

        // when measured operation starts, record the measurement
        Instant start = Instant.now();

        doThings();

        // optionally check if meter is operational for the best performance
        if (meter.isEnabled()) {
            amqpLinkDuration.record(Instant.now().toEpochMilli() - start.toEpochMilli(), attributes, currentContext);
        }
        // END: com.azure.core.util.metrics.AzureMeter.longHistogram
    }

    /**
     * Code snippet for {@link AzureMeter#createLongCounter(String, String, String)}}
     */
    public void createCounterWithErrorFlag() {
        Context currentContext = Context.NONE;

        // BEGIN: com.azure.core.util.metrics.AzureMeter.longCounter#errorFlag

        // Create attributes for possible error codes. Can be done lazily once specific error code is received.
        AzureAttributeBuilder successAttributes = meterProvider.createAttributeBuilder()
            .add("endpoint", "http://service-endpoint.azure.com")
            .add("error", true);

        AzureAttributeBuilder errorAttributes =  meterProvider.createAttributeBuilder()
            .add("endpoint", "http://service-endpoint.azure.com")
            .add("error", false);

        AzureLongCounter httpConnections = defaultMeter.createLongCounter("az.core.http.connections",
            "Number of created HTTP connections", null);

        boolean success = false;
        try {
            success = doThings();
        } finally {
            httpConnections.add(1, success ? successAttributes : errorAttributes, currentContext);
        }

        // END: com.azure.core.util.metrics.AzureMeter.longCounter#errorFlag
    }

    private boolean doThings() {
        return true;
    }
}
