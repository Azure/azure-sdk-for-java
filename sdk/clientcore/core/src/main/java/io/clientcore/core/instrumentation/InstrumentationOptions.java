// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

/**
 * Telemetry options describe application-level configuration and can be configured on specific
 * client instances via the corresponding client builder.
 * <p>
 *
 * Library should use them on all instance of {@link io.clientcore.core.instrumentation.tracing.Tracer}
 * it creates and, if it sets up {@link io.clientcore.core.http.pipeline.InstrumentationPolicy}, it should pass
 * {@link InstrumentationOptions} to the policy.
 *
 * @param <T> The type of the provider. Only {@code io.opentelemetry.api.OpenTelemetry} is supported.
 */
public class InstrumentationOptions<T> {
    private boolean isTracingEnabled = true;
    private T provider = null;

    /**
     * Enables or disables distributed tracing. Distributed tracing is enabled by default when
     * OpenTelemetry is found on the classpath and is configured to export traces.
     *
     * <p><strong>Disable distributed tracing on a specific client instance</strong></p>
     *
     * <!-- src_embed io.clientcore.core.telemetry.disabledistributedtracing -->
     * <pre>
     *
     * InstrumentationOptions&lt;?&gt; instrumentationOptions = new InstrumentationOptions&lt;&gt;&#40;&#41;
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
    public InstrumentationOptions<T> setTracingEnabled(boolean isTracingEnabled) {
        this.isTracingEnabled = isTracingEnabled;
        return this;
    }

    /**
     * Sets the provider to use for telemetry. Only {@code io.opentelemetry.api.OpenTelemetry} and
     * derived classes are supported.
     * <p>
     *
     * When provider is not passed explicitly, clients will attempt to use global OpenTelemetry instance.
     *
     * <p><strong>Pass configured OpenTelemetry instance explicitly</strong></p>
     *
     * <!-- src_embed io.clientcore.core.telemetry.useexplicitopentelemetry -->
     * <pre>
     *
     * OpenTelemetry openTelemetry =  AutoConfiguredOpenTelemetrySdk.initialize&#40;&#41;.getOpenTelemetrySdk&#40;&#41;;
     * InstrumentationOptions&lt;OpenTelemetry&gt; instrumentationOptions = new InstrumentationOptions&lt;OpenTelemetry&gt;&#40;&#41;
     *     .setProvider&#40;openTelemetry&#41;;
     *
     * SampleClient client = new SampleClientBuilder&#40;&#41;.instrumentationOptions&#40;instrumentationOptions&#41;.build&#40;&#41;;
     * client.clientCall&#40;&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.telemetry.useexplicitopentelemetry -->
     *
     * @param provider The provider to use for telemetry.
     * @return The updated {@link InstrumentationOptions} object.
     */
    public InstrumentationOptions<T> setProvider(T provider) {
        this.provider = provider;
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
    public T getProvider() {
        return provider;
    }

    /**
     * Creates an instance of {@link InstrumentationOptions}.
     */
    public InstrumentationOptions() {
    }
}
