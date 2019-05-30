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

final class MessageIdAsyncRawClient {
    private final AzureQueueStorageImpl generateClient;

    MessageIdAsyncRawClient(AzureQueueStorageImpl generateClient) {
        this.generateClient = generateClient;
    }

    MessageIdAsyncRawClient(URL endpoint, HttpPipeline httpPipeline) {
        this.generateClient = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public Mono<Response<UpdatedMessage>> update(QueueMessage queueMessage, String popReceipt, int visibilityTimeout, Context context) {
        return update(queueMessage, popReceipt, visibilityTimeout, null, context);
    }

    Mono<Response<UpdatedMessage>> update(QueueMessage queueMessage, String popReceipt, int visibilityTimeout, Integer timeout, Context context) {
        return generateClient.messageIds().updateWithRestResponseAsync(queueMessage, popReceipt, visibilityTimeout, timeout, null, context)
            .map(this::getUpdatedMessageResponse);
    }

    public Mono<VoidResponse> delete(String popReceipt, Context context) {
        return delete(popReceipt, null, context);
    }

    Mono<VoidResponse> delete(String popReceipt, Integer timeout, Context context) {
        return generateClient.messageIds().deleteWithRestResponseAsync(popReceipt, timeout, null, context)
            .map(VoidResponse::new);
    }

    private Response<UpdatedMessage> getUpdatedMessageResponse(MessageIdsUpdateResponse response) {
        MessageIdUpdateHeaders updateHeaders = response.deserializedHeaders();
        UpdatedMessage updatedMessage = new UpdatedMessage(updateHeaders.popReceipt(), updateHeaders.timeNextVisible());
        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), updatedMessage);
    }
}
