// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

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

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. Note that this is simply a synchronous convenience layer over the
 * {@link EventGridPublisherAsyncClient}, which has more efficient asynchronous functionality and is recommended.
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class)
public final class EventGridPublisherClient {

    EventGridPublisherAsyncClient asyncClient;
    private static final ClientLogger logger = new ClientLogger(EventGridPublisherClient.class);

    EventGridPublisherClient(EventGridPublisherAsyncClient client) {
        this.asyncClient = client;
    }

    /**
     * Get the service version of the Rest API.
     * @return the Service version of the rest API
     */
    public EventGridServiceVersion getServiceVersion() {
        return asyncClient.getServiceVersion();
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain.
     * @param events the EventGrid events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendEvents(Iterable<EventGridEvent> events) {
        asyncClient.sendEvents(events, Context.NONE).block();
    }

    /**
     * Publishes the given cloud events to the given topic or domain.
     * @param events the cloud events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCloudEvents(Iterable<CloudEvent> events) {
        asyncClient.sendCloudEvents(events, Context.NONE).block();
    }

    /**
     * Publishes the given custom events to the given topic or domain.
     * @param events the custom events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCustomEvents(Iterable<Object> events) {
        asyncClient.sendCustomEvents(events, Context.NONE).block();
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain and gives the response issued by EventGrid.
     * @param events  the EventGrid events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendEventsWithResponse(Iterable<EventGridEvent> events, Context context) {
        return asyncClient.sendEventsWithResponse(events, context).block();
    }

    /**
     * Publishes the given cloud events to the given topic or domain and gives the response issued by EventGrid.
     * @param events  the cloud events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendCloudEventsWithResponse(Iterable<CloudEvent> events, Context context) {
        return asyncClient.sendCloudEventsWithResponse(events, context).block();
    }

    /**
     * Publishes the given custom events to the given topic or domain and gives the response issued by EventGrid.
     * @param events  the custom events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendCustomEventsWithResponse(Iterable<Object> events, Context context) {
        return asyncClient.sendCustomEventsWithResponse(events, context).block();
    }

    /**
     * Generate a shared access signature to provide time-limited authentication for requests to the Event Grid
     * service.
     * @param endpoint       the endpoint of the Event Grid topic or domain.
     * @param expirationTime the time in which the signature should expire, no longer providing authentication.
     * @param keyCredential  the access key obtained from the Event Grid topic or domain.
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link AzureSasCredential}.
     */
    public static String buildSas(String endpoint, OffsetDateTime expirationTime, AzureKeyCredential keyCredential) {
        // TODO: To discuss. option 2: instance method instead of static. option 3: in a separate class. Refer to other
        //  libraries.
        try {
            String resKey = "r";
            String expKey = "e";
            String signKey = "s";

            Charset charset = StandardCharsets.UTF_8;
            String encodedResource = URLEncoder.encode(endpoint, charset.name());
            String encodedExpiration = URLEncoder.encode(expirationTime.atZoneSameInstant(ZoneOffset.UTC).format(
                DateTimeFormatter.ofPattern("M/d/yyyy h:m:s a")),
                charset.name());

            String unsignedSas = String.format("%s=%s&%s=%s", resKey, encodedResource, expKey, encodedExpiration);

            Mac hmac = Mac.getInstance("hmacSHA256");
            hmac.init(new SecretKeySpec(Base64.getDecoder().decode(keyCredential.getKey()), "hmacSHA256"));
            String signature = new String(Base64.getEncoder().encode(
                hmac.doFinal(unsignedSas.getBytes(charset))),
                charset);

            String encodedSignature = URLEncoder.encode(signature, charset.name());

            return String.format("%s&%s=%s", unsignedSas, signKey, encodedSignature);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }
}
