// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import com.azure.messaging.webpubsub.client.models.ConnectedEvent;
import com.azure.messaging.webpubsub.client.models.DisconnectedEvent;
import com.azure.messaging.webpubsub.client.models.GroupMessageEvent;
import com.azure.messaging.webpubsub.client.models.SendEventOptions;
import com.azure.messaging.webpubsub.client.models.SendToGroupOptions;
import com.azure.messaging.webpubsub.client.models.ServerMessageEvent;
import com.azure.messaging.webpubsub.client.models.StoppedEvent;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;

@ServiceClient(builder = WebPubSubClientBuilder.class)
public class WebPubSubClient implements AutoCloseable {

    private final WebPubSubAsyncClient client;

    WebPubSubClient(WebPubSubAsyncClient client) {
        this.client = client;
    }

    public String getConnectionId() {
        return this.client.getConnectionId();
    }

    public void start() {
        client.start().block();
    }

    public void stop() {
        client.stop().block();
    }

    @Override
    public void close() {
        client.closeAsync().block();
    }

    public WebPubSubResult joinGroup(String group) {
        return client.joinGroup(group).block();
    }

    public WebPubSubResult joinGroup(String group, long ackId) {
        return client.joinGroup(group, ackId).block();
    }

    public WebPubSubResult leaveGroup(String group) {
        return client.leaveGroup(group).block();
    }

    public WebPubSubResult leaveGroup(String group, long ackId) {
        return client.leaveGroup(group, ackId).block();
    }

    public WebPubSubResult sendToGroup(String group, BinaryData content, WebPubSubDataType dataType) {
        return client.sendToGroup(group, content, dataType).block();
    }

    public WebPubSubResult sendToGroup(String group, BinaryData content, WebPubSubDataType dataType,
                                       SendToGroupOptions options) {
        return client.sendToGroup(group, content, dataType, options).block();
    }

    public WebPubSubResult sendEvent(String eventName, BinaryData content, WebPubSubDataType dataType) {
        return client.sendEvent(eventName, content, dataType).block();
    }

    public WebPubSubResult sendEvent(String eventName, BinaryData content, WebPubSubDataType dataType,
                                           SendEventOptions options) {
        return client.sendEvent(eventName, content, dataType, options).block();
    }

    public IterableStream<GroupMessageEvent> receiveGroupMessageEvents() {
        return new IterableStream<>(client.receiveGroupMessageEvents());
    }

    public IterableStream<ServerMessageEvent> receiveServerMessageEvents() {
        return new IterableStream<>(client.receiveServerMessageEvents());
    }

    public IterableStream<ConnectedEvent> receiveConnectedEvents() {
        return new IterableStream<>(client.receiveConnectedEvents());
    }

    public IterableStream<DisconnectedEvent> receiveDisconnectedEvents() {
        return new IterableStream<>(client.receiveDisconnectedEvents());
    }

    public IterableStream<StoppedEvent> receiveStoppedEvents() {
        return new IterableStream<>(client.receiveStoppedEvents());
    }

    WebPubSubClientState getClientState() {
        return this.client.getClientState();
    }
}
