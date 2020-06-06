// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.servicebus.models.QueueDescription;
import com.azure.messaging.servicebus.models.QueueRuntimeInfo;

import java.time.Duration;
import java.util.Objects;

/**
 * A <b>synchronous</b> client for managing a Service Bus namespace.
 *
 * @see ServiceBusManagementAsyncClient ServiceBusManagementAsyncClient for an asynchronous client.
 */
@ServiceClient(builder = ServiceBusManagementClientBuilder.class)
public final class ServiceBusManagementClient {
    private final ServiceBusManagementAsyncClient asyncClient;

    /**
     * Creates a new instance with the given client.
     *
     * @param asyncClient Asynchronous client to perform management calls through.
     */
    ServiceBusManagementClient(ServiceBusManagementAsyncClient asyncClient) {
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
    }

    /**
     * Creates a queue the {@link QueueDescription}.
     *
     * @param queue Information about the queue to create.
     *
     * @return The created queue.
     * @throws NullPointerException if {@code queue} is null.
     * @throws IllegalArgumentException if {@link QueueDescription#getName() queue.getName()} is null or an empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueDescription createQueue(QueueDescription queue) {
        return asyncClient.createQueue(queue).block();
    }

    /**
     * Creates a queue and returns the created queue in addition to the HTTP response.
     *
     * @param queue The queue to create.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The created queue in addition to the HTTP response.
     * @throws NullPointerException if {@code queue} is null.
     * @throws IllegalArgumentException if {@link QueueDescription#getName() queue.getName()} is null or an empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueDescription> createQueueWithResponse(QueueDescription queue, Context context) {
        return asyncClient.createQueueWithResponse(queue, context).block();
    }

    /**
     * Deletes a queue the matching {@code queueName}.
     *
     * @param queueName Name of queue to delete.
     *
     * @throws NullPointerException if {@code queueName} is null.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteQueue(String queueName) {
        asyncClient.deleteQueue(queueName).block();
    }

    /**
     * Deletes a queue the matching {@code queueName} and returns the HTTP response.
     *
     * @param queueName Name of queue to delete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response when the queue is successfully deleted.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String queueName, Context context) {
        return asyncClient.deleteQueueWithResponse(queueName, context).block();
    }

    /**
     * Gets information about the queue.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return Information about the queue.
     * @throws NullPointerException if {@code queueName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueDescription getQueue(String queueName) {
        return asyncClient.getQueue(queueName).block();
    }

    /**
     * Gets information about the queue along with its HTTP response.
     *
     * @param queueName Name of queue to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Information about the queue and the associated HTTP response.
     * @throws NullPointerException if {@code queueName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueDescription> getQueueWithResponse(String queueName, Context context) {
        return asyncClient.getQueueWithResponse(queueName, context).block();
    }

    /**
     * Gets runtime information about the queue.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return Runtime information about the queue.
     * @throws NullPointerException if {@code queueName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueRuntimeInfo getQueueRuntimeInfo(String queueName) {
        return asyncClient.getQueueRuntimeInfo(queueName).block();
    }

    /**
     * Gets runtime information about the queue along with its HTTP response.
     *
     * @param queueName Name of queue to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Runtime information about the queue and the associated HTTP response.
     * @throws NullPointerException if {@code queueName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueRuntimeInfo> getQueueRuntimeInfoWithResponse(String queueName, Context context) {
        return asyncClient.getQueueRuntimeInfoWithResponse(queueName, context).block();
    }

    /**
     * Fetches all the queues in the Service Bus namespace.
     *
     * @return A PagedIterable of {@link QueueDescription queues} in the Service Bus namespace.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<QueueDescription> listQueues() {
        return new PagedIterable<>(asyncClient.listQueues());
    }

    /**
     * Fetches all the queues in the Service Bus namespace.
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A PagedIterable of {@link QueueDescription queues} in the Service Bus namespace.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<QueueDescription> listQueues(Context context) {
        final PagedFlux<QueueDescription> pagedFlux = new PagedFlux<>(
            () -> asyncClient.listQueuesFirstPage(context),
            continuationToken -> asyncClient.listQueuesNextPage(continuationToken, context));

        return new PagedIterable<>(pagedFlux);
    }

    /**
     * Updates a queue with the given {@link QueueDescription}. The {@link QueueDescription} must be fully populated as
     * all of the properties are replaced.
     *
     * The suggested flow is:
     * <ol>
     *     <li>{@link #getQueue(String) Get queue description.}</li>
     *     <li>Update the required elements.</li>
     *     <li>Pass the updated description into this method.</li>
     * </ol>
     *
     * <p>
     * There are a subset of properties that can be updated. They are:
     * <ul>
     * <li>{@link QueueDescription#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link QueueDescription#setLockDuration(Duration) LockDuration}</li>
     * <li>{@link QueueDescription#setDuplicateDetectionHistoryTimeWindow(Duration) DuplicateDetectionHistoryTimeWindow}
     * </li>
     * <li>{@link QueueDescription#setMaxDeliveryCount(Integer) MaxDeliveryCount}</li>
     * </ul>
     *
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-queue">Update Queue</a>
     * @param queue Information about the queue to update.
     *
     * @return The updated queue.
     * @throws NullPointerException if {@code queue} is null.
     * @throws IllegalArgumentException if {@link QueueDescription#getName() queue.getName()} is null or an empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueDescription updateQueue(QueueDescription queue) {
        return asyncClient.updateQueue(queue).block();
    }

    /**
     * Updates a queue with the given {@link QueueDescription}. The {@link QueueDescription} must be fully populated as
     * all of the properties are replaced.
     *
     * The suggested flow is:
     * <ol>
     *     <li>{@link #getQueue(String) Get queue description.}</li>
     *     <li>Update the required elements.</li>
     *     <li>Pass the updated description into this method.</li>
     * </ol>
     *
     * <p>
     * There are a subset of properties that can be updated. They are:
     * <ul>
     * <li>{@link QueueDescription#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link QueueDescription#setLockDuration(Duration) LockDuration}</li>
     * <li>{@link QueueDescription#setDuplicateDetectionHistoryTimeWindow(Duration) DuplicateDetectionHistoryTimeWindow}
     * </li>
     * <li>{@link QueueDescription#setMaxDeliveryCount(Integer) MaxDeliveryCount}</li>
     * </ul>
     *
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-queue">Update Queue</a>
     * @param queue The queue to update.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The updated queue with its HTTP response.
     * @throws NullPointerException if {@code queue} is null.
     * @throws IllegalArgumentException if {@link QueueDescription#getName() queue.getName()} is null or an empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueDescription> updateQueueWithResponse(QueueDescription queue, Context context) {
        return asyncClient.updateQueueWithResponse(queue, context).block();
    }
}
