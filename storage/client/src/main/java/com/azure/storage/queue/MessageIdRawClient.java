package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.QueueMessage;
import com.azure.storage.queue.models.UpdatedMessage;

import java.time.Duration;

final class MessageIdRawClient {
    private final MessageIdAsyncRawClient client;

    MessageIdRawClient(MessageIdAsyncRawClient client) {
        this.client = client;
    }

    public Response<UpdatedMessage> update(QueueMessage queueMessage, String popReceipt, int visibilityTimeout, Duration timeout, Context context) {
        return client.update(queueMessage, popReceipt, visibilityTimeout, (int) timeout.getSeconds(), context).block();
    }

    public VoidResponse delete(String popReceipt, Duration timeout, Context context) {
        return client.delete(popReceipt, (int) timeout.getSeconds(), context).block();
    }
}
