// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.tracing.TracerProxy;
import com.azure.messaging.eventgrid.implementation.Constants;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImpl;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImplBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. This uses Project Reactor (https://projectreactor.io/) to handle asynchronous
 * programming.
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class, isAsync = true)
public final class EventGridPublisherAsyncClient {

    private final String hostname;

    private final EventGridPublisherClientImpl impl;

    private final EventGridServiceVersion serviceVersion;

    private final ClientLogger logger = new ClientLogger(EventGridPublisherAsyncClient.class);

    private final ObjectSerializer eventDataSerializer;

    EventGridPublisherAsyncClient(HttpPipeline pipeline, String hostname, EventGridServiceVersion serviceVersion,
        ObjectSerializer eventDataSerializer) {
        this.impl = new EventGridPublisherClientImplBuilder()
            .pipeline(pipeline)
            .buildClient();

        // currently the service version is hardcoded into the Impl client, but once another service version gets
        // released we should add this to the impl builder options
        this.serviceVersion = serviceVersion;

        this.hostname = hostname;
        this.eventDataSerializer = eventDataSerializer;
    }

    /**
     * Get the service version of the Rest API.
     * @return the Service version of the rest API
     */
    public EventGridServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain.
     * @param events the EventGrid events to publish.
     *
     * @return A {@link Mono} that completes when the events are sent to the service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendEventGridEvents(Iterable<EventGridEvent> events) {
        return withContext(context -> sendEventGridEvents(events, context));
    }

    Mono<Void> sendEventGridEvents(Iterable<EventGridEvent> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        return Flux.fromIterable(events)
            .map(event -> {
                com.azure.messaging.eventgrid.implementation.models.EventGridEvent internalEvent = event.toImpl();
                if (this.eventDataSerializer != null && internalEvent.getData() != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    eventDataSerializer.serialize(bos, event.getData());
                    internalEvent.setData(Base64.getEncoder().encode(bos.toByteArray()));
                }
                return internalEvent;
            })
            .collectList()
            .flatMap(list -> this.impl.publishEventsAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    /**
     * Publishes the given cloud events to the set topic or domain.
     * @param events the cloud events to publish.
     *
     * @return A {@link Mono} that completes when the events are sent to the service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendCloudEvents(Iterable<CloudEvent> events) {
        return withContext(context -> sendCloudEvents(events, context));
    }

    Mono<Void> sendCloudEvents(Iterable<CloudEvent> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        this.addCloudEventTracePlaceHolder(events);
        return Flux.fromIterable(events)
            .map(event -> {
                com.azure.messaging.eventgrid.implementation.models.CloudEvent internalEvent = event.toImpl();
                if (this.eventDataSerializer != null && internalEvent.getData() != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    eventDataSerializer.serialize(bos, event.getData());
                    internalEvent.setData(Base64.getEncoder().encode(bos.toByteArray()));
                }
                return internalEvent;
            })
            .collectList()
            .flatMap(list -> this.impl.publishCloudEventEventsAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    /**
     * Publishes the given custom events to the set topic or domain.
     * @param events the custom events to publish.
     *
     * @return A {@link Mono} that completes when the events are sent to the service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendCustomEvents(Iterable<Object> events) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        return withContext(context -> sendCustomEvents(events, context));
    }

    Mono<Void> sendCustomEvents(Iterable<Object> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        return Flux.fromIterable(events)
            .collectList()
            .flatMap(list -> this.impl.publishCustomEventEventsAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the EventGrid events to publish.
     *
     * @return the response from the EventGrid service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendEventGridEventsWithResponse(Iterable<EventGridEvent> events) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        return withContext(context -> sendEventGridEventsWithResponse(events, context));
    }

    Mono<Response<Void>> sendEventGridEventsWithResponse(Iterable<EventGridEvent> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        return Flux.fromIterable(events)
            .map(EventGridEvent::toImpl)
            .collectList()
            .flatMap(list -> this.impl.publishEventsWithResponseAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    /**
     * Publishes the given cloud events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the cloud events to publish.
     *
     * @return the response from the EventGrid service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendCloudEventsWithResponse(Iterable<CloudEvent> events) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        return withContext(context -> sendCloudEventsWithResponse(events, context));
    }

    Mono<Response<Void>> sendCloudEventsWithResponse(Iterable<CloudEvent> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        this.addCloudEventTracePlaceHolder(events);
        return Flux.fromIterable(events)
            .map(CloudEvent::toImpl)
            .collectList()
            .flatMap(list -> this.impl.publishCloudEventEventsWithResponseAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    /**
     * Publishes the given custom events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the custom events to publish.
     *
     * @return the response from the EventGrid service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendCustomEventsWithResponse(Iterable<Object> events) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        return withContext(context -> sendCustomEventsWithResponse(events, context));
    }

    Mono<Response<Void>> sendCustomEventsWithResponse(Iterable<Object> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        return Flux.fromIterable(events)
            .collectList()
            .flatMap(list -> this.impl.publishCustomEventEventsWithResponseAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    private void addCloudEventTracePlaceHolder(Iterable<CloudEvent> events) {
        if (TracerProxy.isTracingEnabled()) {
            for (CloudEvent event : events) {
                if (event.getExtensionAttributes() == null ||
                    (event.getExtensionAttributes().get(Constants.TRACE_PARENT) == null &&
                    event.getExtensionAttributes().get(Constants.TRACE_STATE) == null)) {

                    event.addExtensionAttribute(Constants.TRACE_PARENT, Constants.TRACE_PARENT_PLACEHOLDER_UUID);
                    event.addExtensionAttribute(Constants.TRACE_STATE, Constants.TRACE_STATE_PLACEHOLDER_UUID);
                }
            }
        }
    }
}
