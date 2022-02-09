// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.models.CloudEvent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.TracerProxy;
import com.azure.messaging.eventgrid.implementation.Constants;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImpl;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImplBuilder;
import com.fasterxml.jackson.databind.util.RawValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * A service client that publishes events to an EventGrid topic or domain asynchronously.
 * Use {@link EventGridPublisherClientBuilder} to create an instance of this client.
 *
 * <p><strong>Create EventGridPublisherAsyncClient for CloudEvent Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCloudEventClient -->
 * <pre>
 * &#47;&#47; Create a client to send events of CloudEvent schema &#40;com.azure.core.models.CloudEvent&#41;
 * EventGridPublisherAsyncClient&lt;CloudEvent&gt; cloudEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT&quot;&#41;&#41;  &#47;&#47; make sure it accepts CloudEvent
 *     .credential&#40;new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_CLOUDEVENT_KEY&quot;&#41;&#41;&#41;
 *     .buildCloudEventPublisherAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCloudEventClient -->
 *
 * <p><strong>Send CloudEvent Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCloudEvent -->
 * <pre>
 * &#47;&#47; Create a com.azure.models.CloudEvent.
 * User user = new User&#40;&quot;Stephen&quot;, &quot;James&quot;&#41;;
 * CloudEvent cloudEventDataObject = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromObject&#40;user&#41;, CloudEventDataFormat.JSON, &quot;application&#47;json&quot;&#41;;
 *
 * &#47;&#47; Send a single CloudEvent
 * cloudEventPublisherClient.sendEvent&#40;cloudEventDataObject&#41;.block&#40;&#41;;
 *
 * &#47;&#47; Send a list of CloudEvents to the EventGrid service altogether.
 * &#47;&#47; This has better performance than sending one by one.
 * cloudEventPublisherClient.sendEvents&#40;Arrays.asList&#40;
 *     cloudEventDataObject
 *     &#47;&#47; add more CloudEvents objects
 * &#41;&#41;.block&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCloudEvent -->
 *
 * <p><strong>Create EventGridPublisherAsyncClient for EventGridEvent Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateEventGridEventClient -->
 * <pre>
 * &#47;&#47; Create a client to send events of EventGridEvent schema
 * EventGridPublisherAsyncClient&lt;EventGridEvent&gt; eventGridEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_ENDPOINT&quot;&#41;&#41;  &#47;&#47; make sure it accepts EventGridEvent
 *     .credential&#40;new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_KEY&quot;&#41;&#41;&#41;
 *     .buildEventGridEventPublisherAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateEventGridEventClient -->
 *
 * <p><strong>Send EventGridEvent Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendEventGridEvent -->
 * <pre>
 * &#47;&#47; Create an EventGridEvent
 * User user = new User&#40;&quot;John&quot;, &quot;James&quot;&#41;;
 * EventGridEvent eventGridEvent = new EventGridEvent&#40;&quot;&#47;EventGridEvents&#47;example&#47;source&quot;,
 *     &quot;Example.EventType&quot;, BinaryData.fromObject&#40;user&#41;, &quot;0.1&quot;&#41;;
 *
 * &#47;&#47; Send a single EventGridEvent
 * eventGridEventPublisherClient.sendEvent&#40;eventGridEvent&#41;.block&#40;&#41;;
 *
 * &#47;&#47; Send a list of EventGridEvents to the EventGrid service altogether.
 * &#47;&#47; This has better performance than sending one by one.
 * eventGridEventPublisherClient.sendEvents&#40;Arrays.asList&#40;
 *     eventGridEvent
 *     &#47;&#47; add more EventGridEvents objects
 * &#41;&#41;.block&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendEventGridEvent -->
 *
 * <p><strong>Create EventGridPublisherAsyncClient for Custom Event Schema Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCustomEventClient -->
 * <pre>
 * &#47;&#47; Create a client to send events of custom event
 * EventGridPublisherAsyncClient&lt;BinaryData&gt; customEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_CUSTOM_EVENT_ENDPOINT&quot;&#41;&#41;  &#47;&#47; make sure it accepts custom events
 *     .credential&#40;new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_CUSTOM_EVENT_KEY&quot;&#41;&#41;&#41;
 *     .buildCustomEventPublisherAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCustomEventClient -->
 *
 * <p><strong>Send Custom Event Schema Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCustomEvent -->
 * <pre>
 * &#47;&#47; Create an custom event object &#40;both POJO and Map work&#41;
 * Map&lt;String, Object&gt; customEvent = new HashMap&lt;String, Object&gt;&#40;&#41; &#123;
 *     &#123;
 *         put&#40;&quot;id&quot;, UUID.randomUUID&#40;&#41;.toString&#40;&#41;&#41;;
 *         put&#40;&quot;subject&quot;, &quot;Test&quot;&#41;;
 *         put&#40;&quot;foo&quot;, &quot;bar&quot;&#41;;
 *         put&#40;&quot;type&quot;, &quot;Microsoft.MockPublisher.TestEvent&quot;&#41;;
 *         put&#40;&quot;data&quot;, 100.0&#41;;
 *         put&#40;&quot;dataVersion&quot;, &quot;0.1&quot;&#41;;
 *     &#125;
 * &#125;;
 *
 * &#47;&#47; Send a single custom event
 * customEventPublisherClient.sendEvent&#40;BinaryData.fromObject&#40;customEvent&#41;&#41;.block&#40;&#41;;
 *
 * &#47;&#47; Send a list of EventGridEvents to the EventGrid service altogether.
 * &#47;&#47; This has better performance than sending one by one.
 * customEventPublisherClient.sendEvents&#40;Arrays.asList&#40;
 *     BinaryData.fromObject&#40;customEvent&#41;
 *     &#47;&#47; add more custom events in BinaryData
 * &#41;&#41;.block&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCustomEvent -->
 *
 * @see EventGridEvent
 * @see com.azure.core.models.CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class, isAsync = true)
