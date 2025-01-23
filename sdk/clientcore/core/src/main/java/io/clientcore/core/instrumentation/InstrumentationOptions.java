// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;

/**
 * Telemetry options describe application-level configuration and can be configured on specific
 * client instances via the corresponding client builder.
 * <p>
 *
 * Library should use them on all instance of {@link io.clientcore.core.instrumentation.tracing.Tracer}
 * it creates and, if it sets up {@link HttpInstrumentationPolicy}, it should pass
 * {@link InstrumentationOptions} to the policy.
 */
public class InstrumentationOptions {
    private boolean isTracingEnabled = true;
    private Object telemetryProvider = null;

    /**
     * Enables or disables distributed tracing. Distributed tracing is enabled by default when
     * OpenTelemetry is found on the classpath and is configured to export traces.
     *
     * <p><strong>Disable distributed tracing on a specific client instance</strong></p>
     *
     * <!-- src_embed io.clientcore.core.telemetry.disabledistributedtracing -->
     * <pre>
     *
     * HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions&#40;&#41;
     *     .setTracingEnabled&#40;false&#41;;
     *
     * SampleClient client = new SampleClientBuilder&#40;&#41;.instrumentationOptions&#40;instrumentationOptions&#41;.build&#40;&#41;;
     * client.clientCall&#40;&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.telemetry.disabledistributedtracing -->
     *
     * @param isTracingEnabled true to enable distributed tracing, false to disable.
     * @return The updated {@link InstrumentationOptions} object.
     */
    public InstrumentationOptions setTracingEnabled(boolean isTracingEnabled) {
        this.isTracingEnabled = isTracingEnabled;
        return this;
    }

    /**
     * Sets the telemetry provider. Only {@code io.opentelemetry.api.OpenTelemetry} and
     * derived classes are currently supported.
     * <p>
     *
     * When provider is not passed explicitly, clients will attempt to use global OpenTelemetry instance.
     *
     * <p><strong>Pass configured OpenTelemetry instance explicitly</strong></p>
     *
     * <!-- src_embed io.clientcore.core.telemetry.useexplicitopentelemetry -->
     * <pre>
     *
     * OpenTelemetry openTelemetry = AutoConfiguredOpenTelemetrySdk.initialize&#40;&#41;.getOpenTelemetrySdk&#40;&#41;;
     * HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions&#40;&#41;
     *     .setTelemetryProvider&#40;openTelemetry&#41;;
     *
     * SampleClient client = new SampleClientBuilder&#40;&#41;.instrumentationOptions&#40;instrumentationOptions&#41;.build&#40;&#41;;
     *
     * &#47;&#47; this call will be traced using OpenTelemetry SDK provided explicitly
     * client.clientCall&#40;&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.telemetry.useexplicitopentelemetry -->
     *
     * @param telemetryProvider The provider to use for telemetry.
     * @return The updated {@link InstrumentationOptions} object.
     */
    public InstrumentationOptions setTelemetryProvider(Object telemetryProvider) {
        this.telemetryProvider = telemetryProvider;
        return this;
    }

    /**
     * Returns true if distributed tracing is enabled, false otherwise.
     *
     * @return true if distributed tracing is enabled, false otherwise.
     */
    public boolean isTracingEnabled() {
        return isTracingEnabled;
    }

    /**
     * Returns the telemetry provider.
     *
     * @return The telemetry provider instance.
     */
    public Object getTelemetryProvider() {
        return telemetryProvider;
    }

    /**
     * Creates an instance of {@link InstrumentationOptions}.
     */
    public InstrumentationOptions() {
    }
}
