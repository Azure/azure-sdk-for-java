package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;

final class QueueRawClient {
    private final QueueAsyncRawClient client;

    QueueRawClient(QueueAsyncRawClient client) {
        this.client = client;
    }

    public MessagesRawClient getMessagesClient() {
        return new MessagesRawClient(this.client.getMessagesClient());
    }

    public VoidResponse create(Duration timeout, Context context) {
        return client.create((int) timeout.getSeconds(), context).block();
    }

    public VoidResponse delete(Duration timeout, Context context) {
        return client.delete((int) timeout.getSeconds(), context).block();
    }

    public Response<QueueProperties> getProperties(Duration timeout, Context context) {
        return client.getProperties((int) timeout.getSeconds(), context).block();
    }

    public VoidResponse setMetadata(Map<String, String> metadata, Duration timeout, Context context) {
        return client.setMetadata(metadata, (int) timeout.getSeconds(), context).block();
    }

    public List<SignedIdentifier> getAccessPolicy(Duration timeout, Context context) {
        return client.getAccessPolicy((int) timeout.getSeconds(), context).collectList().block();
    }

    public VoidResponse setAccessPolicy(List<SignedIdentifier> permissions, Duration timeout, Context context) {
        return client.setAccessPolicy(permissions, (int) timeout.getSeconds(), context).block();
    }
}
