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
import com.azure.messaging.servicebus.implementation.EntityHelper;
import com.azure.messaging.servicebus.implementation.EntitysImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.implementation.models.CreateSubscriptionBody;
import com.azure.messaging.servicebus.implementation.models.CreateSubscriptionBodyContent;
import com.azure.messaging.servicebus.implementation.models.CreateTopicBody;
import com.azure.messaging.servicebus.implementation.models.CreateTopicBodyContent;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.implementation.models.ServiceBusManagementError;
import com.azure.messaging.servicebus.implementation.models.ServiceBusManagementErrorException;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.TopicDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.TopicDescriptionFeed;
import com.azure.messaging.servicebus.models.QueueDescription;
import com.azure.messaging.servicebus.models.QueueRuntimeInfo;
import com.azure.messaging.servicebus.models.SubscriptionDescription;
import com.azure.messaging.servicebus.models.SubscriptionRuntimeInfo;
import com.azure.messaging.servicebus.models.TopicDescription;
import com.azure.messaging.servicebus.models.TopicRuntimeInfo;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
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

    // Name of the entity type when listing queues and topics.
    private static final String QUEUES_ENTITY_TYPE = "queues";
    private static final String TOPICS_ENTITY_TYPE = "topics";

    private static final int NUMBER_OF_ELEMENTS = 100;

    private final ServiceBusManagementClientImpl managementClient;
    private final EntitysImpl entityClient;
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
        this.entityClient = managementClient.getEntitys();
        this.serializer = serializer;
    }

    /**
     * Creates a queue with the given name.
     *
     * @param queueName Name of the queue to create.
     *
     * @return A Mono that completes with information about the created queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws ResourceExistsException if a queue exists with the same {@code queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueDescription> createQueue(String queueName) {
        try {
            return createQueue(new QueueDescription(queueName));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a queue with the {@link QueueDescription}.
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
     * Creates a subscription with the given topic and subscription names.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     *
     * @return A Mono that completes with information about the created subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or are empty strings.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionDescription> createSubscription(String topicName, String subscriptionName) {
        try {
            return createSubscription(new SubscriptionDescription(topicName, subscriptionName));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a subscription with the {@link SubscriptionDescription}.
     *
     * @param subscription Information about the subscription to create.
     *
     * @return A Mono that completes with information about the created subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws NullPointerException if {@code subscription} is null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionDescription> createSubscription(SubscriptionDescription subscription) {
        return createSubscriptionWithResponse(subscription).map(Response::getValue);
    }

    /**
     * Creates a queue and returns the created queue in addition to the HTTP response.
     *
     * @param subscription Information about the subscription to create.
     *
     * @return A Mono that returns the created queue in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws NullPointerException if {@code subscription} is null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SubscriptionDescription>> createSubscriptionWithResponse(
        SubscriptionDescription subscription) {
        return withContext(context -> createSubscriptionWithResponse(subscription, context));
    }

    /**
     * Creates a topic with the given name.
     *
     * @param topicName Name of the topic to create.
     *
     * @return A Mono that completes with information about the created topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws ResourceExistsException if a topic exists with the same {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TopicDescription> createTopic(String topicName) {
        try {
            return createTopic(new TopicDescription(topicName));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a topic with the {@link TopicDescription}.
     *
     * @param topic Information about the topic to create.
     *
     * @return A Mono that completes with information about the created topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link TopicDescription#getName() topic.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code topic} is null.
     * @throws ResourceExistsException if a topic exists with the same {@link TopicDescription#getName()
     *     topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TopicDescription> createTopic(TopicDescription topic) {
        return createTopicWithResponse(topic).map(Response::getValue);
    }

    /**
     * Creates a topic and returns the created topic in addition to the HTTP response.
     *
     * @param topic The topic to create.
     *
     * @return A Mono that returns the created topic in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link TopicDescription#getName() topic.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code topic} is null.
     * @throws ResourceExistsException if a topic exists with the same {@link TopicDescription#getName()
     *     topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TopicDescription>> createTopicWithResponse(TopicDescription topic) {
        return withContext(context -> createTopicWithResponse(topic, context));
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
     * Deletes a subscription the matching {@code subscriptionName}.
     *
     * @param topicName Name of topic associated with subscription to delete.
     * @param subscriptionName Name of subscription to delete.
     *
     * @return A Mono that completes when the subscription is deleted.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} is null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-subscription">Delete Subscription</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteSubscription(String topicName, String subscriptionName) {
        return deleteSubscriptionWithResponse(topicName, subscriptionName).then();
    }

    /**
     * Deletes a subscription the matching {@code subscriptionName} and returns the HTTP response.
     *
     * @param topicName Name of topic associated with subscription to delete.
     * @param subscriptionName Name of subscription to delete.
     *
     * @return A Mono that completes when the subscription is deleted and returns the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} is null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-subscription">Delete Subscription</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSubscriptionWithResponse(String topicName, String subscriptionName) {
        return withContext(context -> deleteSubscriptionWithResponse(topicName, subscriptionName, context));
    }

    /**
     * Deletes a topic the matching {@code topicName}.
     *
     * @param topicName Name of topic to delete.
     *
     * @return A Mono that completes when the topic is deleted.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-topic">Delete Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTopic(String topicName) {
        return deleteTopicWithResponse(topicName).then();
    }

    /**
     * Deletes a topic the matching {@code topicName} and returns the HTTP response.
     *
     * @param topicName Name of topic to delete.
     *
     * @return A Mono that completes when the topic is deleted and returns the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-topic">Delete Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTopicWithResponse(String topicName) {
        return withContext(context -> deleteTopicWithResponse(topicName, context));
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
        return withContext(context -> getQueueWithResponse(queueName, context, Function.identity()));
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
        return getQueueRuntimeInfoWithResponse(queueName).map(response -> response.getValue());
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
        return withContext(context -> getQueueWithResponse(queueName, context, QueueRuntimeInfo::new));
    }

    /**
     * Gets information about the queue.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return A Mono that completes with information about the subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist in the {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionDescription> getSubscription(String topicName, String subscriptionName) {
        return getSubscriptionWithResponse(topicName, subscriptionName).map(Response::getValue);
    }

    /**
     * Gets information about the subscription along with its HTTP response.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return A Mono that completes with information about the subscription and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SubscriptionDescription>> getSubscriptionWithResponse(String topicName,
        String subscriptionName) {
        return withContext(context -> getSubscriptionWithResponse(topicName, subscriptionName, context,
            Function.identity()));
    }

    /**
     * Gets runtime information about the queue.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return A Mono that completes with runtime information about the queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code subscriptionName} is null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionRuntimeInfo> getSubscriptionRuntimeInfo(String topicName, String subscriptionName) {
        return getSubscriptionRuntimeInfoWithResponse(topicName, subscriptionName)
            .map(response -> response.getValue());
    }

    /**
     * Gets runtime information about the queue.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return A Mono that completes with runtime information about the queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code subscriptionName} is null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SubscriptionRuntimeInfo>> getSubscriptionRuntimeInfoWithResponse(String topicName,
        String subscriptionName) {

        return withContext(context -> getSubscriptionWithResponse(topicName, subscriptionName, context,
            SubscriptionRuntimeInfo::new));
    }

    /**
     * Gets information about the topic.
     *
     * @param topicName Name of topic to get information about.
     *
     * @return A Mono that completes with information about the topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TopicDescription> getTopic(String topicName) {
        return getTopicWithResponse(topicName).map(Response::getValue);
    }

    /**
     * Gets information about the topic along with its HTTP response.
     *
     * @param topicName Name of topic to get information about.
     *
     * @return A Mono that completes with information about the topic and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TopicDescription>> getTopicWithResponse(String topicName) {
        return withContext(context -> getTopicWithResponse(topicName, context, Function.identity()));
    }

    /**
     * Gets runtime information about the topic.
     *
     * @param topicName Name of topic to get information about.
     *
     * @return A Mono that completes with runtime information about the topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TopicRuntimeInfo> getTopicRuntimeInfo(String topicName) {
        return getTopicRuntimeInfoWithResponse(topicName).map(response -> response.getValue());
    }

    /**
     * Gets runtime information about the topic with its HTTP response.
     *
     * @param topicName Name of topic to get information about.
     *
     * @return A Mono that completes with runtime information about the topic and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TopicRuntimeInfo>> getTopicRuntimeInfoWithResponse(String topicName) {
        return withContext(context -> getTopicWithResponse(topicName, context, TopicRuntimeInfo::new));
    }

    /**
     * Fetches all the queues in the Service Bus namespace.
     *
     * @return A Flux of {@link QueueDescription queues} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<QueueDescription> listQueues() {
        return new PagedFlux<>(
            () -> withContext(context -> listQueuesFirstPage(context)),
            token -> withContext(context -> listQueuesNextPage(token, context)));
    }

    /**
     * Fetches all the subscriptions for a topic.
     *
     * @param topicName The topic name under which all the subscriptions need to be retrieved.
     *
     * @return A Flux of {@link SubscriptionDescription subscriptions} for the {@code topicName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SubscriptionDescription> listSubscriptions(String topicName) {
        if (topicName == null) {
            return pagedFluxError(logger, new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            return pagedFluxError(logger, new IllegalArgumentException("'topicName' cannot be an empty string."));
        }

        return new PagedFlux<>(
            () -> withContext(context -> listSubscriptionsFirstPage(topicName, context)),
            token -> withContext(context -> listSubscriptionsNextPage(topicName, token, context)));
    }

    /**
     * Fetches all the topics in the Service Bus namespace.
     *
     * @return A Flux of {@link TopicDescription topics} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TopicDescription> listTopics() {
        return new PagedFlux<>(
            () -> withContext(context -> listTopicsFirstPage(context)),
            token -> withContext(context -> listTopicsNextPage(token, context)));
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
     * @return A Mono that completes with the updated queue.
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
     * Updates a subscription with the given {@link SubscriptionDescription}. The {@link SubscriptionDescription} must
     * be fully populated as all of the properties are replaced. If a property is not set the service default value is
     * used.
     *
     * The suggested flow is:
     * <ol>
     *     <li>{@link #getSubscription(String, String) Get subscription description.}</li>
     *     <li>Update the required elements.</li>
     *     <li>Pass the updated description into this method.</li>
     * </ol>
     *
     * <p>
     * There are a subset of properties that can be updated. They are:
     * <ul>
     * <li>{@link SubscriptionDescription#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link SubscriptionDescription#setLockDuration(Duration) LockDuration}</li>
     * <li>{@link SubscriptionDescription#setMaxDeliveryCount(Integer) MaxDeliveryCount}</li>
     * </ul>
     *
     * @param subscription Information about the subscription to update. You must provide all the property values
     *     that are desired on the updated entity. Any values not provided are set to the service default values.
     *
     * @return A Mono that returns the updated subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the subscription quota is exceeded, or an
     *     error occurred processing the request.
     * @throws IllegalArgumentException if {@link SubscriptionDescription#getTopicName()} or {@link
     *     SubscriptionDescription#getSubscriptionName()} is null or an empty string.
     * @throws NullPointerException if {@code subscription} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionDescription> updateSubscription(SubscriptionDescription subscription) {
        return updateSubscriptionWithResponse(subscription).map(Response::getValue);
    }

    /**
     * Updates a subscription with the given {@link SubscriptionDescription}. The {@link SubscriptionDescription} must
     * be fully populated as all of the properties are replaced. If a property is not set the service default value is
     * used.
     *
     * The suggested flow is:
     * <ol>
     *     <li>{@link #getSubscription(String, String) Get subscription description.}</li>
     *     <li>Update the required elements.</li>
     *     <li>Pass the updated description into this method.</li>
     * </ol>
     *
     * <p>
     * There are a subset of properties that can be updated. They are:
     * <ul>
     * <li>{@link SubscriptionDescription#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link SubscriptionDescription#setLockDuration(Duration) LockDuration}</li>
     * <li>{@link SubscriptionDescription#setMaxDeliveryCount(Integer) MaxDeliveryCount}</li>
     * </ul>
     *
     * @param subscription Information about the subscription to update. You must provide all the property values
     *     that are desired on the updated entity. Any values not provided are set to the service default values.
     *
     * @return A Mono that returns the updated subscription in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the subscription quota is exceeded, or an
     *     error occurred processing the request.
     * @throws IllegalArgumentException if {@link SubscriptionDescription#getTopicName()} or {@link
     *     SubscriptionDescription#getSubscriptionName()} is null or an empty string.
     * @throws NullPointerException if {@code subscription} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SubscriptionDescription>> updateSubscriptionWithResponse(
        SubscriptionDescription subscription) {

        return withContext(context -> updateSubscriptionWithResponse(subscription, context));
    }

    /**
     * Updates a topic with the given {@link TopicDescription}. The {@link TopicDescription} must be fully populated as
     * all of the properties are replaced. If a property is not set the service default value is used.
     *
     * The suggested flow is:
     * <ol>
     *     <li>{@link #getTopic(String) Get topic description.}</li>
     *     <li>Update the required elements.</li>
     *     <li>Pass the updated description into this method.</li>
     * </ol>
     *
     * <p>
     * There are a subset of properties that can be updated. They are:
     * <ul>
     * <li>{@link TopicDescription#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link TopicDescription#setDuplicateDetectionHistoryTimeWindow(Duration) DuplicateDetectionHistoryTimeWindow}
     * </li>
     * </ul>
     *
     * @param topic Information about the topic to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     *
     * @return A Mono that completes with the updated topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link TopicDescription#getName() topic.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code topic} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-topic">Update Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TopicDescription> updateTopic(TopicDescription topic) {
        return updateTopicWithResponse(topic).map(Response::getValue);
    }

    /**
     * Updates a topic with the given {@link TopicDescription}. The {@link TopicDescription} must be fully populated as
     * all of the properties are replaced. If a property is not set the service default value is used.
     *
     * The suggested flow is:
     * <ol>
     *     <li>{@link #getTopic(String) Get topic description.}</li>
     *     <li>Update the required elements.</li>
     *     <li>Pass the updated description into this method.</li>
     * </ol>
     *
     * <p>
     * There are a subset of properties that can be updated. They are:
     * <ul>
     * <li>{@link TopicDescription#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link TopicDescription#setDuplicateDetectionHistoryTimeWindow(Duration) DuplicateDetectionHistoryTimeWindow}
     * </li>
     * </ul>
     *
     * @param topic Information about the topic to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     *
     * @return A Mono that completes with the updated topic and its HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link TopicDescription#getName() topic.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code topic} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-topic">Update Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TopicDescription>> updateTopicWithResponse(TopicDescription topic) {
        return withContext(context -> updateTopicWithResponse(topic, context));
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
            return entityClient.putWithResponseAsync(queue.getName(), createEntity, null, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(this::deserializeQueue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a subscription with its context.
     *
     * @param subscription Subscription to create.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link SubscriptionDescription}.
     */
    Mono<Response<SubscriptionDescription>> createSubscriptionWithResponse(SubscriptionDescription subscription,
        Context context) {
        if (subscription == null) {
            return monoError(logger, new NullPointerException("'subscription' cannot be null."));
        }

        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(subscription);
        final CreateSubscriptionBody createEntity = new CreateSubscriptionBody().setContent(content);

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return managementClient.getSubscriptions().putWithResponseAsync(subscription.getTopicName(),
                subscription.getSubscriptionName(), createEntity, null, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> deserializeSubscription(subscription.getTopicName(), response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a topic with its context.
     *
     * @param topic Topic to create.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link TopicDescription}.
     */
    Mono<Response<TopicDescription>> createTopicWithResponse(TopicDescription topic, Context context) {
        if (topic == null) {
            return monoError(logger, new NullPointerException("'topic' cannot be null"));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(topic);
        final CreateTopicBody createEntity = new CreateTopicBody()
            .setContent(content);

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return entityClient.putWithResponseAsync(topic.getName(), createEntity, null, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(this::deserializeTopic);
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
            return monoError(logger, new IllegalArgumentException("'queueName' cannot be an empty string."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return entityClient.deleteWithResponseAsync(queueName, withTracing)
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
     * Deletes a subscription with its context.
     *
     * @param topicName Name of topic associated with subscription to delete.
     * @param subscriptionName Name of subscription to delete.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link SubscriptionDescription}.
     */
    Mono<Response<Void>> deleteSubscriptionWithResponse(String topicName, String subscriptionName, Context context) {
        if (subscriptionName == null) {
            return monoError(logger, new NullPointerException("'subscriptionName' cannot be null"));
        } else if (subscriptionName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'subscriptionName' cannot be an empty string."));
        } else if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null"));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be empty."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return managementClient.getSubscriptions().deleteWithResponseAsync(topicName, subscriptionName,
                withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a topic with its context.
     *
     * @param topicName Name of topic to delete.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link TopicDescription}.
     */
    Mono<Response<Void>> deleteTopicWithResponse(String topicName, Context context) {
        if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null"));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return entityClient.deleteWithResponseAsync(topicName, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
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
    <T> Mono<Response<T>> getQueueWithResponse(String queueName, Context context,
        Function<QueueDescription, T> mapper) {
        if (queueName == null) {
            return monoError(logger, new NullPointerException("'queueName' cannot be null"));
        } else if (queueName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'queueName' cannot be empty."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return entityClient.getWithResponseAsync(queueName, true, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> {
                    final Response<QueueDescription> deserialize = deserializeQueue(response);

                    // In the case this is a 204, do not try to map it.
                    final T mapped = deserialize.getValue() != null
                        ? mapper.apply(deserialize.getValue())
                        : null;

                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                        mapped);
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a subscription with its context.
     *
     * @param topicName Name of the topic associated with the subscription.
     * @param subscriptionName Name of subscription to fetch information for.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the {@link SubscriptionDescription}.
     */
    <T> Mono<Response<T>> getSubscriptionWithResponse(String topicName, String subscriptionName, Context context,
        Function<SubscriptionDescription, T> mapper) {
        if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (subscriptionName == null) {
            return monoError(logger, new NullPointerException("'subscriptionName' cannot be null."));
        }  else if (subscriptionName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'subscriptionName' cannot be an empty string."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return managementClient.getSubscriptions().getWithResponseAsync(topicName, subscriptionName, true,
                withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> {
                    final Response<SubscriptionDescription> deserialize = deserializeSubscription(topicName, response);

                    // In the case this is a 204, do not try to map it.
                    final T mapped = deserialize.getValue() != null
                        ? mapper.apply(deserialize.getValue())
                        : null;

                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                        mapped);
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a topic with its context.
     *
     * @param topicName Name of topic to fetch information for.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the {@link TopicDescription}.
     */
    <T> Mono<Response<T>> getTopicWithResponse(String topicName, Context context,
        Function<TopicDescription, T> mapper) {
        if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null"));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be empty."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return entityClient.getWithResponseAsync(topicName, true, withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> {
                    final Response<TopicDescription> deserialize = deserializeTopic(response);

                    // In the case this is a 204, do not try to map it.
                    final T mapped = deserialize.getValue() != null
                        ? mapper.apply(deserialize.getValue())
                        : null;

                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                        mapped);
                });
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
            return listQueues(0, withTracing);
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

            return listQueues(skip, withTracing);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets the first page of subscriptions with context.
     *
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with a page of subscriptions.
     */
    Mono<PagedResponse<SubscriptionDescription>> listSubscriptionsFirstPage(String topicName, Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return listSubscriptions(topicName, 0, withTracing);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets the next page of subscriptions with context.
     *
     * @param continuationToken Number of items to skip in feed.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with a page of subscriptions or empty if there are no items left.
     */
    Mono<PagedResponse<SubscriptionDescription>> listSubscriptionsNextPage(String topicName, String continuationToken,
        Context context) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        try {
            final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);
            final int skip = Integer.parseInt(continuationToken);

            return listSubscriptions(topicName, skip, withTracing);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets the first page of topics with context.
     *
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with a page of topics.
     */
    Mono<PagedResponse<TopicDescription>> listTopicsFirstPage(Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return listTopics(0, withTracing);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets the next page of topics with context.
     *
     * @param continuationToken Number of items to skip in feed.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with a page of topics or empty if there are no items left.
     */
    Mono<PagedResponse<TopicDescription>> listTopicsNextPage(String continuationToken, Context context) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        try {
            final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);
            final int skip = Integer.parseInt(continuationToken);

            return listTopics(skip, withTracing);
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
            return entityClient.putWithResponseAsync(queue.getName(), createEntity, "*", withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> deserializeQueue(response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates a subscription with its context.
     *
     * @param subscription Information about the subscription to update. You must provide all the property values
     *     that are desired on the updated entity. Any values not provided are set to the service default values.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the updated {@link SubscriptionDescription}.
     */
    Mono<Response<SubscriptionDescription>> updateSubscriptionWithResponse(SubscriptionDescription subscription,
        Context context) {
        if (subscription == null) {
            return monoError(logger, new NullPointerException("'subscription' cannot be null"));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final String topicName = subscription.getTopicName();
        final String subscriptionName = subscription.getSubscriptionName();
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(subscription);
        final CreateSubscriptionBody createEntity = new CreateSubscriptionBody()
            .setContent(content);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return managementClient.getSubscriptions().putWithResponseAsync(topicName, subscriptionName, createEntity,
                "*", withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> deserializeSubscription(topicName, response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates a topic with its context.
     *
     * @param topic Information about the topic to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the updated {@link TopicDescription}.
     */
    Mono<Response<TopicDescription>> updateTopicWithResponse(TopicDescription topic, Context context) {
        if (topic == null) {
            return monoError(logger, new NullPointerException("'topic' cannot be null"));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(topic);
        final CreateTopicBody createEntity = new CreateTopicBody()
            .setContent(content);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return entityClient.putWithResponseAsync(topic.getName(), createEntity, "*", withTracing)
                .onErrorMap(ServiceBusManagementAsyncClient::mapException)
                .map(response -> deserializeTopic(response));
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
        } else if (entry.getContent() == null) {
            logger.warning("entry.getContent() is null. There should have been content returned. Entry: {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final QueueDescription result = entry.getContent().getQueueDescription();
        final String queueName = getTitleValue(entry.getTitle());
        EntityHelper.setQueueName(result, queueName);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), result);
    }

    /**
     * Converts a Response into its corresponding {@link SubscriptionDescriptionEntry} then mapped into {@link
     * SubscriptionDescription}.
     *
     * @param response HTTP Response to deserialize.
     *
     * @return The corresponding HTTP response with convenience properties set.
     */
    private Response<SubscriptionDescription> deserializeSubscription(String topicName, Response<Object> response) {
        final SubscriptionDescriptionEntry entry = deserialize(response.getValue(), SubscriptionDescriptionEntry.class);

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            logger.warning("entry.getContent() is null. There should have been content returned. Entry: {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final SubscriptionDescription subscription = entry.getContent().getSubscriptionDescription();
        final String subscriptionName = getTitleValue(entry.getTitle());
        EntityHelper.setSubscriptionName(subscription, subscriptionName);
        EntityHelper.setTopicName(subscription, topicName);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            subscription);
    }

    /**
     * Converts a Response into its corresponding {@link TopicDescriptionEntry} then mapped into {@link
     * QueueDescription}.
     *
     * @param response HTTP Response to deserialize.
     *
     * @return The corresponding HTTP response with convenience properties set.
     */
    private Response<TopicDescription> deserializeTopic(Response<Object> response) {
        final TopicDescriptionEntry entry = deserialize(response.getValue(), TopicDescriptionEntry.class);

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            logger.warning("entry.getContent() is null. There should have been content returned. Entry: {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final TopicDescription result = entry.getContent().getTopicDescription();
        final String queueName = getTitleValue(entry.getTitle());
        EntityHelper.setTopicName(result, queueName);

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
     * @param context Context for the query.
     *
     * @return A Mono that completes with a paged response of queues.
     */
    private Mono<PagedResponse<QueueDescription>> listQueues(int skip, Context context) {
        return managementClient.listEntitiesWithResponseAsync(QUEUES_ENTITY_TYPE, skip, ServiceBusManagementAsyncClient.NUMBER_OF_ELEMENTS, context)
            .onErrorMap(ServiceBusManagementAsyncClient::mapException)
            .flatMap(response -> {
                final Response<QueueDescriptionFeed> feedResponse = deserialize(response, QueueDescriptionFeed.class);
                final QueueDescriptionFeed feed = feedResponse.getValue();
                if (feed == null) {
                    logger.warning("Could not deserialize QueueDescriptionFeed. skip {}, top: {}", skip, ServiceBusManagementAsyncClient.NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<QueueDescription> entities = feed.getEntry().stream()
                    .filter(e -> e.getContent() != null && e.getContent().getQueueDescription() != null)
                    .map(e -> {
                        final String queueName = getTitleValue(e.getTitle());
                        final QueueDescription queueDescription = e.getContent().getQueueDescription();
                        EntityHelper.setQueueName(queueDescription, queueName);

                        return queueDescription;
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
     * Helper method that invokes the service method, extracts the data and translates it to a PagedResponse.
     *
     * @param skip Number of elements to skip.
     * @param context Context for the query.
     *
     * @return A Mono that completes with a paged response of subscriptions.
     */
    private Mono<PagedResponse<SubscriptionDescription>> listSubscriptions(String topicName, int skip,
        Context context) {
        return managementClient.listSubscriptionsWithResponseAsync(topicName, skip, NUMBER_OF_ELEMENTS, context)
            .onErrorMap(ServiceBusManagementAsyncClient::mapException)
            .flatMap(response -> {
                final Response<SubscriptionDescriptionFeed> feedResponse = deserialize(response,
                    SubscriptionDescriptionFeed.class);

                final SubscriptionDescriptionFeed feed = feedResponse.getValue();
                if (feed == null) {
                    logger.warning("Could not deserialize SubscriptionDescriptionFeed. skip {}, top: {}", skip, ServiceBusManagementAsyncClient.NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<SubscriptionDescription> entities = feed.getEntry().stream()
                    .filter(e -> e.getContent() != null && e.getContent().getSubscriptionDescription() != null)
                    .map(e -> {
                        final String subscriptionName = getTitleValue(e.getTitle());
                        final SubscriptionDescription description = e.getContent().getSubscriptionDescription();

                        EntityHelper.setTopicName(description, topicName);
                        EntityHelper.setSubscriptionName(description, subscriptionName);

                        return description;
                    })
                    .collect(Collectors.toList());
                try {
                    return Mono.just(extractPage(feedResponse, entities, feed.getLink()));
                } catch (MalformedURLException | UnsupportedEncodingException error) {
                    return Mono.error(new RuntimeException("Could not parse response into FeedPage<SubscriptionDescription>",
                        error));
                }
            });
    }

    /**
     * Helper method that invokes the service method, extracts the data and translates it to a PagedResponse.
     *
     * @param skip Number of elements to skip.
     * @param context Context for the query.
     *
     * @return A Mono that completes with a paged response of topics.
     */
    private Mono<PagedResponse<TopicDescription>> listTopics(int skip, Context context) {
        return managementClient.listEntitiesWithResponseAsync(TOPICS_ENTITY_TYPE, skip, ServiceBusManagementAsyncClient.NUMBER_OF_ELEMENTS, context)
            .onErrorMap(ServiceBusManagementAsyncClient::mapException)
            .flatMap(response -> {
                final Response<TopicDescriptionFeed> feedResponse = deserialize(response, TopicDescriptionFeed.class);
                final TopicDescriptionFeed feed = feedResponse.getValue();
                if (feed == null) {
                    logger.warning("Could not deserialize TopicDescriptionFeed. skip {}, top: {}", skip, ServiceBusManagementAsyncClient.NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<TopicDescription> entities = feed.getEntry().stream()
                    .filter(e -> e.getContent() != null && e.getContent().getTopicDescription() != null)
                    .map(e -> {
                        final String topicName = getTitleValue(e.getTitle());
                        final TopicDescription topicDescription = e.getContent().getTopicDescription();
                        EntityHelper.setTopicName(topicDescription, topicName);

                        return topicDescription;
                    })
                    .collect(Collectors.toList());
                try {
                    return Mono.just(extractPage(feedResponse, entities, feed.getLink()));
                } catch (MalformedURLException | UnsupportedEncodingException error) {
                    return Mono.error(new RuntimeException("Could not parse response into FeedPage<TopicDescription>",
                        error));
                }
            });
    }

    /**
     * Given an XML title element, returns the XML text inside. Jackson deserializes Objects as LinkedHashMaps. XML text
     * is represented as an entry with an empty string as the key.
     *
     * For example, the text returned from this {@code <title text="text/xml">QueueName</title>} is "QueueName".
     *
     * @param responseTitle XML title element.
     *
     * @return The XML text inside the title. {@code null} is returned if there is no value.
     */
    @SuppressWarnings("unchecked")
    private String getTitleValue(Object responseTitle) {
        if (!(responseTitle instanceof Map)) {
            return null;
        }

        final Map<String, String> map;
        try {
            map = (Map<String, String>) responseTitle;
            return map.get("");
        } catch (ClassCastException error) {
            logger.warning("Unable to cast to Map<String,String>. Title: {}", responseTitle, error);
            return null;
        }
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
