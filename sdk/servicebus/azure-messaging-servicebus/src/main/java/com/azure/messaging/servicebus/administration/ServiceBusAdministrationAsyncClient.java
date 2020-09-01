// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.AzureException;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.NamespaceProperties;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeInfo;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionRuntimeInfo;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.administration.models.TopicRuntimeInfo;
import com.azure.messaging.servicebus.implementation.EntitiesImpl;
import com.azure.messaging.servicebus.implementation.EntityHelper;
import com.azure.messaging.servicebus.implementation.RulesImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.implementation.models.CreateRuleBody;
import com.azure.messaging.servicebus.implementation.models.CreateRuleBodyContent;
import com.azure.messaging.servicebus.implementation.models.CreateSubscriptionBody;
import com.azure.messaging.servicebus.implementation.models.CreateSubscriptionBodyContent;
import com.azure.messaging.servicebus.implementation.models.CreateTopicBody;
import com.azure.messaging.servicebus.implementation.models.CreateTopicBodyContent;
import com.azure.messaging.servicebus.implementation.models.NamespacePropertiesEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.implementation.models.RuleActionImpl;
import com.azure.messaging.servicebus.implementation.models.RuleDescription;
import com.azure.messaging.servicebus.implementation.models.RuleDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.RuleDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.RuleFilterImpl;
import com.azure.messaging.servicebus.implementation.models.ServiceBusManagementError;
import com.azure.messaging.servicebus.implementation.models.ServiceBusManagementErrorException;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescription;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.TopicDescription;
import com.azure.messaging.servicebus.implementation.models.TopicDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.TopicDescriptionFeed;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
 * @see ServiceBusAdministrationClient ServiceBusManagementClient for a synchronous client.
 */
@ServiceClient(builder = ServiceBusAdministrationClientBuilder.class, isAsync = true)
public final class ServiceBusAdministrationAsyncClient {
    // See https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers
    // for more information on Azure resource provider namespaces.
    private static final String SERVICE_BUS_TRACING_NAMESPACE_VALUE = "Microsoft.ServiceBus";
    private static final String CONTENT_TYPE = "application/xml";

    // Name of the entity type when listing queues and topics.
    private static final String QUEUES_ENTITY_TYPE = "queues";
    private static final String TOPICS_ENTITY_TYPE = "topics";

    private static final int NUMBER_OF_ELEMENTS = 100;

    private final ServiceBusManagementClientImpl managementClient;
    private final EntitiesImpl entityClient;
    private final ClientLogger logger = new ClientLogger(ServiceBusAdministrationAsyncClient.class);
    private final ServiceBusManagementSerializer serializer;
    private final RulesImpl rulesClient;

