// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.UpdatedMessage;

import java.time.Duration;

final class MessageIdClient {
    private final MessageIdAsyncClient client;

    MessageIdClient(MessageIdAsyncClient client) {
        this.client = client;
    }

    public static MessageIdClientBuilder builder() {
        return new MessageIdClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public Response<UpdatedMessage> update(String messageText, String popReceipt, int visibilityTimeout, Duration timeout, Context context) {
        return client.update(messageText, popReceipt, visibilityTimeout, timeout, context).block();
    }

    public VoidResponse delete(String popReceipt, Duration timeout, Context context) {
        return client.delete(popReceipt, timeout, context).block();
    }
}
