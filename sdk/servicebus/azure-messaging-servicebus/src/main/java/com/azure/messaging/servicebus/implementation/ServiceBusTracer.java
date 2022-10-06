// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Signal;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;

public class ServiceBusTracer {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusTracer.class);
    protected static final String TRACEPARENT_KEY = "traceparent";
    protected static final String REACTOR_PARENT_TRACE_CONTEXT_KEY = "otel-context-key";

    protected static final boolean IS_TRACING_DISABLED = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TRACING_DISABLED, false);
    protected final Tracer tracer;
    protected final String fullyQualifiedName;
    protected final String entityPath;

    protected ServiceBusTracer(Tracer tracer, String fullyQualifiedName, String entityPath) {
        this.tracer = IS_TRACING_DISABLED ? null : tracer;
        this.fullyQualifiedName = Objects.requireNonNull(fullyQualifiedName, "'fullyQualifiedName' cannot be null");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null");
    }

    public static Tracer getDefaultTracer() {
        Iterable<Tracer> tracers = ServiceLoader.load(Tracer.class);
        Iterator<Tracer> it = tracers.iterator();
        return it.hasNext() ? it.next() : null;
    }

    public boolean isEnabled() {
        return tracer != null;
    }

    public void endSpan(Throwable throwable, Context span, AutoCloseable scope) {
        if (tracer == null) {
            return;
        }

        String errorCondition = "success";
        if (throwable instanceof AmqpException) {
            AmqpException exception = (AmqpException) throwable;
            errorCondition = exception.getErrorCondition().getErrorCondition();
        }

        try {
            if (scope != null) {
                scope.close();
            }
        } catch (Exception e) {
            LOGGER.warning("Can't close scope", e);
        } finally {
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
        endSpan(signal.getThrowable(), span, null);
    }

    protected void addLink(Map<String, Object> applicationProperties, OffsetDateTime enqueuedTime, Context spanBuilder, Context eventContext) {
        if (applicationProperties == null) {
            return;
        }

        Optional<Object> linkContext = eventContext.getData(SPAN_CONTEXT_KEY);
        if (!linkContext.isPresent()) {
            String traceparent = getTraceparent(applicationProperties);
            Context link = traceparent == null ? Context.NONE : tracer.extractContext(traceparent, Context.NONE);
            linkContext = link.getData(SPAN_CONTEXT_KEY);
        }

        if (enqueuedTime != null) {
            spanBuilder = spanBuilder.addData(MESSAGE_ENQUEUED_TIME, enqueuedTime.toInstant());
        }

        if (linkContext.isPresent()) {
            tracer.addLink(spanBuilder.addData(SPAN_CONTEXT_KEY, linkContext.get()));
        }
    }
}
