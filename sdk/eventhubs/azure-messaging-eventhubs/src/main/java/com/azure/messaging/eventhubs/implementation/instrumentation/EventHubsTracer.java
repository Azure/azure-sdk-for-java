// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracingLink;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.azure.core.util.tracing.SpanKind.CLIENT;
import static com.azure.core.util.tracing.SpanKind.CONSUMER;
import static com.azure.core.util.tracing.SpanKind.PRODUCER;

import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.DIAGNOSTIC_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_BATCH_MESSAGE_COUNT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_PARTITION_ID;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_EVENTHUBS_MESSAGE_ENQUEUED_TIME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_OPERATION_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_OPERATION_TYPE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_SYSTEM;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_SYSTEM_VALUE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.SERVER_ADDRESS;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.TRACEPARENT_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.getErrorType;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.getOperationType;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.unwrap;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.EVENT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.PROCESS;

public class EventHubsTracer {
    private static final AutoCloseable NOOP_AUTOCLOSEABLE = () -> {
    };

    private static final TracingLink DUMMY_LINK = new TracingLink(Context.NONE);

    private static final ClientLogger LOGGER = new ClientLogger(EventHubsTracer.class);

    private static final boolean IS_TRACING_DISABLED = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TRACING_DISABLED, false);

    protected final Tracer tracer;
    private final String fullyQualifiedName;
    private final String entityName;
    private final String consumerGroup;

    public EventHubsTracer(Tracer tracer, String fullyQualifiedName, String entityName, String consumerGroup) {
        this.tracer = IS_TRACING_DISABLED ? null : tracer;
        this.fullyQualifiedName = Objects.requireNonNull(fullyQualifiedName, "'fullyQualifiedName' cannot be null");
        this.entityName = Objects.requireNonNull(entityName, "'entityPath' cannot be null");
        this.consumerGroup = DEFAULT_CONSUMER_GROUP_NAME.equalsIgnoreCase(consumerGroup) ? null : consumerGroup;
    }

    public boolean isEnabled() {
        return tracer != null && tracer.isEnabled();
    }

    public Context startSpan(OperationName operationName, StartSpanOptions startOptions, Context context) {
        return isEnabled() ? tracer.start(getSpanName(operationName), startOptions, context) : context;
    }

    public Context startGenericOperationSpan(OperationName operationName, String partitionId, Context parent) {
        if (!isEnabled()) {
            return parent;
        }

        return tracer.start(getSpanName(operationName),
                    createStartOptions(CLIENT, operationName, partitionId),
                    parent);
    }

    /**
     * Used in ServiceBusMessageBatch.tryAddMessage() to start tracing for to-be-sent out messages.
     */
    public void reportMessageSpan(EventData eventData, Context eventContext) {
        if (!isEnabled() || eventContext == null || eventContext.getData(SPAN_CONTEXT_KEY).isPresent()) {
            // if message has context (in case of retries), don't start a message span or add a new context
            return;
        }

        String traceparent = EventHubsTracer.getTraceparent(eventData.getProperties());
        if (traceparent != null) {
            // if message has context (in case of retries) or if user supplied it, don't start a message span or add a new context
            return;
        }

        // Starting the span makes the sampling decision (nothing is logged at this time)
        StartSpanOptions startOptions = createStartOptions(PRODUCER, EVENT, null);

        Context eventSpanContext = tracer.start(getSpanName(EVENT), startOptions, eventContext);

        Exception exception = null;
        String error = null;
        if (canModifyApplicationProperties(eventData.getProperties())) {
            try {
                tracer.injectContext((key, value) -> {
                    eventData.getProperties().put(key, value);
                    if (TRACEPARENT_KEY.equals(key)) {
                        eventData.getProperties().put(DIAGNOSTIC_ID_KEY, value);
                    }
                }, eventSpanContext);
            } catch (RuntimeException ex) {
                // it might happen that unmodifiable map is something that we
                // didn't account for in canModifyApplicationProperties method
                // if it happens, let's not break everything, but log a warning
                LOGGER.warning("Failed to inject context into EventData", ex);
                exception = ex;
            }
        } else {
            error = "failed to inject context into EventData";
        }
        tracer.end(error, exception, eventSpanContext);

        Optional<Object> spanContext = eventSpanContext.getData(SPAN_CONTEXT_KEY);
        spanContext.ifPresent(o -> eventData.addContext(SPAN_CONTEXT_KEY, o));
    }

    public TracingLink createLink(Map<String, Object> applicationProperties, Instant enqueuedTime) {
        return createLink(extractContext(applicationProperties), enqueuedTime);
    }

    private TracingLink createLink(Context linkContext, Instant enqueuedTime) {
        Map<String, Object> linkAttributes = null;
        if (enqueuedTime != null) {
            linkAttributes = Collections.singletonMap(MESSAGING_EVENTHUBS_MESSAGE_ENQUEUED_TIME, enqueuedTime.atOffset(ZoneOffset.UTC).toEpochSecond());
        }

        return new TracingLink(linkContext, linkAttributes);
    }

    public TracingLink createProducerLink(Map<String, Object> applicationProperties, Context eventContext) {
        if (!tracer.isEnabled() || applicationProperties == null) {
            return DUMMY_LINK;
        }

        Context link = Context.NONE;
        Optional<Object> linkContext = eventContext.getData(SPAN_CONTEXT_KEY);
        if (linkContext.isPresent()) {
            if (linkContext.get() instanceof Context) {
                link = (Context) linkContext.get();
            } else {
                LOGGER.verbose("Unexpected type under 'span-context' key - {}", linkContext.get().getClass());
            }
        } else {
            link = extractContext(applicationProperties);
        }

        return new TracingLink(link, null);
    }

    void endSpan(String errorType, Throwable throwable, Context context, AutoCloseable scope) {
        if (isEnabled()) {
            try {
                if (scope != null) {
                    scope.close();
                }
            } catch (Exception e) {
                LOGGER.verbose("Can't close scope", e);
            } finally {
                tracer.end(errorType == null ? getErrorType(throwable) : errorType, unwrap(throwable), context);
            }
        }
    }

    AutoCloseable makeSpanCurrent(Context context) {
        return isEnabled() ? tracer.makeSpanCurrent(context) : NOOP_AUTOCLOSEABLE;
    }

    Context startProcessSpan(Map<String, Object> applicationProperties, Instant enqueuedTime, String partitionId) {
        if (isEnabled()) {
            Context remoteContext = extractContext(applicationProperties);
            StartSpanOptions startOptions = createStartOptions(CONSUMER, PROCESS, partitionId)
                .addLink(createLink(remoteContext, null))
                .setRemoteParent(remoteContext);

            // we could add these attributes only on sampled-in spans, but we don't have the API to check
            if (enqueuedTime != null) {
                startOptions.setAttribute(MESSAGING_EVENTHUBS_MESSAGE_ENQUEUED_TIME, enqueuedTime.atOffset(ZoneOffset.UTC).toEpochSecond());
            }

            return tracer.start(getSpanName(PROCESS), startOptions, Context.NONE);
        }

        return Context.NONE;
    }

    Context startProcessSpan(EventBatchContext batchContext) {
        if (isEnabled() && batchContext != null && !CoreUtils.isNullOrEmpty(batchContext.getEvents())) {
            StartSpanOptions startOptions = createStartOptions(CONSUMER, PROCESS,
                    batchContext.getPartitionContext().getPartitionId());
            startOptions.setAttribute(MESSAGING_BATCH_MESSAGE_COUNT, batchContext.getEvents().size());

            for (EventData event : batchContext.getEvents()) {
                startOptions.addLink(createLink(event.getProperties(), event.getEnqueuedTime()));
            }

            return tracer.start(getSpanName(PROCESS), startOptions, Context.NONE);
        }

        return Context.NONE;
    }

    public StartSpanOptions createStartOptions(SpanKind kind, OperationName operationName, String partitionId) {
        StartSpanOptions startOptions = new StartSpanOptions(kind)
                .setAttribute(MESSAGING_SYSTEM, MESSAGING_SYSTEM_VALUE)
                .setAttribute(MESSAGING_DESTINATION_NAME, entityName)
                .setAttribute(SERVER_ADDRESS, fullyQualifiedName)
                .setAttribute(MESSAGING_OPERATION_NAME, operationName.toString());

        if (consumerGroup != null) {
            startOptions.setAttribute(MESSAGING_CONSUMER_GROUP_NAME, consumerGroup);
        }

        if (partitionId != null) {
            startOptions.setAttribute(MESSAGING_DESTINATION_PARTITION_ID, partitionId);
        }

        String operationType = getOperationType(operationName);
        if (operationType != null) {
            startOptions.setAttribute(MESSAGING_OPERATION_TYPE, operationType);
        }

        return startOptions;
    }

    private static String getTraceparent(Map<String, Object> applicationProperties) {
        Object diagnosticId = applicationProperties.get(DIAGNOSTIC_ID_KEY);
        if (diagnosticId == null) {
            diagnosticId = applicationProperties.get(TRACEPARENT_KEY);
        }

        return diagnosticId == null ? null : diagnosticId.toString();
    }

    private Context extractContext(Map<String, Object> applicationProperties) {
        if (tracer.isEnabled() && applicationProperties != null) {
            return tracer.extractContext(key -> {
                if (TRACEPARENT_KEY.equals(key)) {
                    return getTraceparent(applicationProperties);
                } else {
                    Object value = applicationProperties.get(key);
                    if (value != null) {
                        return value.toString();
                    }
                }
                return null;
            });
        }

        return Context.NONE;
    }

    private String getSpanName(OperationName operationName) {
        return operationName.toString() + " " + entityName;
    }

    private static boolean canModifyApplicationProperties(Map<String, Object> applicationProperties) {
        return applicationProperties != null && !applicationProperties.getClass().getSimpleName().equals("UnmodifiableMap");
    }
}
