// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.QueuesImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.implementation.models.ServiceBusManagementError;
import com.azure.messaging.servicebus.implementation.models.ServiceBusManagementErrorException;
import com.azure.messaging.servicebus.models.QueueDescription;
import com.azure.messaging.servicebus.models.QueueRuntimeInfo;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * An <b>asynchronous</b> client for managing a Service Bus namespace.
 *
 * @see ServiceBusManagementClient ServiceBusManagementClient for a synchronous client.
 */
@ServiceClient(builder = ServiceBusManagementClientBuilder.class, isAsync = true)
public final class ServiceBusManagementAsyncClient {
    // See https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers
    // for more information on Azure resource provider namespaces.
    private static final String SERVICE_BUS_TRACING_NAMESPACE_VALUE = "Microsoft.ServiceBus";
    private static final String CONTENT_TYPE = "application/xml";

    // Name of the entity type when listing queues.
    private static final String QUEUES_ENTITY_TYPE = "queues";
    private static final int NUMBER_OF_ELEMENTS = 10;

    private final ServiceBusManagementClientImpl managementClient;
    private final QueuesImpl queuesClient;
    private final ClientLogger logger = new ClientLogger(ServiceBusManagementAsyncClient.class);
    private final ServiceBusManagementSerializer serializer;

    /**
     * Creates a new instance with the given management client and serializer.
     *
     * @param managementClient Client to make management calls.
     * @param serializer Serializer to deserialize ATOM XML responses.
     */
    ServiceBusManagementAsyncClient(ServiceBusManagementClientImpl managementClient,
        ServiceBusManagementSerializer serializer) {
        this.managementClient = Objects.requireNonNull(managementClient, "'managementClient' cannot be null.");
        this.queuesClient = managementClient.getQueues();
        this.serializer = serializer;
    }

