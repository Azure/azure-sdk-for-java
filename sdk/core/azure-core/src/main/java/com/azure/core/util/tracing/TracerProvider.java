// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.util.SdkTelemetryOptions;
import com.azure.core.util.TracingOptions;

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
     * @param libraryName Azure client library package name
     * @param libraryVersion Azure client library version
     * @param azNamespace Azure Resource Provider namespace.
     * @param options instance of {@link TracingOptions}
     * @return a tracer instance.
     */
    Tracer createTracer(String libraryName, String libraryVersion, String azNamespace, TracingOptions options);

    /**
     * Creates tracer provider instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.core.util.tracing.TracerProvider#create-tracer -->
     * <pre>
     *
     * SdkTelemetryOptions sdkTelemetryOptions = new SdkTelemetryOptions&#40;&#41;
     *     .setSdkName&#40;&quot;azure-storage-blobs&quot;&#41;
     *     .setSdkVersion&#40;&quot;12.20.0&quot;&#41;
     *     .setResourceProviderNamespace&#40;&quot;Microsoft.Storage&quot;&#41;
     *     .setSchemaUrl&#40;&quot;https:&#47;&#47;opentelemetry.io&#47;schemas&#47;1.23.1&quot;&#41;;
     *
     * Tracer tracer = TracerProvider.getDefaultProvider&#40;&#41;
     *     .createTracer&#40;sdkTelemetryOptions, clientOptions.getTracingOptions&#40;&#41;&#41;;
     * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
     *     .tracer&#40;tracer&#41;
     *     .clientOptions&#40;clientOptions&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.TracerProvider#create-tracer -->
     *
     * @param providerOptions Library-specific tracing options.
     * @param options Tracing options configured by the application.
     * @return a tracer instance.
     */
    default Tracer createTracer(SdkTelemetryOptions providerOptions, TracingOptions options) {
        return createTracer(providerOptions.getSdkName(), providerOptions.getSdkVersion(),
            providerOptions.getResourceProviderNamespace(), options);
    }

    /**
     * Returns default implementation of {@code TracerProvider} that uses SPI to resolve tracing implementation.
     * @return an instance of {@code TracerProvider}
     */
    static TracerProvider getDefaultProvider() {
        return DefaultTracerProvider.getInstance();
    }
}
