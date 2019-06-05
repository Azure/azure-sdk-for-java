// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.MessageIdUpdateHeaders;
import com.azure.storage.queue.models.MessageIdsUpdateResponse;
import com.azure.storage.queue.models.QueueMessage;
import com.azure.storage.queue.models.UpdatedMessage;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;

public final class MessageIdAsyncClient {
    private final AzureQueueStorageImpl client;

    MessageIdAsyncClient(String messageId, AzureQueueStorageImpl generateClient) {
        this.client = new AzureQueueStorageImpl(generateClient.httpPipeline())
            .withUrl(generateClient.url() + messageId)
            .withVersion(generateClient.version());
    }

    MessageIdAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public static MessageIdAsyncClientBuilder builder() {
        return new MessageIdAsyncClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public Mono<Response<UpdatedMessage>> update(String messageText, String popReceipt, int visibilityTimeout) {
        return update(messageText, popReceipt, visibilityTimeout, null);
    }

    Mono<Response<UpdatedMessage>> update(String messageText, String popReceipt, int visibilityTimeout, Duration timeout) {
        QueueMessage message = new QueueMessage().messageText(messageText);
        if (timeout == null) {
            return client.messageIds().updateWithRestResponseAsync(message, popReceipt, visibilityTimeout, Context.NONE)
                .map(this::getUpdatedMessageResponse);
        } else {
            return client.messageIds().updateWithRestResponseAsync(message, popReceipt, visibilityTimeout, Context.NONE)
                .timeout(timeout)
                .map(this::getUpdatedMessageResponse);
        }
    }

    public Mono<VoidResponse> delete(String popReceipt) {
        return delete(popReceipt, null);
    }

    Mono<VoidResponse> delete(String popReceipt, Duration timeout) {
        if (timeout == null) {
            return client.messageIds().deleteWithRestResponseAsync(popReceipt, Context.NONE)
                .map(VoidResponse::new);
        } else {
            return client.messageIds().deleteWithRestResponseAsync(popReceipt, Context.NONE)
                .timeout(timeout)
                .map(VoidResponse::new);
        }
    }

    private Response<UpdatedMessage> getUpdatedMessageResponse(MessageIdsUpdateResponse response) {
        MessageIdUpdateHeaders updateHeaders = response.deserializedHeaders();
        UpdatedMessage updatedMessage = new UpdatedMessage(updateHeaders.popReceipt(), updateHeaders.timeNextVisible());
        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), updatedMessage);
    }
}