    /**
     * Creates a queue the {@link QueueDescription}.
     *
     * @param queue Information about the queue to create.
     *
     * @return A Mono that completes with information about the created queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link QueueDescription#getName() queue.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code queue} is null.
     * @throws ResourceExistsException if a queue exists with the same {@link QueueDescription#getName()
     *     queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueDescription> createQueue(QueueDescription queue) {
        return createQueueWithResponse(queue).map(Response::getValue);
    }

    /**
     * Creates a queue and returns the created queue in addition to the HTTP response.
     *
     * @param queue The queue to create.
     *
     * @return A Mono that returns the created queue in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link QueueDescription#getName() queue.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code queue} is null.
     * @throws ResourceExistsException if a queue exists with the same {@link QueueDescription#getName()
     *     queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueDescription>> createQueueWithResponse(QueueDescription queue) {
        return withContext(context -> createQueueWithResponse(queue, context));
    }

    /**
     * Deletes a queue the matching {@code queueName}.
     *
     * @param queueName Name of queue to delete.
     *
     * @return A Mono that completes when the queue is deleted.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-queue">Delete Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteQueue(String queueName) {
        return deleteQueueWithResponse(queueName).then();
    }

    /**
     * Deletes a queue the matching {@code queueName} and returns the HTTP response.
     *
     * @param queueName Name of queue to delete.
     *
     * @return A Mono that completes when the queue is deleted and returns the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-queue">Delete Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteQueueWithResponse(String queueName) {
        return withContext(context -> deleteQueueWithResponse(queueName, context));
    }

    /**
     * Gets information about the queue.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return A Mono that completes with information about the queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueDescription> getQueue(String queueName) {
        return getQueueWithResponse(queueName).map(Response::getValue);
    }

    /**
     * Gets information about the queue along with its HTTP response.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return A Mono that completes with information about the queue and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueDescription>> getQueueWithResponse(String queueName) {
        return withContext(context -> getQueueWithResponse(queueName, context));
    }

    /**
     * Gets runtime information about the queue.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return A Mono that completes with runtime information about the queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueRuntimeInfo> getQueueRuntimeInfo(String queueName) {
        return getQueueWithResponse(queueName).map(response -> new QueueRuntimeInfo(response.getValue()));
    }

    /**
     * Gets runtime information about the queue along with its HTTP response.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return A Mono that completes with runtime information about the queue and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueRuntimeInfo>> getQueueRuntimeInfoWithResponse(String queueName) {
        return withContext(context -> getQueueRuntimeInfoWithResponse(queueName, context));
    }

    /**
     * Fetches all the queues in the Service Bus namespace.
     *
     * @return A Flux of {@link QueueDescription queues} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List Queues, Subscriptions, or
     *     Authorization Rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<QueueDescription> listQueues() {
        return new PagedFlux<>(
            () -> withContext(context -> listQueuesFirstPage(context)),
            token -> withContext(context -> listQueuesNextPage(token, context)));
    }

    /**
     * Updates a queue with the given {@link QueueDescription}. The {@link QueueDescription} must be fully populated as
     * all of the properties are replaced. If a property is not set the service default value is used.
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
     * @param queue Information about the queue to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     *
     * @return A Mono that completes with information about the created queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link QueueDescription#getName() queue.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code queue} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-queue">Update Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueDescription> updateQueue(QueueDescription queue) {
        return updateQueueWithResponse(queue).map(Response::getValue);
    }

    /**
     * Updates a queue with the given {@link QueueDescription}. The {@link QueueDescription} must be fully populated as
     * all of the properties are replaced. If a property is not set the service default value is used.
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
     * @param queue Information about the queue to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     *
     * @return A Mono that returns the updated queue in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link QueueDescription#getName() queue.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code queue} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-queue">Update Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueDescription>> updateQueueWithResponse(QueueDescription queue) {
        return withContext(context -> updateQueueWithResponse(queue, context));
    }

    /**
     * Creates a queue with its context.
     *
     * @param queue Queue to create.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link QueueDescription}.
     */
    Mono<Response<QueueDescription>> createQueueWithResponse(QueueDescription queue, Context context) {
        if (queue == null) {
            return monoError(logger, new NullPointerException("'queue' cannot be null"));
        } else if (queue.getName() == null || queue.getName().isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'queue.getName' cannot be null or empty."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType(CONTENT_TYPE)
            .setQueueDescription(queue);
        final CreateQueueBody createEntity = new CreateQueueBody()
            .setContent(content);

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return queuesClient.putWithResponseAsync(queue.getName(), createEntity, null, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(this::deserializeQueue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a queue with its context.
     *
     * @param queueName Name of queue to delete.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link QueueDescription}.
     */
    Mono<Response<Void>> deleteQueueWithResponse(String queueName, Context context) {
        if (queueName == null) {
            return monoError(logger, new NullPointerException("'queueName' cannot be null"));
        } else if (queueName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'queueName' cannot be empty."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return queuesClient.deleteWithResponseAsync(queueName, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> {
                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), null);
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a queue with its context.
     *
     * @param queueName Name of queue to fetch information for.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the {@link QueueDescription}.
     */
    Mono<Response<QueueRuntimeInfo>> getQueueRuntimeInfoWithResponse(String queueName, Context context) {
        if (queueName == null) {
            return monoError(logger, new NullPointerException("'queueName' cannot be null"));
        } else if (queueName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'queueName' cannot be empty."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return queuesClient.getWithResponseAsync(queueName, true, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> {
                    final Response<QueueDescription> deserializeQueue = deserializeQueue(response);
                    final QueueRuntimeInfo runtimeInfo = deserializeQueue.getValue() != null
                        ? new QueueRuntimeInfo(deserializeQueue.getValue())
                        : null;

                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                        runtimeInfo);
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a queue with its context.
     *
     * @param queueName Name of queue to fetch information for.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the {@link QueueDescription}.
     */
    Mono<Response<QueueDescription>> getQueueWithResponse(String queueName, Context context) {
        if (queueName == null) {
            return monoError(logger, new NullPointerException("'queueName' cannot be null"));
        } else if (queueName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'queueName' cannot be empty."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return queuesClient.getWithResponseAsync(queueName, true, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(this::deserializeQueue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the first page of queues with context.
     *
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with a page of queues.
     */
    Mono<PagedResponse<QueueDescription>> listQueuesFirstPage(Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return listQueues(0, NUMBER_OF_ELEMENTS, withTracing);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets the next page of queues with context.
     *
     * @param continuationToken Number of items to skip in feed.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with a page of queues or empty if there are no items left.
     */
    Mono<PagedResponse<QueueDescription>> listQueuesNextPage(String continuationToken, Context context) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        try {
            final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);
            final int skip = Integer.parseInt(continuationToken);

            return listQueues(skip, NUMBER_OF_ELEMENTS, withTracing);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Updates a queue with its context.
     *
     * @param queue Information about the queue to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the updated {@link QueueDescription}.
     */
    Mono<Response<QueueDescription>> updateQueueWithResponse(QueueDescription queue, Context context) {
        if (queue == null) {
            return monoError(logger, new NullPointerException("'queue' cannot be null"));
        } else if (queue.getName() == null || queue.getName().isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'queue.getName' cannot be null or empty."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType(CONTENT_TYPE)
            .setQueueDescription(queue);
        final CreateQueueBody createEntity = new CreateQueueBody()
            .setContent(content);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return queuesClient.putWithResponseAsync(queue.getName(), createEntity, "*", withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> deserializeQueue(response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private <T> T deserialize(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }

        final String contents = String.valueOf(object);
        if (contents.isEmpty()) {
            return null;
        }

        try {
            return serializer.deserialize(contents, clazz);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(String.format(
                "Exception while deserializing. Body: [%s]. Class: %s", contents, clazz), e));
        }
    }

    /**
     * Given an HTTP response, will deserialize it into a strongly typed Response object.
     *
     * @param response HTTP response to deserialize response body from.
     * @param clazz Class to deserialize response type into.
     * @param <T> Class type to deserialize response into.
     *
     * @return A Response with a strongly typed response value.
     */
    private <T> Response<T> deserialize(Response<Object> response, Class<T> clazz) {
        final T deserialize = deserialize(response.getValue(), clazz);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            deserialize);
    }

    /**
     * Converts a Response into its corresponding {@link QueueDescriptionEntry} then mapped into {@link
     * QueueDescription}.
     *
     * @param response HTTP Response to deserialize.
     *
     * @return The corresponding HTTP response with convenience properties set.
     */
    private Response<QueueDescription> deserializeQueue(Response<Object> response) {
        final QueueDescriptionEntry entry = deserialize(response.getValue(), QueueDescriptionEntry.class);

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final QueueDescription result = entry.getContent().getQueueDescription()
            .setName(entry.getTitle().getTitle());

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), result);
    }

    /**
     * Creates a {@link FeedPage} given the elements and a set of response links to get the next link from.
     *
     * @param entities Entities in the feed.
     * @param responseLinks Links returned from the feed.
     * @param <TResult> Type of Service Bus entities in page.
     *
     * @return A {@link FeedPage} indicating whether this can be continued or not.
     * @throws MalformedURLException if the "next" page link does not contain a well-formed URL.
     */
    private <TResult, TFeed> FeedPage<TResult> extractPage(Response<TFeed> response, List<TResult> entities,
        List<ResponseLink> responseLinks)
        throws MalformedURLException, UnsupportedEncodingException {
        final Optional<ResponseLink> nextLink = responseLinks.stream()
            .filter(link -> link.getRel().equalsIgnoreCase("next"))
            .findFirst();

        if (!nextLink.isPresent()) {
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
        }

        final URL url = new URL(nextLink.get().getHref());
        final String decode = URLDecoder.decode(url.getQuery(), StandardCharsets.UTF_8.name());
        final Optional<Integer> skipParameter = Arrays.stream(decode.split("&amp;|&"))
            .map(part -> part.split("=", 2))
            .filter(parts -> parts[0].equalsIgnoreCase("$skip") && parts.length == 2)
            .map(parts -> Integer.valueOf(parts[1]))
            .findFirst();

        if (skipParameter.isPresent()) {
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities,
                skipParameter.get());
        } else {
            logger.warning("There should have been a skip parameter for the next page.");
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
        }
    }

    /**
     * Helper method that invokes the service method, extracts the data and translates it to a PagedResponse.
     *
     * @param skip Number of elements to skip.
     * @param top Number of elements to fetch.
     * @param context Context for the query.
     *
     * @return A Mono that completes with a paged response of queues.
     */
    private Mono<PagedResponse<QueueDescription>> listQueues(int skip, int top, Context context) {
        return managementClient.listEntitiesWithResponseAsync(QUEUES_ENTITY_TYPE, skip, top, context)
            .onErrorMap(ServiceBusManagementAsyncClient::mapException)
            .flatMap(response -> {
                final Response<QueueDescriptionFeed> feedResponse = deserialize(response, QueueDescriptionFeed.class);
                final QueueDescriptionFeed feed = feedResponse.getValue();
                if (feed == null) {
                    logger.warning("Could not deserialize QueueDescriptionFeed. skip {}, top: {}", skip, top);
                    return Mono.empty();
                }

                final List<QueueDescription> entities = feed.getEntry().stream()
                    .filter(e -> e.getContent() != null && e.getContent().getQueueDescription() != null)
                    .map(e -> {
                        final String queueName = e.getTitle().getTitle();
                        return e.getContent().getQueueDescription().setName(queueName);
                    })
                    .collect(Collectors.toList());
                try {
                    return Mono.just(extractPage(feedResponse, entities, feed.getLink()));
                } catch (MalformedURLException | UnsupportedEncodingException error) {
                    return Mono.error(new RuntimeException("Could not parse response into FeedPage<QueueDescription>",
                        error));
                }
            });
    }

    /**
     * Maps an exception from the ATOM APIs to its associated {@link HttpResponseException}.
     *
     * @param exception Exception from the ATOM API.
     *
     * @return The corresponding {@link HttpResponseException} or {@code throwable} if it is not an instance of {@link
     *     ServiceBusManagementErrorException}.
     */
    private static Throwable mapException(Throwable exception) {
        if (!(exception instanceof ServiceBusManagementErrorException)) {
            return exception;
        }

        final ServiceBusManagementErrorException managementError = ((ServiceBusManagementErrorException) exception);
        final ServiceBusManagementError error = managementError.getValue();
        switch (error.getCode()) {
            case 401:
                return new ClientAuthenticationException(error.getDetail(), managementError.getResponse(), exception);
            case 404:
                return new ResourceNotFoundException(error.getDetail(), managementError.getResponse(), exception);
            case 409:
                return new ResourceExistsException(error.getDetail(), managementError.getResponse(), exception);
            case 412:
                return new ResourceModifiedException(error.getDetail(), managementError.getResponse(), exception);
            default:
                return new HttpResponseException(error.getDetail(), managementError.getResponse(), exception);
        }
    }

    /**
     * A page of Service Bus entities.
     *
     * @param <T> The entity description from Service Bus.
     */
    private static final class FeedPage<T> implements PagedResponse<T> {
        private final int statusCode;
        private final HttpHeaders header;
        private final HttpRequest request;
        private final IterableStream<T> entries;
        private final String continuationToken;

        /**
         * Creates a page that does not have any more pages.
         *
         * @param entries Items in the page.
         */
        private FeedPage(int statusCode, HttpHeaders header, HttpRequest request, List<T> entries) {
            this.statusCode = statusCode;
            this.header = header;
            this.request = request;
            this.entries = new IterableStream<>(entries);
            this.continuationToken = null;
        }

        /**
         * Creates an instance that has additional pages to fetch.
         *
         * @param entries Items in the page.
         * @param skip Number of elements to "skip".
         */
        private FeedPage(int statusCode, HttpHeaders header, HttpRequest request, List<T> entries, int skip) {
            this.statusCode = statusCode;
            this.header = header;
            this.request = request;
            this.entries = new IterableStream<>(entries);
            this.continuationToken = String.valueOf(skip);
        }

        @Override
        public IterableStream<T> getElements() {
            return entries;
        }

        @Override
        public String getContinuationToken() {
            return continuationToken;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public HttpHeaders getHeaders() {
            return header;
        }

        @Override
        public HttpRequest getRequest() {
            return request;
        }

        @Override
        public void close() {
        }
    }
}