public final class EventGridPublisherAsyncClient<T> {

    private final String hostname;

    private final EventGridPublisherClientImpl impl;

    private final ClientLogger logger = new ClientLogger(EventGridPublisherAsyncClient.class);

    private final Class<T> eventClass;

    private static final DateTimeFormatter SAS_DATE_TIME_FORMATER = DateTimeFormatter.ofPattern("M/d/yyyy h:m:s a");
    private static final String HMAC_SHA256 = "hmacSHA256";
    private static final String API_VERSION = "api-version";

    private static final ClientLogger LOGGER = new ClientLogger(EventGridPublisherAsyncClient.class);

    EventGridPublisherAsyncClient(HttpPipeline pipeline, String hostname, EventGridServiceVersion serviceVersion,
        Class<T> eventClass) {
        this.impl = new EventGridPublisherClientImplBuilder()
            .pipeline(pipeline)
            .apiVersion(serviceVersion.getVersion())
            .buildClient();
        this.hostname = hostname;
        this.eventClass = eventClass;
    }

    /**
     * Generate a shared access signature to provide time-limited authentication for requests to the Event Grid
     * service with the latest Event Grid service API defined in {@link EventGridServiceVersion#getLatest()}.
     * @param endpoint the endpoint of the Event Grid topic or domain.
     * @param expirationTime the time in which the signature should expire, no longer providing authentication.
     * @param keyCredential the access key obtained from the Event Grid topic or domain.
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link AzureSasCredential}.
     *
     * @throws NullPointerException if endpoint, keyCredential or expirationTime is {@code null}.
     * @throws RuntimeException if java security doesn't have algorithm "hmacSHA256".
     */
    public static String generateSas(String endpoint, AzureKeyCredential keyCredential, OffsetDateTime expirationTime) {
        return generateSas(endpoint, keyCredential, expirationTime, EventGridServiceVersion.getLatest());
    }

