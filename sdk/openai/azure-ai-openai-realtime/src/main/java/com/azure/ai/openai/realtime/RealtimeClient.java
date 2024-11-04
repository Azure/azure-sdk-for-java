// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.EventHandlerCollection;
import com.azure.ai.openai.realtime.models.RealtimeClientEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventType;
import com.azure.core.annotation.Generated;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.ai.openai.realtime.implementation.RealtimesImpl;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Consumer;

/**
 * Initializes a new instance of the synchronous OpenAIClient type.
 */
@ServiceClient(builder = RealtimeClientBuilder.class)
public final class RealtimeClient {

    private final RealtimeAsyncClient asyncClient;
    private final EventHandlerCollection eventHandlerCollection = new EventHandlerCollection();


    RealtimeClient(RealtimeAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public synchronized void start() {
        asyncClient.start(() -> {
            this.asyncClient.getServerEvents().publishOn(Schedulers.boundedElastic())
                    .subscribe(event -> eventHandlerCollection.fireEvent(event.getType().toString(), event));
        }).block();
    }

    public void sendMessage(RealtimeClientEvent event) {
        asyncClient.sendMessage(event).block();
    }

    public void addOnSessionCreatedEventHandler (Consumer<RealtimeServerEventSessionCreated> onSessionCreatedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.SESSION_CREATED.toString(), onSessionCreatedEventHandler);
    }

    public void addOnSessionUpdatedEventHandler (Consumer<RealtimeServerEvent> onSessionUpdatedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.SESSION_UPDATED.toString(), onSessionUpdatedEventHandler);
    }
}
