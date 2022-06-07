// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AzureAttributeBuilder;
import com.azure.core.util.MetricsOptions;

/**
 * Resolves and provides {@link AzureMeter} implementation.
 *
 * This class is intended to be used by Azure client libraries and provides abstraction over different metrics implementations.
 * Application developers should use metrics implementations such as OpenTelemetry or Micrometer directly.
 */
public interface AzureMeterProvider {
    /**
     * Creates named and versioned meter instance.
     *
     * <!-- src_embed com.azure.core.util.metrics.AzureMeterProvider.createMeter -->
     * <pre>
     * MetricsOptions metricsOptions = new MetricsOptions&#40;&#41;
     *     .setProvider&#40;new LoggingMeterRegistry&#40;&#41;&#41;;
     *
     * AzureMeter meter = meterProvider.createMeter&#40;&quot;azure-core&quot;, &quot;1.0.0&quot;, metricsOptions&#41;;
     * </pre>
     * <!-- end com.azure.core.util.metrics.AzureMeterProvider.createMeter -->
     *
     * @param libraryName Azure client library package name
     * @param libraryVersion Azure client library version
     * @param options instance of {@link MetricsOptions}
     * @return a meter instance.
     */
    AzureMeter createMeter(String libraryName, String libraryVersion, MetricsOptions options);

    /**
     * Returns default implementation of {@code AzureMeterProvider} that uses SPI to resolve metrics implementation.
     * @return an instance of {@code AzureMeterProvider}
     */
    static AzureMeterProvider getDefaultProvider() {
        return DefaultAzureMeterProvider.INSTANCE;
    }

    /**
     * Creates and returns attribute collection implementation specific to the meter implementation.
     * Attribute collections differ in how they support different types of attributes and internal
     * data structures they use.
     *
     * For the best performance, client libraries should create and cache attribute collections
     * for the client lifetime and pass cached instance when recoding new measurements.
     *
     * <!-- src_embed com.azure.core.util.metrics.AzureMeter.longCounter#errorFlag -->
     * <pre>
     *
     * &#47;&#47; Create attributes for possible error codes. Can be done lazily once specific error code is received.
     * AzureAttributeBuilder successAttributes = meterProvider.createAttributeBuilder&#40;&#41;
     *     .add&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;
     *     .add&#40;&quot;error&quot;, true&#41;;
     *
     * AzureAttributeBuilder errorAttributes =  meterProvider.createAttributeBuilder&#40;&#41;
     *     .add&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;
     *     .add&#40;&quot;error&quot;, false&#41;;
     *
     * AzureLongCounter httpConnections = defaultMeter.createLongCounter&#40;&quot;az.core.http.connections&quot;,
     *     &quot;Number of created HTTP connections&quot;, null&#41;;
     *
     * boolean success = false;
     * try &#123;
     *     success = doThings&#40;&#41;;
     * &#125; finally &#123;
     *     httpConnections.add&#40;1, success ? successAttributes : errorAttributes, currentContext&#41;;
     * &#125;
     *
     * </pre>
     * <!-- end com.azure.core.util.metrics.AzureMeter.longCounter#errorFlag -->
     * @return an instance of {@code AzureAttributeBuilder}
     */
    AzureAttributeBuilder createAttributeBuilder();
}