    /**
     * Generate a shared access signature to provide time-limited authentication for requests to the Event Grid
     * service.
     * @param endpoint the endpoint of the Event Grid topic or domain.
     * @param expirationTime the time in which the signature should expire, no longer providing authentication.
     * @param keyCredential the access key obtained from the Event Grid topic or domain.
     * @param apiVersion the EventGrid service api version defined in {@link EventGridServiceVersion}
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link AzureSasCredential}.
     *
     * @throws NullPointerException if endpoint, keyCredential or expirationTime is {@code null}.
     * @throws RuntimeException if java security doesn't have algorithm "hmacSHA256".
     */
    public static String generateSas(String endpoint, AzureKeyCredential keyCredential, OffsetDateTime expirationTime,
        EventGridServiceVersion apiVersion) {
        if (Objects.isNull(endpoint)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'endpoint' cannot be null."));
        }
        if (Objects.isNull(keyCredential)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'keyCredetial' cannot be null."));
        }
        if (Objects.isNull(expirationTime)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'expirationTime' cannot be null."));
        }
        try {
            String resKey = "r";
            String expKey = "e";
            String signKey = "s";

            Charset charset = StandardCharsets.UTF_8;
            endpoint = String.format("%s?%s=%s", endpoint, API_VERSION, apiVersion.getVersion());
            String encodedResource = URLEncoder.encode(endpoint, charset.name());
            String encodedExpiration = URLEncoder.encode(expirationTime.atZoneSameInstant(ZoneOffset.UTC).format(
                SAS_DATE_TIME_FORMATER),
                charset.name());

            String unsignedSas = String.format("%s=%s&%s=%s", resKey, encodedResource, expKey, encodedExpiration);

            Mac hmac = Mac.getInstance(HMAC_SHA256);
            hmac.init(new SecretKeySpec(Base64.getDecoder().decode(keyCredential.getKey()), HMAC_SHA256));
            String signature = new String(Base64.getEncoder().encode(
                hmac.doFinal(unsignedSas.getBytes(charset))),
                charset);

            String encodedSignature = URLEncoder.encode(signature, charset.name());

            return String.format("%s&%s=%s", unsignedSas, signKey, encodedSignature);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Publishes the given events to the set topic or domain.
     * @param events the events to publish.
     *
     * @return A {@link Mono} that completes when the events are sent to the service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendEvents(Iterable<T> events) {
        return withContext(context -> sendEvents(events, context));
    }

    @SuppressWarnings("unchecked")
    Mono<Void> sendEvents(Iterable<T> events, Context context) {
        if (this.eventClass == CloudEvent.class) {
            return this.sendCloudEvents((Iterable<CloudEvent>) events, context);
        } else if (this.eventClass == EventGridEvent.class) {
            return this.sendEventGridEvents((Iterable<EventGridEvent>) events, context);
        } else {
            return this.sendCustomEvents((Iterable<BinaryData>) events, context);
        }
    }

    /**
     * Publishes the given events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the events to publish.
     *
     * @return the response from the EventGrid service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendEventsWithResponse(Iterable<T> events) {
        return withContext(context -> this.sendEventsWithResponse(events, context));
    }

    @SuppressWarnings("unchecked")
    Mono<Response<Void>> sendEventsWithResponse(Iterable<T> events, Context context) {
        if (this.eventClass == CloudEvent.class) {
            return this.sendCloudEventsWithResponse((Iterable<CloudEvent>) events, context);
        } else if (this.eventClass == EventGridEvent.class) {
            return this.sendEventGridEventsWithResponse((Iterable<EventGridEvent>) events, context);
        } else {
            return this.sendCustomEventsWithResponse((Iterable<BinaryData>) events, context);
        }
    }

    /**
     * Publishes the given events to the set topic or domain.
     * @param event the event to publish.
     *
     * @return A {@link Mono} that completes when the event is sent to the service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendEvent(T event) {
        List<T> events = Collections.singletonList(event);
        return withContext(context -> sendEvents(events, context));
    }

    Mono<Void> sendEventGridEvents(Iterable<EventGridEvent> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        return Flux.fromIterable(events)
            .map(EventGridEvent::toImpl)
            .collectList()
            .flatMap(list -> this.impl.publishEventsAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    Mono<Void> sendCloudEvents(Iterable<CloudEvent> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        this.addCloudEventTracePlaceHolder(events);
        return Flux.fromIterable(events)
            .collectList()
            .flatMap(list -> this.impl.publishCloudEventEventsAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    Mono<Void> sendCustomEvents(Iterable<BinaryData> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        return Flux.fromIterable(events)
            .map(event -> (Object) new RawValue(event.toString()))
            .collectList()
            .flatMap(list -> this.impl.publishCustomEventEventsAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
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

    Mono<Response<Void>> sendCloudEventsWithResponse(Iterable<CloudEvent> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        this.addCloudEventTracePlaceHolder(events);
        return Flux.fromIterable(events)
            .collectList()
            .flatMap(list -> this.impl.publishCloudEventEventsWithResponseAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    Mono<Response<Void>> sendCustomEventsWithResponse(Iterable<BinaryData> events, Context context) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }
        final Context finalContext = context != null ? context : Context.NONE;
        return Flux.fromIterable(events)
            .map(event -> (Object) new RawValue(event.toString()))
            .collectList()
            .flatMap(list -> this.impl.publishCustomEventEventsWithResponseAsync(this.hostname, list,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, Constants.EVENT_GRID_TRACING_NAMESPACE_VALUE)));
    }

    private void addCloudEventTracePlaceHolder(Iterable<CloudEvent> events) {
        if (TracerProxy.isTracingEnabled()) {
            for (CloudEvent event : events) {
                if (event.getExtensionAttributes() == null
                    || (event.getExtensionAttributes().get(Constants.TRACE_PARENT) == null
                    && event.getExtensionAttributes().get(Constants.TRACE_STATE) == null)) {

                    event.addExtensionAttribute(Constants.TRACE_PARENT, Constants.TRACE_PARENT_PLACEHOLDER_UUID);
                    event.addExtensionAttribute(Constants.TRACE_STATE, Constants.TRACE_STATE_PLACEHOLDER_UUID);
                }
            }
        }
    }
}