    /**
     * Creates a new instance with the given management client and serializer.
     *
     * @param managementClient Client to make management calls.
     * @param serializer Serializer to deserialize ATOM XML responses.
     */
    ServiceBusAdministrationAsyncClient(ServiceBusManagementClientImpl managementClient,
        ServiceBusManagementSerializer serializer) {
        this.serializer = Objects.requireNonNull(serializer, "'serializer' cannot be null.");
        this.managementClient = Objects.requireNonNull(managementClient, "'managementClient' cannot be null.");
        this.entityClient = managementClient.getEntities();
        this.rulesClient = managementClient.getRules();
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
    public Mono<QueueProperties> createQueue(String queueName) {
        try {
            return createQueue(queueName, new CreateQueueOptions());
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a queue with the {@link CreateQueueOptions} and given queue name.
     *
     * @param queueName Name of the queue to create.
     * @param queueOptions Options about the queue to create.
     *
     * @return A Mono that completes with information about the created queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} or {@code queueOptions} is null.
     * @throws ResourceExistsException if a queue exists with the same {@link QueueProperties#getName() queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueProperties> createQueue(String queueName, CreateQueueOptions queueOptions) {
        return createQueueWithResponse(queueName, queueOptions).map(Response::getValue);
    }

    /**
     * Creates a queue and returns the created queue in addition to the HTTP response.
     *
     * @param queueName Name of the queue to create.
     * @param queueOptions Options about the queue to create.
     *
     * @return A Mono that returns the created queue in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} or {@code queueOptions} is null.
     * @throws ResourceExistsException if a queue exists with the same {@link QueueProperties#getName() queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueProperties>> createQueueWithResponse(String queueName, CreateQueueOptions queueOptions) {
        return withContext(context -> createQueueWithResponse(queueName, queueOptions, context));
    }

    /**
     * Creates a rule under the given topic and subscription
     *
     * @param topicName Name of the topic associated with rule.
     * @param subscriptionName Name of the subscription associated with the rule.
     * @param ruleName Name of the rule.
     *
     * @return A Mono that completes with information about the created rule.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are are empty strings.
     * @throws NullPointerException if {@code topicName} or {@code ruleName} are are null.
     * @throws ResourceExistsException if a rule exists with the same topic, subscription, and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RuleProperties> createRule(String topicName, String subscriptionName, String ruleName) {
        try {
            return createRule(topicName, subscriptionName, ruleName, new CreateRuleOptions());
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a rule with the {@link CreateRuleOptions}.
     *
     * @param topicName Name of the topic associated with rule.
     * @param subscriptionName Name of the subscription associated with the rule.
     * @param ruleName Name of the rule.
     * @param ruleOptions Information about the rule to create.
     *
     * @return A Mono that completes with information about the created rule.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are are empty strings.
     * @throws NullPointerException if {@code topicName}, {@code ruleName}, or {@code ruleOptions}
     *     are are null.
     * @throws ResourceExistsException if a rule exists with the same topic and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RuleProperties> createRule(String topicName, String subscriptionName, String ruleName,
        CreateRuleOptions ruleOptions) {

        return createRuleWithResponse(topicName, subscriptionName, ruleName, ruleOptions)
            .map(Response::getValue);
    }

    /**
     * Creates a rule and returns the created rule in addition to the HTTP response.
     *
     * @param topicName Name of the topic associated with rule.
     * @param subscriptionName Name of the subscription associated with the rule.
     * @param ruleName Name of the rule.
     * @param ruleOptions Information about the rule to create.
     *
     * @return A Mono that returns the created rule in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are are empty strings.
     * @throws NullPointerException if {@code topicName}, {@code ruleName}, or {@code ruleOptions}
     *     are are null.
     * @throws ResourceExistsException if a rule exists with the same topic and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RuleProperties>> createRuleWithResponse(String topicName, String subscriptionName,
        String ruleName, CreateRuleOptions ruleOptions) {
        return withContext(context -> createRuleWithResponse(topicName, subscriptionName, ruleName, ruleOptions,
            context));
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are are empty strings.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} are are null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionProperties> createSubscription(String topicName, String subscriptionName) {
        try {
            return createSubscription(topicName, subscriptionName, new CreateSubscriptionOptions());
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a subscription with the {@link CreateSubscriptionOptions}.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param subscriptionOptions Information about the subscription to create.
     *
     * @return A Mono that completes with information about the created subscription.
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
    public Mono<SubscriptionProperties> createSubscription(String topicName, String subscriptionName,
        CreateSubscriptionOptions subscriptionOptions) {

        return createSubscriptionWithResponse(topicName, subscriptionName, subscriptionOptions)
            .map(Response::getValue);
    }

    /**
     * Creates a subscription and returns the created subscription in addition to the HTTP response.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param subscriptionOptions Information about the subscription to create.
     *
     * @return A Mono that returns the created subscription in addition to the HTTP response.
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
    public Mono<Response<SubscriptionProperties>> createSubscriptionWithResponse(String topicName,
        String subscriptionName, CreateSubscriptionOptions subscriptionOptions) {
        return withContext(context -> createSubscriptionWithResponse(topicName, subscriptionName, subscriptionOptions,
            context));
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
     * @throws NullPointerException if {@code topicName} is null.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws ResourceExistsException if a topic exists with the same {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TopicProperties> createTopic(String topicName) {
        try {
            return createTopic(topicName, new CreateTopicOptions());
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a topic with the {@link CreateTopicOptions}.
     *
     * @param topicName Name of the topic to create.
     * @param topicOptions The options used to create the topic.
     *
     * @return A Mono that completes with information about the created topic.
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
    public Mono<TopicProperties> createTopic(String topicName, CreateTopicOptions topicOptions) {
        return createTopicWithResponse(topicName, topicOptions).map(Response::getValue);
    }

    /**
     * Creates a topic and returns the created topic in addition to the HTTP response.
     *
     * @param topicName Name of the topic to create.
     * @param topicOptions The options used to create the topic.
     *
     * @return A Mono that returns the created topic in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link TopicProperties#getName() topic.getName()} is null or an empty
     *     string.
     * @throws NullPointerException if {@code topicName} or {@code topicOptions} is null.
     * @throws ResourceExistsException if a topic exists with the same {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TopicProperties>> createTopicWithResponse(String topicName, CreateTopicOptions topicOptions) {
        return withContext(context -> createTopicWithResponse(topicName, topicOptions, context));
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
     * @throws NullPointerException if {@code queueName} is null.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
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
     * @throws NullPointerException if {@code queueName} is null.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-queue">Delete Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteQueueWithResponse(String queueName) {
        return withContext(context -> deleteQueueWithResponse(queueName, context));
    }

    /**
     * Deletes a rule the matching {@code ruleName}.
     *
     * @param topicName Name of topic associated with rule to delete.
     * @param subscriptionName Name of the subscription associated with the rule to delete.
     * @param ruleName Name of rule to delete.
     *
     * @return A Mono that completes when the rule is deleted.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} is an empty string.
     * @throws NullPointerException if {@code topicName} or {@code ruleName} is null.
     * @throws ResourceNotFoundException if the {@code ruleName} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRule(String topicName, String subscriptionName, String ruleName) {
        return deleteRuleWithResponse(topicName, subscriptionName, ruleName).then();
    }

    /**
     * Deletes a rule the matching {@code ruleName} and returns the HTTP response.
     *
     * @param topicName Name of topic associated with rule to delete.
     * @param subscriptionName Name of the subscription associated with the rule to delete.
     * @param ruleName Name of rule to delete.
     *
     * @return A Mono that completes when the rule is deleted and returns the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName}, {@code subscriptionName}, or {@code ruleName} is an
     *     empty string.
     * @throws NullPointerException if {@code topicName}, {@code subscriptionName}, or {@code ruleName} is null.
     * @throws ResourceNotFoundException if the {@code ruleName} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRuleWithResponse(String topicName, String subscriptionName,
        String ruleName) {
        return withContext(context -> deleteRuleWithResponse(topicName, subscriptionName, ruleName, context));
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
    public Mono<QueueProperties> getQueue(String queueName) {
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
    public Mono<Response<QueueProperties>> getQueueWithResponse(String queueName) {
        return withContext(context -> getQueueWithResponse(queueName, context, Function.identity()));
    }

    /**
     * Gets whether or not a queue with {@code queueName} exists in the Service Bus namespace.
     *
     * @param queueName Name of the queue.
     *
     * @return A Mono that completes indicating whether or not the queue exists.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> getQueueExists(String queueName) {
        return getQueueExistsWithResponse(queueName).map(Response::getValue);
    }

    /**
     * Gets whether or not a queue with {@code queueName} exists in the Service Bus namespace.
     *
     * @param queueName Name of the queue.
     *
     * @return A Mono that completes indicating whether or not the queue exists along with its HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     * @throws NullPointerException if {@code queueName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> getQueueExistsWithResponse(String queueName) {
        return getEntityExistsWithResponse(getQueueWithResponse(queueName));
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
     * Gets information about the Service Bus namespace.
     *
     * @return A Mono that completes with information about the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to the namespace.
     * @throws HttpResponseException If error occurred processing the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamespaceProperties> getNamespaceProperties() {
        return getNamespacePropertiesWithResponse().map(Response::getValue);
    }

    /**
     * Gets information about the Service Bus namespace along with its HTTP response.
     *
     * @return A Mono that completes with information about the namespace and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NamespaceProperties>> getNamespacePropertiesWithResponse() {
        return withContext(this::getNamespacePropertiesWithResponse);
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
    public Mono<RuleProperties> getRule(String topicName, String subscriptionName, String ruleName) {
        return getRuleWithResponse(topicName, subscriptionName, ruleName).map(response -> response.getValue());
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
     *
     * @return The associated rule with the corresponding HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RuleProperties>> getRuleWithResponse(String topicName, String subscriptionName,
        String ruleName) {
        return withContext(context -> getRuleWithResponse(topicName, subscriptionName, ruleName, context));
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are empty strings.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} are null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist in the {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionProperties> getSubscription(String topicName, String subscriptionName) {
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are empty strings.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} are null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SubscriptionProperties>> getSubscriptionWithResponse(String topicName,
        String subscriptionName) {
        return withContext(context -> getSubscriptionWithResponse(topicName, subscriptionName, context,
            Function.identity()));
    }

    /**
     * Gets whether or not a subscription within a topic exists.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     *
     * @return A Mono that completes indicating whether or not the subscription exists.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code subscriptionName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> getSubscriptionExists(String topicName, String subscriptionName) {
        return getSubscriptionExistsWithResponse(topicName, subscriptionName).map(Response::getValue);
    }

    /**
     * Gets whether or not a subscription within a topic exists.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     *
     * @return A Mono that completes indicating whether or not the subscription exists along with its HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is an empty string.
     * @throws NullPointerException if {@code subscriptionName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> getSubscriptionExistsWithResponse(String topicName, String subscriptionName) {
        return getEntityExistsWithResponse(getSubscriptionWithResponse(topicName, subscriptionName));
    }

    /**
     * Gets runtime information about the subscription.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return A Mono that completes with runtime information about the subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are empty strings.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} are null.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionRuntimeInfo> getSubscriptionRuntimeInfo(String topicName, String subscriptionName) {
        return getSubscriptionRuntimeInfoWithResponse(topicName, subscriptionName)
            .map(response -> response.getValue());
    }

    /**
     * Gets runtime information about the subscription.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return A Mono that completes with runtime information about the subscription.
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
    public Mono<TopicProperties> getTopic(String topicName) {
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
    public Mono<Response<TopicProperties>> getTopicWithResponse(String topicName) {
        return withContext(context -> getTopicWithResponse(topicName, context, Function.identity()));
    }

    /**
     * Gets whether or not a topic with {@code topicName} exists in the Service Bus namespace.
     *
     * @param topicName Name of the topic.
     *
     * @return A Mono that completes indicating whether or not the topic exists.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> getTopicExists(String topicName) {
        return getTopicExistsWithResponse(topicName).map(Response::getValue);
    }

    /**
     * Gets whether or not a topic with {@code topicName} exists in the Service Bus namespace.
     *
     * @param topicName Name of the topic.
     *
     * @return A Mono that completes indicating whether or not the topic exists along with its HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @throws NullPointerException if {@code topicName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> getTopicExistsWithResponse(String topicName) {
        return getEntityExistsWithResponse(getTopicWithResponse(topicName));
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
     * @return A Flux of {@link QueueProperties queues} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<QueueProperties> listQueues() {
        return new PagedFlux<>(
            () -> withContext(context -> listQueuesFirstPage(context)),
            token -> withContext(context -> listQueuesNextPage(token, context)));
    }

    /**
     * Fetches all the rules for a topic and subscription.
     *
     * @param topicName The topic name under which all the rules need to be retrieved.
     * @param subscriptionName The name of the subscription for which all rules need to be retrieved.
     *
     * @return A Flux of {@link RuleProperties rules} for the {@code topicName} and {@code subscriptionName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} is null.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, rules, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<RuleProperties> listRules(String topicName, String subscriptionName) {
        if (topicName == null) {
            return pagedFluxError(logger, new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            return pagedFluxError(logger, new IllegalArgumentException("'topicName' cannot be an empty string."));
        }

        return new PagedFlux<>(
            () -> withContext(context -> listRulesFirstPage(topicName, subscriptionName, context)),
            token -> withContext(context -> listRulesNextPage(topicName, subscriptionName, token, context)));
    }

    /**
     * Fetches all the subscriptions for a topic.
     *
     * @param topicName The topic name under which all the subscriptions need to be retrieved.
     *
     * @return A Flux of {@link SubscriptionProperties subscriptions} for the {@code topicName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SubscriptionProperties> listSubscriptions(String topicName) {
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
     * @return A Flux of {@link TopicProperties topics} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TopicProperties> listTopics() {
        return new PagedFlux<>(
            () -> withContext(context -> listTopicsFirstPage(context)),
            token -> withContext(context -> listTopicsNextPage(token, context)));
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
     * @return A Mono that completes with the updated queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws NullPointerException if {@code queue} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-queue">Update Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueProperties> updateQueue(QueueProperties queue) {
        return updateQueueWithResponse(queue).map(Response::getValue);
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
     * @return A Mono that returns the updated queue in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws NullPointerException if {@code queue} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-queue">Update Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueProperties>> updateQueueWithResponse(QueueProperties queue) {
        return withContext(context -> updateQueueWithResponse(queue, context));
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
     * @return A Mono that returns the updated rule.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the rule quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@link RuleProperties#getName()} is null or an empty string.
     * @throws NullPointerException if {@code rule} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RuleProperties> updateRule(String topicName, String subscriptionName, RuleProperties rule) {
        return updateRuleWithResponse(topicName, subscriptionName, rule).map(Response::getValue);
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
    public Mono<Response<RuleProperties>> updateRuleWithResponse(String topicName, String subscriptionName,
        RuleProperties rule) {

        return withContext(context -> updateRuleWithResponse(topicName, subscriptionName, rule, context));
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
     * @return A Mono that returns the updated subscription.
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
    public Mono<SubscriptionProperties> updateSubscription(SubscriptionProperties subscription) {
        return updateSubscriptionWithResponse(subscription).map(Response::getValue);
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
     * @return A Mono that returns the updated subscription in addition to the HTTP response.
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
    public Mono<Response<SubscriptionProperties>> updateSubscriptionWithResponse(
        SubscriptionProperties subscription) {

        return withContext(context -> updateSubscriptionWithResponse(subscription, context));
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
     * @return A Mono that completes with the updated topic.
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
    public Mono<TopicProperties> updateTopic(TopicProperties topic) {
        return updateTopicWithResponse(topic).map(Response::getValue);
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
     * @return A Mono that completes with the updated topic and its HTTP response.
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
    public Mono<Response<TopicProperties>> updateTopicWithResponse(TopicProperties topic) {
        return withContext(context -> updateTopicWithResponse(topic, context));
    }

    /**
     * Creates a queue with its context.
     *
     * @param createQueueOptions Queue to create.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link QueueProperties}.
     */
    Mono<Response<QueueProperties>> createQueueWithResponse(String queueName, CreateQueueOptions createQueueOptions,
        Context context) {
        if (queueName == null) {
            return monoError(logger, new NullPointerException("'queueName' cannot be null."));
        } else if (queueName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'queueName' cannot be empty."));
        }

        if (createQueueOptions == null) {
            return monoError(logger, new NullPointerException("'createQueueOptions' cannot be null."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final QueueDescription description = EntityHelper.getQueueDescription(createQueueOptions);
        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType(CONTENT_TYPE)
            .setQueueDescription(description);
        final CreateQueueBody createEntity = new CreateQueueBody()
            .setContent(content);

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return entityClient.putWithResponseAsync(queueName, createEntity, null, withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(this::deserializeQueue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a rule with its context.
     *
     * @param ruleOptions Rule to create.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link RuleProperties}.
     */
    Mono<Response<RuleProperties>> createRuleWithResponse(String topicName, String subscriptionName, String ruleName,
        CreateRuleOptions ruleOptions, Context context) {
        if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be empty."));
        }

        if (subscriptionName == null) {
            return monoError(logger, new NullPointerException("'subscriptionName' cannot be null."));
        } else if (subscriptionName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'subscriptionName' cannot be empty."));
        }

        if (ruleName == null) {
            return monoError(logger, new NullPointerException("'ruleName' cannot be null."));
        } else if (ruleName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'ruleName' cannot be empty."));
        }

        if (ruleOptions == null) {
            return monoError(logger, new NullPointerException("'rule' cannot be null."));
        }

        final RuleActionImpl action = ruleOptions.getAction() != null
            ? EntityHelper.toImplementation(ruleOptions.getAction())
            : null;
        final RuleFilterImpl filter = ruleOptions.getFilter() != null
            ? EntityHelper.toImplementation(ruleOptions.getFilter())
            : null;
        final RuleDescription rule = new RuleDescription()
            .setAction(action)
            .setFilter(filter)
            .setName(ruleName);

        final CreateRuleBodyContent content = new CreateRuleBodyContent()
            .setType(CONTENT_TYPE)
            .setRuleDescription(rule);
        final CreateRuleBody createEntity = new CreateRuleBody().setContent(content);

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return managementClient.getRules().putWithResponseAsync(topicName, subscriptionName, ruleName, createEntity,
                null, withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> deserializeRule(response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a subscription with its context.
     *
     * @param subscriptionOptions Subscription to create.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link SubscriptionProperties}.
     */
    Mono<Response<SubscriptionProperties>> createSubscriptionWithResponse(String topicName, String subscriptionName,
        CreateSubscriptionOptions subscriptionOptions, Context context) {
        if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be empty."));
        }

        if (subscriptionName == null) {
            return monoError(logger, new NullPointerException("'subscriptionName' cannot be null."));
        } else if (subscriptionName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'subscriptionName' cannot be empty."));
        }

        if (subscriptionOptions == null) {
            return monoError(logger, new NullPointerException("'subscription' cannot be null."));
        }

        final SubscriptionDescription subscription = EntityHelper.getSubscriptionDescription(subscriptionOptions);
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(subscription);
        final CreateSubscriptionBody createEntity = new CreateSubscriptionBody().setContent(content);

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return managementClient.getSubscriptions().putWithResponseAsync(topicName, subscriptionName, createEntity,
                null, withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> deserializeSubscription(topicName, response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a topicOptions with its context.
     *
     * @param topicOptions Topic to create.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link TopicProperties}.
     */
    Mono<Response<TopicProperties>> createTopicWithResponse(String topicName, CreateTopicOptions topicOptions,
        Context context) {
        if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be empty."));
        }

        if (topicOptions == null) {
            return monoError(logger, new NullPointerException("'topicOptions' cannot be null"));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final TopicDescription topic = EntityHelper.getTopicDescription(topicOptions);
        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(topic);
        final CreateTopicBody createEntity = new CreateTopicBody()
            .setContent(content);

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return entityClient.putWithResponseAsync(topicName, createEntity, null, withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
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
     * @return A Mono that completes when the queue is deleted.
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
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> {
                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), null);
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a queue with its context.
     *
     * @param topicName Name of topic to delete.
     * @param subscriptionName Name of the subscription for the rule.
     * @param ruleName Name of the rule.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link QueueProperties}.
     */
    Mono<Response<Void>> deleteRuleWithResponse(String topicName, String subscriptionName, String ruleName,
        Context context) {
        if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null"));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (subscriptionName == null) {
            return monoError(logger, new NullPointerException("'subscriptionName' cannot be null"));
        } else if (subscriptionName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'subscriptionName' cannot be an empty string."));
        } else if (ruleName == null) {
            return monoError(logger, new NullPointerException("'ruleName' cannot be null"));
        } else if (ruleName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'ruleName' cannot be an empty string."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return rulesClient.deleteWithResponseAsync(topicName, subscriptionName, ruleName, withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
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
     * @return A Mono that completes with the created {@link SubscriptionProperties}.
     */
    Mono<Response<Void>> deleteSubscriptionWithResponse(String topicName, String subscriptionName, Context context) {
        if (subscriptionName == null) {
            return monoError(logger, new NullPointerException("'subscriptionName' cannot be null"));
        } else if (subscriptionName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'subscriptionName' cannot be an empty string."));
        } else if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null"));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return managementClient.getSubscriptions().deleteWithResponseAsync(topicName, subscriptionName,
                withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
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
     * @return A Mono that completes with the created {@link TopicProperties}.
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
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets whether an entity exists.
     *
     * @param getEntityOperation Operation to get information about entity. If {@link ResourceNotFoundException} is
     *     thrown, then it is mapped to false.
     * @param <T> Entity type.
     *
     * @return True if the entity exists, false otherwise.
     */
    <T> Mono<Response<Boolean>> getEntityExistsWithResponse(Mono<Response<T>> getEntityOperation) {
        return getEntityOperation.map(response -> {
            // When an entity does not exist, it does not have any description object in it.
            final boolean exists = response.getValue() != null;
            return (Response<Boolean>) new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), exists);
        })
            .onErrorResume(ResourceNotFoundException.class, exception -> {
                final HttpResponse response = exception.getResponse();
                final Response<Boolean> result = new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), false);

                return Mono.just(result);
            });
    }

    /**
     * Gets a queue with its context.
     *
     * @param queueName Name of queue to fetch information for.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the {@link QueueProperties}.
     */
    <T> Mono<Response<T>> getQueueWithResponse(String queueName, Context context,
        Function<QueueProperties, T> mapper) {
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
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .handle((response, sink) -> {
                    final Response<QueueProperties> deserialize = deserializeQueue(response);

                    // if this is null, then the queue could not be found.
                    if (deserialize.getValue() == null) {
                        final HttpResponse notFoundResponse = new EntityNotFoundHttpResponse<>(deserialize);
                        sink.error(new ResourceNotFoundException(String.format("Queue '%s' does not exist.", queueName),
                            notFoundResponse));
                    } else {
                        final T mapped = mapper.apply(deserialize.getValue());
                        sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), mapped));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<RuleProperties>> getRuleWithResponse(String topicName, String subscriptionName,
        String ruleName, Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return rulesClient.getWithResponseAsync(topicName, subscriptionName, ruleName, true, withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(this::deserializeRule);
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
     * @return A Mono that completes with the {@link SubscriptionProperties}.
     */
    <T> Mono<Response<T>> getSubscriptionWithResponse(String topicName, String subscriptionName, Context context,
        Function<SubscriptionProperties, T> mapper) {
        if (topicName == null) {
            return monoError(logger, new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (subscriptionName == null) {
            return monoError(logger, new NullPointerException("'subscriptionName' cannot be null."));
        } else if (subscriptionName.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'subscriptionName' cannot be an empty string."));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return managementClient.getSubscriptions().getWithResponseAsync(topicName, subscriptionName, true,
                withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .handle((response, sink) -> {
                    final Response<SubscriptionProperties> deserialize = deserializeSubscription(topicName, response);

                    // if this is null, then the queue could not be found.
                    if (deserialize.getValue() == null) {
                        final HttpResponse notFoundResponse = new EntityNotFoundHttpResponse<>(deserialize);
                        sink.error(new ResourceNotFoundException(String.format(
                            "Subscription '%s' in topic '%s' does not exist.", topicName, subscriptionName),
                            notFoundResponse));
                    } else {
                        final T mapped = mapper.apply(deserialize.getValue());
                        sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), mapped));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the namespace properties with its context.
     *
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the {@link NamespaceProperties}.
     */
    Mono<Response<NamespaceProperties>> getNamespacePropertiesWithResponse(Context context) {
        return managementClient.getNamespaces().getWithResponseAsync(context).handle((response, sink) -> {
            final NamespacePropertiesEntry entry = response.getValue();
            if (entry == null || entry.getContent() == null) {
                sink.error(new AzureException(
                    "There was no content inside namespace response. Entry: " + response));
                return;
            }

            final NamespaceProperties namespaceProperties = entry.getContent().getNamespaceProperties();
            final Response<NamespaceProperties> result = new SimpleResponse<>(response.getRequest(),
                response.getStatusCode(), response.getHeaders(), namespaceProperties);

            sink.next(result);
        });
    }

    /**
     * Gets a topic with its context.
     *
     * @param topicName Name of topic to fetch information for.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the {@link TopicProperties}.
     */
    <T> Mono<Response<T>> getTopicWithResponse(String topicName, Context context,
        Function<TopicProperties, T> mapper) {
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
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .handle((response, sink) -> {
                    final Response<TopicProperties> deserialize = deserializeTopic(response);

                    // if this is null, then the queue could not be found.
                    if (deserialize.getValue() == null) {
                        final HttpResponse notFoundResponse = new EntityNotFoundHttpResponse<>(deserialize);
                        sink.error(new ResourceNotFoundException(String.format("Topic '%s' does not exist.", topicName),
                            notFoundResponse));
                    } else {
                        final T mapped = mapper.apply(deserialize.getValue());
                        sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), mapped));
                    }
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
    Mono<PagedResponse<QueueProperties>> listQueuesFirstPage(Context context) {
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
    Mono<PagedResponse<QueueProperties>> listQueuesNextPage(String continuationToken, Context context) {
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
     * Gets the first page of rules with context.
     *
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with a page of rules.
     */
    Mono<PagedResponse<RuleProperties>> listRulesFirstPage(String topicName, String subscriptionName, Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            return listRules(topicName, subscriptionName, 0, withTracing);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets the next page of rules with context.
     *
     * @param continuationToken Number of items to skip in feed.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with a page of rules or empty if there are no items left.
     */
    Mono<PagedResponse<RuleProperties>> listRulesNextPage(String topicName, String subscriptionName,
        String continuationToken, Context context) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        try {
            final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);
            final int skip = Integer.parseInt(continuationToken);

            return listRules(topicName, subscriptionName, skip, withTracing);
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
    Mono<PagedResponse<SubscriptionProperties>> listSubscriptionsFirstPage(String topicName, Context context) {
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
    Mono<PagedResponse<SubscriptionProperties>> listSubscriptionsNextPage(String topicName, String continuationToken,
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
    Mono<PagedResponse<TopicProperties>> listTopicsFirstPage(Context context) {
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
    Mono<PagedResponse<TopicProperties>> listTopicsNextPage(String continuationToken, Context context) {
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
     * @return A Mono that completes with the updated {@link QueueProperties}.
     */
    Mono<Response<QueueProperties>> updateQueueWithResponse(QueueProperties queue, Context context) {
        if (queue == null) {
            return monoError(logger, new NullPointerException("'queue' cannot be null"));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final QueueDescription queueDescription = EntityHelper.toImplementation(queue);
        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType(CONTENT_TYPE)
            .setQueueDescription(queueDescription);
        final CreateQueueBody createEntity = new CreateQueueBody()
            .setContent(content);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return entityClient.putWithResponseAsync(queue.getName(), createEntity, "*", withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> deserializeQueue(response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates a rule with its context.
     *
     * @param rule Information about the rule to update. You must provide all the property values that are desired
     *     on the updated entity. Any values not provided are set to the service default values.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the updated {@link RuleProperties}.
     */
    Mono<Response<RuleProperties>> updateRuleWithResponse(String topicName, String subscriptionName,
        RuleProperties rule, Context context) {
        if (rule == null) {
            return monoError(logger, new NullPointerException("'rule' cannot be null"));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final RuleDescription implementation = EntityHelper.toImplementation(rule);
        final CreateRuleBodyContent content = new CreateRuleBodyContent()
            .setType(CONTENT_TYPE)
            .setRuleDescription(implementation);
        final CreateRuleBody ruleBody = new CreateRuleBody()
            .setContent(content);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return managementClient.getRules().putWithResponseAsync(topicName, subscriptionName, rule.getName(),
                ruleBody, "*", withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> deserializeRule(response));
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
     * @return A Mono that completes with the updated {@link SubscriptionProperties}.
     */
    Mono<Response<SubscriptionProperties>> updateSubscriptionWithResponse(SubscriptionProperties subscription,
        Context context) {
        if (subscription == null) {
            return monoError(logger, new NullPointerException("'subscription' cannot be null"));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final String topicName = subscription.getTopicName();
        final String subscriptionName = subscription.getSubscriptionName();
        final SubscriptionDescription implementation = EntityHelper.toImplementation(subscription);
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(implementation);
        final CreateSubscriptionBody createEntity = new CreateSubscriptionBody()
            .setContent(content);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return managementClient.getSubscriptions().putWithResponseAsync(topicName, subscriptionName, createEntity,
                "*", withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
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
     * @return A Mono that completes with the updated {@link TopicProperties}.
     */
    Mono<Response<TopicProperties>> updateTopicWithResponse(TopicProperties topic, Context context) {
        if (topic == null) {
            return monoError(logger, new NullPointerException("'topic' cannot be null"));
        } else if (context == null) {
            return monoError(logger, new NullPointerException("'context' cannot be null."));
        }

        final TopicDescription implementation = EntityHelper.toImplementation(topic);
        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(implementation);
        final CreateTopicBody createEntity = new CreateTopicBody()
            .setContent(content);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, SERVICE_BUS_TRACING_NAMESPACE_VALUE);

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return entityClient.putWithResponseAsync(topic.getName(), createEntity, "*", withTracing)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
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
     * QueueProperties}.
     *
     * @param response HTTP Response to deserialize.
     *
     * @return The corresponding HTTP response with convenience properties set.
     */
    private Response<QueueProperties> deserializeQueue(Response<Object> response) {
        final QueueDescriptionEntry entry = deserialize(response.getValue(), QueueDescriptionEntry.class);

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            logger.info("entry.getContent() is null. The entity may not exist. {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final QueueProperties result = EntityHelper.toModel(entry.getContent().getQueueDescription());
        final String queueName = getTitleValue(entry.getTitle());
        EntityHelper.setQueueName(result, queueName);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), result);
    }

    /**
     * Converts a Response into its corresponding {@link RuleDescriptionEntry} then mapped into {@link RuleProperties}.
     *
     * @param response HTTP Response to deserialize.
     *
     * @return The corresponding HTTP response with convenience properties set.
     */
    private Response<RuleProperties> deserializeRule(Response<Object> response) {
        final RuleDescriptionEntry entry = deserialize(response.getValue(), RuleDescriptionEntry.class);

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            logger.info("entry.getContent() is null. The entity may not exist. {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final RuleDescription description = entry.getContent().getRuleDescription();
        final RuleProperties result = EntityHelper.toModel(description);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), result);
    }

    /**
     * Converts a Response into its corresponding {@link SubscriptionDescriptionEntry} then mapped into {@link
     * SubscriptionProperties}.
     *
     * @param response HTTP Response to deserialize.
     *
     * @return The corresponding HTTP response with convenience properties set.
     */
    private Response<SubscriptionProperties> deserializeSubscription(String topicName, Response<Object> response) {
        final SubscriptionDescriptionEntry entry = deserialize(response.getValue(), SubscriptionDescriptionEntry.class);

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            logger.warning("entry.getContent() is null. There should have been content returned. Entry: {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final SubscriptionProperties subscription = EntityHelper.toModel(
            entry.getContent().getSubscriptionDescription());
        final String subscriptionName = getTitleValue(entry.getTitle());
        EntityHelper.setSubscriptionName(subscription, subscriptionName);
        EntityHelper.setTopicName(subscription, topicName);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            subscription);
    }

    /**
     * Converts a Response into its corresponding {@link TopicDescriptionEntry} then mapped into {@link
     * QueueProperties}.
     *
     * @param response HTTP Response to deserialize.
     *
     * @return The corresponding HTTP response with convenience properties set.
     */
    private Response<TopicProperties> deserializeTopic(Response<Object> response) {
        final TopicDescriptionEntry entry = deserialize(response.getValue(), TopicDescriptionEntry.class);

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            logger.warning("entry.getContent() is null. There should have been content returned. Entry: {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final TopicProperties result = EntityHelper.toModel(entry.getContent().getTopicDescription());
        final String topicName = getTitleValue(entry.getTitle());
        EntityHelper.setTopicName(result, topicName);

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
    private Mono<PagedResponse<QueueProperties>> listQueues(int skip, Context context) {
        return managementClient.listEntitiesWithResponseAsync(QUEUES_ENTITY_TYPE, skip, NUMBER_OF_ELEMENTS, context)
            .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
            .flatMap(response -> {
                final Response<QueueDescriptionFeed> feedResponse = deserialize(response, QueueDescriptionFeed.class);
                final QueueDescriptionFeed feed = feedResponse.getValue();
                if (feed == null) {
                    logger.warning("Could not deserialize QueueDescriptionFeed. skip {}, top: {}", skip,
                        NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<QueueProperties> entities = feed.getEntry().stream()
                    .filter(e -> e.getContent() != null && e.getContent().getQueueDescription() != null)
                    .map(e -> {
                        final String queueName = getTitleValue(e.getTitle());
                        final QueueProperties queueProperties = EntityHelper.toModel(
                            e.getContent().getQueueDescription());

                        EntityHelper.setQueueName(queueProperties, queueName);

                        return queueProperties;
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
     * @return A Mono that completes with a paged response of rules.
     */
    private Mono<PagedResponse<RuleProperties>> listRules(String topicName, String subscriptionName, int skip,
        Context context) {
        return managementClient.listRulesWithResponseAsync(topicName, subscriptionName, skip, NUMBER_OF_ELEMENTS,
            context)
            .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
            .flatMap(response -> {
                final Response<RuleDescriptionFeed> feedResponse = deserialize(response,
                    RuleDescriptionFeed.class);

                final RuleDescriptionFeed feed = feedResponse.getValue();
                if (feed == null) {
                    logger.warning("Could not deserialize RuleDescriptionFeed. skip {}, top: {}", skip,
                        NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<RuleProperties> entities = feed.getEntry().stream()
                    .filter(e -> e.getContent() != null && e.getContent().getRuleDescription() != null)
                    .map(e -> {
                        return EntityHelper.toModel(e.getContent().getRuleDescription());
                    })
                    .collect(Collectors.toList());
                try {
                    return Mono.just(extractPage(feedResponse, entities, feed.getLink()));
                } catch (MalformedURLException | UnsupportedEncodingException error) {
                    return Mono.error(new RuntimeException(
                        "Could not parse response into FeedPage<RuleDescription>", error));
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
    private Mono<PagedResponse<SubscriptionProperties>> listSubscriptions(String topicName, int skip,
        Context context) {
        return managementClient.listSubscriptionsWithResponseAsync(topicName, skip, NUMBER_OF_ELEMENTS, context)
            .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
            .flatMap(response -> {
                final Response<SubscriptionDescriptionFeed> feedResponse = deserialize(response,
                    SubscriptionDescriptionFeed.class);

                final SubscriptionDescriptionFeed feed = feedResponse.getValue();
                if (feed == null) {
                    logger.warning("Could not deserialize SubscriptionDescriptionFeed. skip {}, top: {}", skip,
                        NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<SubscriptionProperties> entities = feed.getEntry().stream()
                    .filter(e -> e.getContent() != null && e.getContent().getSubscriptionDescription() != null)
                    .map(e -> {
                        final String subscriptionName = getTitleValue(e.getTitle());
                        final SubscriptionProperties description = EntityHelper.toModel(
                            e.getContent().getSubscriptionDescription());

                        EntityHelper.setTopicName(description, topicName);
                        EntityHelper.setSubscriptionName(description, subscriptionName);

                        return description;
                    })
                    .collect(Collectors.toList());
                try {
                    return Mono.just(extractPage(feedResponse, entities, feed.getLink()));
                } catch (MalformedURLException | UnsupportedEncodingException error) {
                    return Mono.error(new RuntimeException(
                        "Could not parse response into FeedPage<SubscriptionDescription>", error));
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
    private Mono<PagedResponse<TopicProperties>> listTopics(int skip, Context context) {
        return managementClient.listEntitiesWithResponseAsync(TOPICS_ENTITY_TYPE, skip, NUMBER_OF_ELEMENTS, context)
            .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
            .flatMap(response -> {
                final Response<TopicDescriptionFeed> feedResponse = deserialize(response, TopicDescriptionFeed.class);
                final TopicDescriptionFeed feed = feedResponse.getValue();
                if (feed == null) {
                    logger.warning("Could not deserialize TopicDescriptionFeed. skip {}, top: {}", skip,
                        NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<TopicProperties> entities = feed.getEntry().stream()
                    .filter(e -> e.getContent() != null && e.getContent().getTopicDescription() != null)
                    .map(e -> {
                        final String topicName = getTitleValue(e.getTitle());
                        final TopicProperties topicProperties = EntityHelper.toModel(
                            e.getContent().getTopicDescription());
                        EntityHelper.setTopicName(topicProperties, topicName);

                        return topicProperties;
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

    private static final class EntityNotFoundHttpResponse<T> extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;

        private EntityNotFoundHttpResponse(Response<T> response) {
            super(response.getRequest());
            this.headers = response.getHeaders();
            this.statusCode = response.getStatusCode();
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.empty();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.empty();
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.empty();
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.empty();
        }
    }
}
