// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.NamespaceProperties;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.administration.models.TopicRuntimeProperties;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * A <b>synchronous</b> client for managing a Service Bus namespace.
 *
 * <p><strong>Create a queue</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string}
 *
 * <p><strong>Edit an existing subscription</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatesubscription#subscriptionproperties}
 *
 * <p><strong>List all queues</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.administration.servicebusadministrationclient.listQueues}
 *
 * @see ServiceBusAdministrationClientBuilder
 * @see ServiceBusAdministrationAsyncClient ServiceBusAdministrationAsyncClient for an asynchronous client.
 */
@ServiceClient(builder = ServiceBusAdministrationClientBuilder.class)
public final class ServiceBusAdministrationClient {
    private final ServiceBusAdministrationAsyncClient asyncClient;

    /**
     * Creates a new instance with the given client.
     *
     * @param asyncClient Asynchronous client to perform management calls through.
     */
    ServiceBusAdministrationClient(ServiceBusAdministrationAsyncClient asyncClient) {
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
    }

    /**
     * Creates a queue with the given name.
     *
     * @param queueName Name of the queue to create.
     *
     * @return The created queue.
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
    public QueueProperties createQueue(String queueName) {
        return asyncClient.createQueue(queueName).block();
    }

    /**
     * Creates a queue with the {@link CreateQueueOptions}.
     *
     * @param queueName Name of the queue to create.
     * @param queueOptions Information about the queue to create.
     *
     * @return The created queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws NullPointerException if {@code queue} is null.
     * @throws ResourceExistsException if a queue exists with the same {@link QueueProperties#getName() queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueProperties createQueue(String queueName, CreateQueueOptions queueOptions) {
        return asyncClient.createQueue(queueName, queueOptions).block();
    }

    /**
     * Creates a queue and returns the created queue in addition to the HTTP response.
     *
     * @param queueName Name of the queue to create.
     * @param queueOptions Information about the queue to create.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The created queue in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws NullPointerException if {@code queue} is null.
     * @throws ResourceExistsException if a queue exists with the same {@link QueueProperties#getName() queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueProperties> createQueueWithResponse(String queueName, CreateQueueOptions queueOptions,
        Context context) {
        return asyncClient.createQueueWithResponse(queueName, queueOptions, context != null ? context : Context.NONE)
            .block();
    }

    /**
     * Creates a rule under the given topic and subscription
     *
     * @param topicName Name of the topic associated with rule.
     * @param subscriptionName Name of the subscription associated with the rule.
     * @param ruleName Name of the rule.
     *
     * @return Information about the created rule.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are are empty strings.
     * @throws NullPointerException if {@code topicName} or {@code ruleName} are are null.
     * @throws ResourceExistsException if a rule exists with the same topic, subscription, and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RuleProperties createRule(String topicName, String subscriptionName, String ruleName) {
        return asyncClient.createRule(topicName, subscriptionName, ruleName).block();
    }

    /**
     * Creates a rule with the {@link CreateRuleOptions}.
     *
     * @param topicName Name of the topic associated with rule.
     * @param subscriptionName Name of the subscription associated with the rule.
     * @param ruleName Name of the rule.
     * @param ruleOptions Information about the rule to create.
     *
     * @return Information about the created rule.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are are empty strings.
     * @throws NullPointerException if {@code topicName}, {@code ruleName}, or {@code ruleOptions} are are null.
     * @throws ResourceExistsException if a rule exists with the same topic and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RuleProperties createRule(String topicName, String ruleName, String subscriptionName,
        CreateRuleOptions ruleOptions) {
        return asyncClient.createRule(topicName, subscriptionName, ruleName, ruleOptions).block();
    }

    /**
     * Creates a rule and returns the created rule in addition to the HTTP response.
     *
     * @param topicName Name of the topic associated with rule.
     * @param subscriptionName Name of the subscription associated with the rule.
     * @param ruleName Name of the rule.
     * @param ruleOptions Information about the rule to create.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The created rule in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are are empty strings.
     * @throws NullPointerException if {@code topicName}, {@code ruleName}, or {@code ruleOptions} are are null.
     * @throws ResourceExistsException if a rule exists with the same topic and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RuleProperties> createRuleWithResponse(String topicName, String subscriptionName,
        String ruleName, CreateRuleOptions ruleOptions, Context context) {
        return asyncClient.createRuleWithResponse(topicName, subscriptionName, ruleName, ruleOptions, context).block();
    }

    /**
     * Creates a subscription with the given topic and subscription names.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     *
     * @return Information about the created subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} are are empty strings.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are are null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionProperties createSubscription(String topicName, String subscriptionName) {
        return asyncClient.createSubscription(topicName, subscriptionName).block();
    }

    /**
     * Creates a subscription with the {@link SubscriptionProperties}.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param subscriptionOptions Information about the subscription to create.
     *
     * @return Information about the created subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are are empty strings.
     * @throws NullPointerException if {@code topicName}, {@code subscriptionName}, or {@code subscriptionOptions}
     *     are are null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionProperties createSubscription(String topicName, String subscriptionName,
        CreateSubscriptionOptions subscriptionOptions) {
        return asyncClient.createSubscription(topicName, subscriptionName, subscriptionOptions).block();
    }

    /**
     * Creates a queue and returns the created queue in addition to the HTTP response.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param subscriptionOptions Information about the subscription to create.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The created subscription in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws NullPointerException if {@code subscription} is null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SubscriptionProperties> createSubscriptionWithResponse(String topicName, String subscriptionName,
        CreateSubscriptionOptions subscriptionOptions, Context context) {
        return asyncClient.createSubscriptionWithResponse(topicName, subscriptionName, subscriptionOptions,
            context != null ? context : Context.NONE).block();
    }

    /**
     * Creates a topic with the given name.
     *
     * @param topicName Name of the topic to create.
     *
     * @return Information about the created topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws ResourceExistsException if a topic exists with the same {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TopicProperties createTopic(String topicName) {
        return asyncClient.createTopic(topicName).block();
    }

    /**
     * Creates a topic with the {@link CreateTopicOptions}.
     *
     * @param topicName Name of the topic to create.
     * @param topicOptions Information about the topic to create.
     *
     * @return Information about the created topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topicOptions quota is exceeded, or an
     *     error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} or {@code topicOptions} is null.
     * @throws ResourceExistsException if a topic exists with the same {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TopicProperties createTopic(String topicName, CreateTopicOptions topicOptions) {
        return asyncClient.createTopic(topicName, topicOptions).block();
    }

    /**
     * Creates a topic and returns the created topic in addition to the HTTP response.
     *
     * @param topicName Name of the topic to create.
     * @param topicOptions Information about the topic to create.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The created topic in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} or {@code topicOptions} is null.
     * @throws ResourceExistsException if a topic exists with the same {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TopicProperties> createTopicWithResponse(String topicName, CreateTopicOptions topicOptions,
        Context context) {
        return asyncClient.createTopicWithResponse(topicName, topicOptions, context != null ? context : Context.NONE)
            .block();
    }

    /**
     * Deletes a queue the matching {@code queueName}.
     *
     * @param queueName Name of queue to delete.
     *
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-queue">Delete Queue</a>
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
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-queue">Delete Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String queueName, Context context) {
        return asyncClient.deleteQueueWithResponse(queueName, context != null ? context : Context.NONE).block();
    }

    /**
     * Deletes a rule the matching {@code ruleName}.
     *
     * @param topicName Name of topic associated with rule to delete.
     * @param subscriptionName Name of the subscription associated with the rule to delete.
     * @param ruleName Name of rule to delete.
     *
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} is an empty string.
     * @throws NullPointerException if {@code topicName} or {@code ruleName} is null.
     * @throws ResourceNotFoundException if the {@code ruleName} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRule(String topicName, String subscriptionName, String ruleName) {
        asyncClient.deleteRule(topicName, subscriptionName, ruleName).block();
    }

    /**
     * Deletes a rule the matching {@code ruleName} and returns the HTTP response.
     *
     * @param topicName Name of topic associated with rule to delete.
     * @param subscriptionName Name of the subscription associated with the rule to delete.
     * @param ruleName Name of rule to delete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName}, {@code subscriptionName}, or {@code ruleName} is an
     *     empty string.
     * @throws NullPointerException if {@code topicName}, {@code subscriptionName}, or {@code ruleName} is null.
     * @throws ResourceNotFoundException if the {@code ruleName} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRuleWithResponse(String topicName, String subscriptionName,
        String ruleName, Context context) {
        return asyncClient.deleteRuleWithResponse(topicName, subscriptionName, ruleName,
            context != null ? context : Context.NONE).block();
    }

    /**
     * Deletes a subscription matching the {@code subscriptionName} in topic {@code topicName}.
     *
     * @param topicName Name of topic associated with subscription to delete.
     * @param subscriptionName Name of subscription to delete.
     *
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} is null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-subscription">Delete Subscription</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSubscription(String topicName, String subscriptionName) {
        asyncClient.deleteSubscription(topicName, subscriptionName).block();
    }

    /**
     * Deletes a subscription the matching {@code subscriptionName} and returns the HTTP response.
     *
     * @param topicName Name of topic associated with subscription to delete.
     * @param subscriptionName Name of subscription to delete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} is null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-subscription">Delete Subscription</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteSubscriptionWithResponse(String topicName, String subscriptionName, Context context) {
        return asyncClient.deleteSubscriptionWithResponse(topicName, subscriptionName,
            context != null ? context : Context.NONE).block();
    }

    /**
     * Deletes a topic the matching {@code topicName}.
     *
     * @param topicName Name of topic to delete.
     *
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-topic">Delete Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTopic(String topicName) {
        asyncClient.deleteTopic(topicName).block();
    }

    /**
     * Deletes a topic the matching {@code topicName} and returns the HTTP response.
     *
     * @param topicName Name of topic to delete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-topic">Delete Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTopicWithResponse(String topicName, Context context) {
        return asyncClient.deleteTopicWithResponse(topicName, context != null ? context : Context.NONE).block();
    }

    /**
     * Gets information about the queue.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return Information about the queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueProperties getQueue(String queueName) {
        return asyncClient.getQueue(queueName).block();
    }

    /**
     * Gets information about the queue along with its HTTP response.
     *
     * @param queueName Name of queue to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Information about the queue and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueProperties> getQueueWithResponse(String queueName, Context context) {
        return asyncClient.getQueueWithResponse(queueName, context != null ? context : Context.NONE,
            Function.identity()).block();
    }

    /**
     * Gets whether or not a queue with {@code queueName} exists in the Service Bus namespace.
     *
     * @param queueName Name of the queue.
     *
     * @return {@code true} if the queue exists; otherwise {@code false}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean getQueueExists(String queueName) {
        final Boolean exists = asyncClient.getQueueExists(queueName).block();
        return exists != null && exists;
    }

    /**
     * Gets whether or not a queue with {@code queueName} exists in the Service Bus namespace.
     *
     * @param queueName Name of the queue.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response and {@code true} if the queue exists; otherwise {@code false}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> getQueueExistsWithResponse(String queueName, Context context) {
        final Mono<Response<QueueProperties>> queueWithResponse =
            asyncClient.getQueueWithResponse(queueName, context != null ? context : Context.NONE, Function.identity());
        return asyncClient.getEntityExistsWithResponse(queueWithResponse).block();
    }

    /**
     * Gets runtime properties about the queue.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return Runtime properties about the queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueRuntimeProperties getQueueRuntimeProperties(String queueName) {
        return asyncClient.getQueueRuntimeProperties(queueName).block();
    }

    /**
     * Gets runtime properties about the queue along with its HTTP response.
     *
     * @param queueName Name of queue to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Runtime properties about the queue and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueRuntimeProperties> getQueueRuntimePropertiesWithResponse(String queueName, Context context) {
        return asyncClient.getQueueWithResponse(queueName, context != null ? context : Context.NONE,
            QueueRuntimeProperties::new).block();
    }

    /**
     * Gets information about the Service Bus namespace.
     *
     * @return Information about the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to the namespace.
     * @throws HttpResponseException If error occurred processing the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NamespaceProperties getNamespaceProperties() {
        return asyncClient.getNamespaceProperties().block();
    }

    /**
     * Gets information about the Service Bus namespace along with its HTTP response.
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Information about the namespace and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NamespaceProperties> getNamespacePropertiesWithResponse(Context context) {
        return asyncClient.getNamespacePropertiesWithResponse(context).block();
    }

    /**
     * Gets a rule from the service namespace.
     *
     * Only following data types are deserialized in Filters and Action parameters - string, int, long, boolean, double,
     * and OffsetDateTime. Other data types would return its string value.
     *
     * @param topicName The name of the topic relative to service bus namespace.
     * @param subscriptionName The subscription name the rule belongs to.
     * @param ruleName The name of the rule to retrieve.
     *
     * @return The associated rule.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RuleProperties getRule(String topicName, String subscriptionName, String ruleName) {
        return asyncClient.getRule(topicName, subscriptionName, ruleName).block();
    }

    /**
     * Gets a rule from the service namespace.
     *
     * Only following data types are deserialized in Filters and Action parameters - string, int, long, bool, double,
     * and OffsetDateTime. Other data types would return its string value.
     *
     * @param topicName The name of the topic relative to service bus namespace.
     * @param subscriptionName The subscription name the rule belongs to.
     * @param ruleName The name of the rule to retrieve.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The associated rule with the corresponding HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RuleProperties> getRuleWithResponse(String topicName, String subscriptionName,
        String ruleName, Context context) {
        return asyncClient.getRuleWithResponse(topicName, subscriptionName, ruleName,
            context != null ? context : Context.NONE).block();
    }

    /**
     * Gets information about the queue.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return Information about the subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are empty strings.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} are null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist in the {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionProperties getSubscription(String topicName, String subscriptionName) {
        return asyncClient.getSubscription(topicName, subscriptionName).block();
    }

    /**
     * Gets information about the subscription along with its HTTP response.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Information about the subscription and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are empty strings.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} are null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SubscriptionProperties> getSubscriptionWithResponse(String topicName,
        String subscriptionName, Context context) {
        return asyncClient.getSubscriptionWithResponse(topicName, subscriptionName,
            context != null ? context : Context.NONE, Function.identity()).block();
    }

    /**
     * Gets whether or not a subscription within a topic exists.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     *
     * @return {@code true} if the subscription exists.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code subscriptionName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean getSubscriptionExists(String topicName, String subscriptionName) {
        final Boolean exists = asyncClient.getSubscriptionExists(topicName, subscriptionName).block();
        return exists != null && exists;
    }

    /**
     * Gets whether or not a subscription within a topic exists.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response and {@code true} if the subscription exists; otherwise {@code false}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code subscriptionName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> getSubscriptionExistsWithResponse(String topicName, String subscriptionName,
        Context context) {
        final Mono<Response<SubscriptionProperties>> subscriptionWithResponse =
            asyncClient.getSubscriptionWithResponse(topicName, subscriptionName,
                context != null ? context : Context.NONE, Function.identity());
        return asyncClient.getEntityExistsWithResponse(subscriptionWithResponse).block();
    }

    /**
     * Gets runtime properties about the subscription.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return Runtime properties about the subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code subscriptionName} is null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionRuntimeProperties getSubscriptionRuntimeProperties(String topicName, String subscriptionName) {
        return asyncClient.getSubscriptionRuntimeProperties(topicName, subscriptionName).block();
    }

    /**
     * Gets runtime properties about the subscription.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Runtime properties about the subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code subscriptionName} is null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SubscriptionRuntimeProperties> getSubscriptionRuntimePropertiesWithResponse(
        String topicName, String subscriptionName, Context context) {
        return asyncClient.getSubscriptionWithResponse(topicName, subscriptionName,
            context != null ? context : Context.NONE, SubscriptionRuntimeProperties::new).block();
    }

    /**
     * Gets information about the topic.
     *
     * @param topicName Name of topic to get information about.
     *
     * @return Information about the topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TopicProperties getTopic(String topicName) {
        return asyncClient.getTopic(topicName).block();
    }

    /**
     * Gets information about the topic along with its HTTP response.
     *
     * @param topicName Name of topic to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Information about the topic and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TopicProperties> getTopicWithResponse(String topicName, Context context) {
        return asyncClient.getTopicWithResponse(topicName, context != null ? context : Context.NONE,
            Function.identity()).block();
    }

    /**
     * Gets whether or not a topic with {@code topicName} exists in the Service Bus namespace.
     *
     * @param topicName Name of the topic.
     *
     * @return {@code true} if the topic exists.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean getTopicExists(String topicName) {
        final Boolean exists = asyncClient.getTopicExists(topicName).block();
        return exists != null && exists;
    }

    /**
     * Gets whether or not a topic with {@code topicName} exists in the Service Bus namespace.
     *
     * @param topicName Name of the topic.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response and {@code true} if the topic exists; otherwise {@code false}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> getTopicExistsWithResponse(String topicName, Context context) {
        final Mono<Response<TopicProperties>> topicWithResponse =
            asyncClient.getTopicWithResponse(topicName, context != null ? context : Context.NONE, Function.identity());
        return asyncClient.getEntityExistsWithResponse(topicWithResponse).block();
    }

    /**
     * Gets runtime properties about the topic.
     *
     * @param topicName Name of topic to get information about.
     *
     * @return Runtime properties about the topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TopicRuntimeProperties getTopicRuntimeProperties(String topicName) {
        return asyncClient.getTopicRuntimeProperties(topicName).block();
    }

    /**
     * Gets runtime properties about the topic with its HTTP response.
     *
     * @param topicName Name of topic to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Runtime properties about the topic and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TopicRuntimeProperties> getTopicRuntimePropertiesWithResponse(String topicName, Context context) {
        return asyncClient.getTopicWithResponse(topicName, context != null ? context : Context.NONE,
            TopicRuntimeProperties::new).block();
    }

    /**
     * Fetches all the queues in the Service Bus namespace.
     *
     * @return A PagedIterable of {@link QueueProperties queues} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List Queues, Subscriptions, or
     *     Authorization Rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<QueueProperties> listQueues() {
        return new PagedIterable<>(asyncClient.listQueues());
    }

    /**
     * Fetches all the queues in the Service Bus namespace.
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A PagedIterable of {@link QueueProperties queues} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List Queues, Subscriptions, or
     *     Authorization Rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<QueueProperties> listQueues(Context context) {
        final PagedFlux<QueueProperties> pagedFlux = new PagedFlux<>(
            () -> asyncClient.listQueuesFirstPage(context),
            continuationToken -> asyncClient.listQueuesNextPage(continuationToken,
                context != null ? context : Context.NONE));

        return new PagedIterable<>(pagedFlux);
    }

    /**
     * Fetches all the rules for a topic and subscription.
     *
     * @param topicName The topic name under which all the rules need to be retrieved.
     * @param subscriptionName The name of the subscription for which all rules need to be retrieved.
     *
     * @return An iterable of {@link RuleProperties rules} for the {@code topicName} and {@code subscriptionName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} is null.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, rules, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<RuleProperties> listRules(String topicName, String subscriptionName) {
        return new PagedIterable<>(asyncClient.listRules(topicName, subscriptionName));
    }

    /**
     * Fetches all the subscriptions for a topic.
     *
     * @param topicName The topic name under which all the subscriptions need to be retrieved.
     *
     * @return A paged iterable of {@link SubscriptionProperties subscriptions} for the {@code topicName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SubscriptionProperties> listSubscriptions(String topicName) {
        return new PagedIterable<>(asyncClient.listSubscriptions(topicName));
    }

    /**
     * Fetches all the subscriptions for a topic.
     *
     * @param topicName The topic name under which all the subscriptions need to be retrieved.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A paged iterable of {@link SubscriptionProperties subscriptions} for the {@code topicName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SubscriptionProperties> listSubscriptions(String topicName, Context context) {
        final PagedFlux<SubscriptionProperties> pagedFlux = new PagedFlux<>(
            () -> asyncClient.listSubscriptionsFirstPage(topicName, context != null ? context : Context.NONE),
            continuationToken -> asyncClient.listSubscriptionsNextPage(topicName, continuationToken,
                context != null ? context : Context.NONE));

        return new PagedIterable<>(pagedFlux);
    }

    /**
     * Fetches all the topics in the Service Bus namespace.
     *
     * @return A paged iterable of {@link TopicProperties topics} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TopicProperties> listTopics() {
        return new PagedIterable<>(asyncClient.listTopics());
    }

    /**
     * Fetches all the topics in the Service Bus namespace.
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A paged iterable of {@link TopicProperties topics} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TopicProperties> listTopics(Context context) {
        final PagedFlux<TopicProperties> pagedFlux = new PagedFlux<>(
            () -> asyncClient.listTopicsFirstPage(context),
            continuationToken -> asyncClient.listTopicsNextPage(continuationToken,
                context != null ? context : Context.NONE));

        return new PagedIterable<>(pagedFlux);
    }

    /**
     * Updates a queue with the given {@link QueueProperties}. The {@link QueueProperties} must be fully populated as
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
     * There are a subset of properties that can be updated. More information can be found in the links below. They are:
     * <ul>
     * <li>{@link QueueProperties#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link QueueProperties#setLockDuration(Duration) LockDuration}</li>
     * <li>{@link QueueProperties#setDuplicateDetectionHistoryTimeWindow(Duration) DuplicateDetectionHistoryTimeWindow}
     * </li>
     * <li>{@link QueueProperties#setMaxDeliveryCount(Integer) MaxDeliveryCount}</li>
     * </ul>
     *
     * @param queue Information about the queue to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     *
     * @return The updated queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws NullPointerException if {@code queue} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-queue">Update Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueProperties updateQueue(QueueProperties queue) {
        return asyncClient.updateQueue(queue).block();
    }

    /**
     * Updates a queue with the given {@link QueueProperties}. The {@link QueueProperties} must be fully populated as
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
     * There are a subset of properties that can be updated. More information can be found in the links below. They are:
     * <ul>
     * <li>{@link QueueProperties#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link QueueProperties#setLockDuration(Duration) LockDuration}</li>
     * <li>{@link QueueProperties#setDuplicateDetectionHistoryTimeWindow(Duration) DuplicateDetectionHistoryTimeWindow}
     * </li>
     * <li>{@link QueueProperties#setMaxDeliveryCount(Integer) MaxDeliveryCount}</li>
     * </ul>
     *
     * @param queue Information about the queue to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The updated queue with its HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws NullPointerException if {@code queue} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-queue">Update Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueProperties> updateQueueWithResponse(QueueProperties queue, Context context) {
        return asyncClient.updateQueueWithResponse(queue, context != null ? context : Context.NONE).block();
    }

    /**
     * Updates a rule with the given {@link RuleProperties}. The {@link RuleProperties} must be fully populated as all
     * of the properties are replaced. If a property is not set the service default value is used.
     *
     * The suggested flow is:
     * <ol>
     *     <li>{@link #getRule(String, String, String) Get rule description.}</li>
     *     <li>Update the required elements.</li>
     *     <li>Pass the updated description into this method.</li>
     * </ol>
     *
     * @param topicName The topic name under which the rule is updated.
     * @param subscriptionName The name of the subscription for which the rule is updated.
     * @param rule Information about the rule to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     *
     * @return The updated rule.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the rule quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link RuleProperties#getName()} is null or an empty string.
     * @throws NullPointerException if {@code rule} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RuleProperties updateRule(String topicName, String subscriptionName, RuleProperties rule) {
        return asyncClient.updateRule(topicName, subscriptionName, rule).block();
    }

    /**
     * Updates a rule with the given {@link RuleProperties}. The {@link RuleProperties} must be fully populated as all
     * of the properties are replaced. If a property is not set the service default value is used.
     *
     * The suggested flow is:
     * <ol>
     *     <li>{@link #getRule(String, String, String) Get rule description.}</li>
     *     <li>Update the required elements.</li>
     *     <li>Pass the updated description into this method.</li>
     * </ol>
     *
     * @param topicName The topic name under which the rule is updated.
     * @param subscriptionName The name of the subscription for which the rule is updated.
     * @param rule Information about the rule to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A Mono that returns the updated rule in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the rule quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link RuleProperties#getName()} is null or an empty string.
     * @throws NullPointerException if {@code rule} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RuleProperties> updateRuleWithResponse(String topicName, String subscriptionName,
        RuleProperties rule, Context context) {
        return asyncClient.updateRuleWithResponse(topicName, subscriptionName, rule,
            context != null ? context : Context.NONE).block();
    }

    /**
     * Updates a subscription with the given {@link SubscriptionProperties}. The {@link SubscriptionProperties} must be
     * fully populated as all of the properties are replaced. If a property is not set the service default value is
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
     * There are a subset of properties that can be updated. More information can be found in the links below. They are:
     * <ul>
     * <li>{@link SubscriptionProperties#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link SubscriptionProperties#setLockDuration(Duration) LockDuration}</li>
     * <li>{@link SubscriptionProperties#setMaxDeliveryCount(int) MaxDeliveryCount}</li>
     * </ul>
     *
     * @param subscription Information about the subscription to update. You must provide all the property values
     *     that are desired on the updated entity. Any values not provided are set to the service default values.
     *
     * @return Updated subscription in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the subscription quota is exceeded, or an
     *     error occurred processing the request.
     * @throws IllegalArgumentException if {@link SubscriptionProperties#getTopicName()} or {@link
     *     SubscriptionProperties#getSubscriptionName()} is null or an empty string.
     * @throws NullPointerException if {@code subscription} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionProperties updateSubscription(SubscriptionProperties subscription) {
        return asyncClient.updateSubscription(subscription).block();
    }

    /**
     * Updates a subscription with the given {@link SubscriptionProperties}. The {@link SubscriptionProperties} must be
     * fully populated as all of the properties are replaced. If a property is not set the service default value is
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
     * There are a subset of properties that can be updated. More information can be found in the links below. They are:
     * <ul>
     * <li>{@link SubscriptionProperties#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link SubscriptionProperties#setLockDuration(Duration) LockDuration}</li>
     * <li>{@link SubscriptionProperties#setMaxDeliveryCount(int) MaxDeliveryCount}</li>
     * </ul>
     *
     * @param subscription Information about the subscription to update. You must provide all the property values
     *     that are desired on the updated entity. Any values not provided are set to the service default values.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return Updated subscription in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the subscription quota is exceeded, or an
     *     error occurred processing the request.
     * @throws IllegalArgumentException if {@link SubscriptionProperties#getTopicName()} or {@link
     *     SubscriptionProperties#getSubscriptionName()} is null or an empty string.
     * @throws NullPointerException if {@code subscription} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SubscriptionProperties> updateSubscriptionWithResponse(
        SubscriptionProperties subscription, Context context) {
        return asyncClient.updateSubscriptionWithResponse(subscription, context != null ? context : Context.NONE)
            .block();
    }

    /**
     * Updates a topic with the given {@link TopicProperties}. The {@link TopicProperties} must be fully populated as
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
     * There are a subset of properties that can be updated. More information can be found in the links below. They are:
     * <ul>
     * <li>{@link TopicProperties#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link TopicProperties#setDuplicateDetectionHistoryTimeWindow(Duration) DuplicateDetectionHistoryTimeWindow}
     * </li>
     * </ul>
     *
     * @param topic Information about the topic to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     *
     * @return The updated topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link TopicProperties#getName() topic.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code topic} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-topic">Update Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TopicProperties updateTopic(TopicProperties topic) {
        return asyncClient.updateTopic(topic).block();
    }

    /**
     * Updates a topic with the given {@link TopicProperties}. The {@link TopicProperties} must be fully populated as
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
     * There are a subset of properties that can be updated. More information can be found in the links below. They are:
     * <ul>
     * <li>{@link TopicProperties#setDefaultMessageTimeToLive(Duration) DefaultMessageTimeToLive}</li>
     * <li>{@link TopicProperties#setDuplicateDetectionHistoryTimeWindow(Duration) DuplicateDetectionHistoryTimeWindow}
     * </li>
     * </ul>
     *
     * @param topic Information about the topic to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The updated topic with its HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link TopicProperties#getName() topic.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code topic} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-topic">Update Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TopicProperties> updateTopicWithResponse(TopicProperties topic, Context context) {
        return asyncClient.updateTopicWithResponse(topic, context != null ? context : Context.NONE).block();
    }
}
