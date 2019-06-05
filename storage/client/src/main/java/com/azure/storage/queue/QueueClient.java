// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;

final class QueueClient {
    private final QueueAsyncClient client;

    QueueClient(QueueAsyncClient client) {
        this.client = client;
    }

    public static QueueClientBuilder builder() {
        return new QueueClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public MessagesClient getMessagesClient() {
        return new MessagesClient(this.client.getMessagesAsyncClient());
    }

    public VoidResponse create(Map<String, String> metadata, Duration timeout) {
        return client.create(metadata, timeout, Context.NONE).block();
    }

    public VoidResponse delete(Duration timeout) {
        return client.delete(timeout, Context.NONE).block();
    }

    public Response<QueueProperties> getProperties(Duration timeout) {
        return client.getProperties(timeout, Context.NONE).block();
    }

    public VoidResponse setMetadata(Map<String, String> metadata, Duration timeout) {
        return client.setMetadata(metadata, timeout, Context.NONE).block();
    }

    public Iterable<SignedIdentifier> getAccessPolicy(Duration timeout) {
        return client.getAccessPolicy(timeout, Context.NONE).toIterable();
    }

    public VoidResponse setAccessPolicy(List<SignedIdentifier> permissions, Duration timeout) {
        return client.setAccessPolicy(permissions, timeout, Context.NONE).block();
    }
}
