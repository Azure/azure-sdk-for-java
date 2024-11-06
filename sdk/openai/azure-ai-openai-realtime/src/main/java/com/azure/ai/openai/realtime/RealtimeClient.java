// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.EventHandlerCollection;
import com.azure.ai.openai.realtime.models.RealtimeClientEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventType;
import com.azure.core.annotation.ServiceClient;
import reactor.core.scheduler.Schedulers;

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
