// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.tracing;

import com.azure.core.v2.util.TracingOptions;

/**
 * Resolves and provides {@link Tracer} implementation.
 * <p>
 * This class is intended to be used by Azure client libraries and provides abstraction over possible tracing
 * implementations.
 * Application developers should use tracing libraries such as OpenTelemetry or Spring tracing.
 */
public interface TracerProvider {
    /**
     * Creates named and versioned tracer instance.
     *
     * <!-- src_embed com.azure.core.util.tracing.TracerProvider#create-tracer -->
     * <pre>
     * Tracer tracer = TracerProvider.getDefaultProvider&#40;&#41;.createTracer&#40;&quot;azure-storage-blobs&quot;, &quot;12.20.0&quot;,
     *     &quot;Microsoft.Storage&quot;, clientOptions.getTracingOptions&#40;&#41;&#41;;
     * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
     *     .tracer&#40;tracer&#41;
     *     .clientOptions&#40;clientOptions&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.TracerProvider#create-tracer -->
     *
     * @param libraryName Azure client library package name
     * @param libraryVersion Azure client library version
     * @param azNamespace Azure Resource Provider namespace.
     * @param options instance of {@link TracingOptions}
     * @return a tracer instance.
     */
    Tracer createTracer(String libraryName, String libraryVersion, String azNamespace, TracingOptions options);

    /**
     * Returns default implementation of {@code TracerProvider} that uses SPI to resolve tracing implementation.
     * @return an instance of {@code TracerProvider}
     */
    static TracerProvider getDefaultProvider() {
        return DefaultTracerProvider.getInstance();
    }
}
