// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;

public class WebPubSubClient implements AutoCloseable {

    private final WebPubSubAsyncClient client;

    WebPubSubClient(WebPubSubAsyncClient client) {
        this.client = client;
    }

    public void start() {
        client.start().block();
    }

    public void stop() {
        client.stop().block();
    }

    @Override
    public void close() {
        client.close().block();
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

    public WebPubSubResult sendMessageToGroup(String group, BinaryData content, WebPubSubDataType dataType) {
        return client.sendMessageToGroup(group, content, dataType).block();
    }

    public WebPubSubResult sendMessageToGroup(String group, BinaryData content, WebPubSubDataType dataType,
                                              long ackId, boolean noEcho, boolean fireAndForget) {
        return client.sendMessageToGroup(group, content, dataType, ackId, noEcho, fireAndForget).block();
    }

    public IterableStream<GroupDataMessage> receiveGroupMessages() {
        return new IterableStream<>(client.receiveGroupMessages());
    }

    public IterableStream<ConnectedEvent> receiveConnectedEvents() {
        return new IterableStream<>(client.receiveConnectedEvents());
    }

    public IterableStream<DisconnectedEvent> receiveDisconnectedEvents() {
        return new IterableStream<>(client.receiveDisconnectedEvents());
    }
}
