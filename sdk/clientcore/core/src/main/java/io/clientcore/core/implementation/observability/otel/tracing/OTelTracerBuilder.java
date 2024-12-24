// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.observability.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.TRACER_BUILDER_CLASS;

public class OTelTracerBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(OTelTracerBuilder.class);
    private static final ReflectiveInvoker SET_INSTRUMENTATION_VERSION_INVOKER;
    private static final ReflectiveInvoker BUILD_INVOKER;
    private static final ReflectiveInvoker SET_SCHEMA_URL_INVOKER;
    private final Object otelTracerBuilder;

    static {
        ReflectiveInvoker setInstrumentationVersionInvoker = null;
        ReflectiveInvoker buildInvoker = null;
        ReflectiveInvoker setSchemaUrlInvoker = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                setInstrumentationVersionInvoker = ReflectionUtils.getMethodInvoker(TRACER_BUILDER_CLASS,
                    TRACER_BUILDER_CLASS.getMethod("setInstrumentationVersion", String.class));
                setSchemaUrlInvoker = ReflectionUtils.getMethodInvoker(TRACER_BUILDER_CLASS,
                    TRACER_BUILDER_CLASS.getMethod("setSchemaUrl", String.class));
                buildInvoker
                    = ReflectionUtils.getMethodInvoker(TRACER_BUILDER_CLASS, TRACER_BUILDER_CLASS.getMethod("build"));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        SET_INSTRUMENTATION_VERSION_INVOKER = setInstrumentationVersionInvoker;
        SET_SCHEMA_URL_INVOKER = setSchemaUrlInvoker;
        BUILD_INVOKER = buildInvoker;
    }

    public OTelTracerBuilder(Object otelTracerBuilder) {
        this.otelTracerBuilder = otelTracerBuilder;
    }

    public OTelTracerBuilder setInstrumentationVersion(String instrumentationVersion) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelTracerBuilder != null) {
            try {
                SET_INSTRUMENTATION_VERSION_INVOKER.invokeWithArguments(otelTracerBuilder, instrumentationVersion);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }
        return this;
    }

    public OTelTracerBuilder setSchemaUrl(String schemaUrl) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelTracerBuilder != null) {
            try {
                SET_SCHEMA_URL_INVOKER.invokeWithArguments(otelTracerBuilder, schemaUrl);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }
        return this;
    }

    public OTelTracer build() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelTracerBuilder != null) {
            try {
                return new OTelTracer(BUILD_INVOKER.invokeWithArguments(otelTracerBuilder));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }
        return OTelTracer.NOOP;
    }
}
