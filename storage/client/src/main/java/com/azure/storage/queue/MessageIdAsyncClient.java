// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.QueueMessage;
import com.azure.storage.queue.models.UpdatedMessage;
import reactor.core.publisher.Mono;

public final class MessageIdAsyncClient {
    private final MessageIdAsyncRawClient client;

    MessageIdAsyncClient(MessageIdAsyncRawClient client) {
        this.client = client;
    }

    public static MessageIdAsyncClientBuilder builder() {
        return new MessageIdAsyncClientBuilder();
    }

    public MessageIdAsyncRawClient getRawClient() {
        return client;
    }

    public Mono<UpdatedMessage> update(QueueMessage queueMessage, String popReceipt, int visibilityTimeout) {
        return client.update(queueMessage, popReceipt, visibilityTimeout, null, Context.NONE)
            .map(Response::value);
    }

    public Mono<Void> delete(String popReceipt) {
        return client.delete(popReceipt, null, Context.NONE)
            .flatMap(response -> Mono.empty());
    }
}
