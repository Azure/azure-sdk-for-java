// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.util.Context;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;

import java.util.List;
import java.util.Map;

public final class QueueClient {
    private final QueueRawClient client;

    public static QueueClientBuilder builder() {
        return new QueueClientBuilder();
    }

    QueueClient(QueueRawClient client) {
        this.client = client;
    }

    public QueueRawClient getRawClient() {
        return client;
    }

    public MessagesClient getMessagesClient() {
        return new MessagesClient(client.getMessagesClient());
    }

    public void create() {
        client.create(null, Context.NONE);
    }

    public void delete() {
        client.delete(null, Context.NONE);
    }

    public QueueProperties getProperties() {
        return client.getProperties(null, Context.NONE).value();
    }

    public void setMetadata(Map<String, String> metadata) {
        client.setMetadata(metadata, null, Context.NONE);
    }

    public List<SignedIdentifier> getAccessPolicy() {
        return client.getAccessPolicy(null, Context.NONE);
    }

    public void setAccessPolicy(List<SignedIdentifier> permissions) {
        client.setAccessPolicy(permissions, null, Context.NONE);
    }
}
