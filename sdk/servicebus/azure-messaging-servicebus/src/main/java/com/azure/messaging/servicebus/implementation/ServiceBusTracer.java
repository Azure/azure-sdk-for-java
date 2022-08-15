// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Signal;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;

public class ServiceBusTracer {
    protected static final String TRACEPARENT_KEY = "traceparent";
    protected static final String REACTOR_PARENT_TRACE_CONTEXT_KEY = "otel-context-key";

    protected static final boolean IS_TRACING_DISABLED = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TRACING_DISABLED, false);
    protected final Tracer tracer;
    protected final String fullyQualifiedName;
    protected final String entityPath;

    protected ServiceBusTracer(String fullyQualifiedName, String entityPath) {
        this(getTracerOrNull(), fullyQualifiedName, entityPath);
    }

    protected ServiceBusTracer(Tracer tracer, String fullyQualifiedName, String entityPath) {
        this.tracer = IS_TRACING_DISABLED ? null : tracer;
        this.fullyQualifiedName = Objects.requireNonNull(fullyQualifiedName, "'fullyQualifiedName' cannot be null");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null");
    }

    public void endSpan(Throwable throwable, Context span) {
        if (tracer != null) {
            String errorCondition = "success";
            if (throwable instanceof AmqpException) {
                AmqpException exception = (AmqpException) throwable;
                errorCondition = exception.getErrorCondition().getErrorCondition();
            }
            tracer.end(errorCondition, throwable, span);
        }
    }

    protected Context getBuilder(String spanName, Context context) {
        return setAttributes(tracer.getSharedSpanBuilder(spanName, context));
    }

    protected Context setAttributes(Context context) {
        return context
            .addData(ENTITY_PATH_KEY, entityPath)
            .addData(HOST_NAME_KEY, fullyQualifiedName)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
    }

    protected static String getTraceparent(Map<String, Object> applicationProperties) {
        Object diagnosticId = applicationProperties.get(DIAGNOSTIC_ID_KEY);
        if (diagnosticId == null) {
            diagnosticId = applicationProperties.get(TRACEPARENT_KEY);
        }

        return diagnosticId == null ? null : diagnosticId.toString();
    }

    protected <T> void endSpan(Signal<T> signal) {
        Context span = signal.getContextView().getOrDefault(REACTOR_PARENT_TRACE_CONTEXT_KEY, Context.NONE);
        endSpan(signal.getThrowable(), span);
    }

    protected void addLink(Map<String, Object> applicationProperties, Context spanBuilder) {
        if (applicationProperties == null) {
            return;
        }

        String traceparent = getTraceparent(applicationProperties);
        Context link = traceparent == null ? Context.NONE : tracer.extractContext(traceparent, Context.NONE);
        Optional<Object> linkContext = link.getData(Tracer.SPAN_CONTEXT_KEY);
        if (linkContext.isPresent()) {
            tracer.addLink(spanBuilder.addData(Tracer.SPAN_CONTEXT_KEY, linkContext.get()));
        }
    }

    private static Tracer getTracerOrNull() {
        Iterable<Tracer> tracers = ServiceLoader.load(Tracer.class);
        Iterator<Tracer> it = tracers.iterator();
        return it.hasNext() ? it.next() : null;
    }
}
