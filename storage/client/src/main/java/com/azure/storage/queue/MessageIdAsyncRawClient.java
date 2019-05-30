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

final class MessageIdAsyncRawClient {
    private final String messageId;
    private final AzureQueueStorageImpl generateClient;

    MessageIdAsyncRawClient(String messageId, AzureQueueStorageImpl generateClient) {
        this.messageId = messageId;
        this.generateClient = generateClient;
    }

    MessageIdAsyncRawClient(URL endpoint, HttpPipeline httpPipeline, String messageId) {
        this.messageId = messageId;
        this.generateClient = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public Mono<Response<UpdatedMessage>> update(QueueMessage queueMessage, String popReceipt, int visibilityTimeout, Context context) {
        return update(queueMessage, popReceipt, visibilityTimeout, null, context);
    }

    Mono<Response<UpdatedMessage>> update(QueueMessage queueMessage, String popReceipt, int visibilityTimeout, Duration timeout, Context context) {
        Integer timeoutInSeconds = (timeout == null) ? null : (int) timeout.getSeconds();
        return generateClient.messageIds().updateWithRestResponseAsync(queueMessage, popReceipt, visibilityTimeout, timeoutInSeconds, null, context)
            .map(this::getUpdatedMessageResponse);
    }

    public Mono<VoidResponse> delete(String popReceipt, Context context) {
        return delete(popReceipt, null, context);
    }

    Mono<VoidResponse> delete(String popReceipt, Duration timeout, Context context) {
        Integer timeoutInSeconds = (timeout == null) ? null : (int) timeout.getSeconds();
        return generateClient.messageIds().deleteWithRestResponseAsync(popReceipt, timeoutInSeconds, null, context)
            .map(VoidResponse::new);
    }

    private Response<UpdatedMessage> getUpdatedMessageResponse(MessageIdsUpdateResponse response) {
        MessageIdUpdateHeaders updateHeaders = response.deserializedHeaders();
        UpdatedMessage updatedMessage = new UpdatedMessage(updateHeaders.popReceipt(), updateHeaders.timeNextVisible());
        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), updatedMessage);
    }
}
