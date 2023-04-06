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
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.implementation.EntitiesImpl;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.RulesImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBody;
import com.azure.messaging.servicebus.administration.implementation.models.NamespacePropertiesEntry;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementError;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementErrorException;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescription;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.http.policy.AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.NUMBER_OF_ELEMENTS;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.QUEUES_ENTITY_TYPE;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.TOPICS_ENTITY_TYPE;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.addSupplementaryAuthHeader;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.extractPage;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getContext;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getCreateQueueBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getCreateRuleBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getCreateSubscriptionBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getCreateTopicBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getQueues;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getRulePropertiesSimpleResponse;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getRules;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getSubscriptionPropertiesSimpleResponse;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getSubscriptions;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getTitleValue;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getTopics;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getUpdateRuleBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getUpdateTopicBody;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;

/**
 * An <b>asynchronous</b> client for managing a Service Bus namespace. Instantiated via
 * {@link ServiceBusAdministrationClientBuilder}.
 *
 * <p><strong>Create a queue</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.createqueue#string -->
 * <pre>
 * &#47;&#47; `.subscribe&#40;&#41;` is a non-blocking call. It'll move onto the next
 * &#47;&#47; instruction after setting up the `consumer` and `errorConsumer` callbacks.
 * client.createQueue&#40;&quot;my-new-queue&quot;&#41;.subscribe&#40;queue -&gt; &#123;
 *     System.out.printf&#40;&quot;Queue created. Name: %s. Lock Duration: %s.%n&quot;,
 *         queue.getName&#40;&#41;, queue.getLockDuration&#40;&#41;&#41;;
 * &#125;, error -&gt; &#123;
 *         System.err.println&#40;&quot;Error creating queue: &quot; + error&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.createqueue#string -->
 *
 * <p><strong>Edit an existing subscription</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.updatesubscription#subscriptionproperties -->
 * <pre>
 * &#47;&#47; To update the subscription we have to:
 * &#47;&#47; 1. Get the subscription info from the service.
 * &#47;&#47; 2. Update the SubscriptionProperties we want to change.
 * &#47;&#47; 3. Call the updateSubscription&#40;&#41; with the updated object.
 *
 * &#47;&#47; `.subscribe&#40;&#41;` is a non-blocking call. It'll move onto the next
 * &#47;&#47; instruction after setting up the `consumer` and `errorConsumer` callbacks.
 * client.getSubscription&#40;&quot;my-topic&quot;, &quot;my-subscription&quot;&#41;
 *     .flatMap&#40;subscription -&gt; &#123;
 *         System.out.println&#40;&quot;Original delivery count: &quot; + subscription.getMaxDeliveryCount&#40;&#41;&#41;;
 *
 *         &#47;&#47; Updating it to a new value.
 *         subscription.setMaxDeliveryCount&#40;5&#41;;
 *
 *         &#47;&#47; Persisting the updates to the subscription object.
 *         return client.updateSubscription&#40;subscription&#41;;
 *     &#125;&#41;
 *     .subscribe&#40;subscription -&gt; &#123;
 *         System.out.printf&#40;&quot;Subscription updated. Name: %s. Delivery count: %s.%n&quot;,
 *             subscription.getSubscriptionName&#40;&#41;, subscription.getMaxDeliveryCount&#40;&#41;&#41;;
 *     &#125;, error -&gt; &#123;
 *             System.err.println&#40;&quot;Error updating subscription: &quot; + error&#41;;
 *         &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.updatesubscription#subscriptionproperties -->
 *
 * <p><strong>List all queues</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.listQueues -->
 * <pre>
 * &#47;&#47; `.subscribe&#40;&#41;` is a non-blocking call. It'll move onto the next
 * &#47;&#47; instruction after setting up the `consumer` and `errorConsumer` callbacks.
 * client.listQueues&#40;&#41;.subscribe&#40;queue -&gt; &#123;
 *     System.out.printf&#40;&quot;Queue [%s]. Lock Duration: %s.%n&quot;,
 *         queue.getName&#40;&#41;, queue.getLockDuration&#40;&#41;&#41;;
 * &#125;, error -&gt; &#123;
 *         System.err.println&#40;&quot;Error fetching queues: &quot; + error&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.listQueues -->
 *
 * @see ServiceBusAdministrationClientBuilder
 * @see ServiceBusAdministrationClient ServiceBusAdministrationClient for a synchronous client.
 */
@ServiceClient(builder = ServiceBusAdministrationClientBuilder.class, isAsync = true)
public final class ServiceBusAdministrationAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusAdministrationAsyncClient.class);

    private final ServiceBusManagementClientImpl managementClient;
    private final EntitiesImpl entityClient;
    private final ServiceBusManagementSerializer serializer;
    private final RulesImpl rulesClient;

    /**
     * Creates a new instance with the given management client and serializer.
     *
     * @param managementClient Client to make management calls.
     * @param serializer Serializer to deserialize ATOM XML responses.
     *
     * @throws NullPointerException if any one of {@code managementClient, serializer, credential} is null.
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
     * @throws IllegalArgumentException if {@code queueName} is null or is an empty string.
     * @throws ResourceExistsException if a queue exists with the same {@code queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueProperties> createQueue(String queueName) {
        try {
            return createQueue(queueName, new CreateQueueOptions());
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * @throws IllegalArgumentException if {@code queueName} is null or is an empty string.
     * @throws ResourceExistsException if a queue exists with the same {@link QueueProperties#getName() queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueProperties> createQueue(String queueName, CreateQueueOptions queueOptions) {
        return createQueueWithResponse(queueName, queueOptions).map(response -> response.getValue());
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
     * @throws IllegalArgumentException if {@code queueName} is null or is an empty string.
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
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are null or empty strings.
     * @throws ResourceExistsException if a rule exists with the same topic, subscription, and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RuleProperties> createRule(String topicName, String subscriptionName, String ruleName) {
        try {
            return createRule(topicName, subscriptionName, ruleName, new CreateRuleOptions());
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are null or empty strings.
     * @throws NullPointerException {@code ruleOptions} are null.
     * @throws ResourceExistsException if a rule exists with the same topic and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RuleProperties> createRule(String topicName, String subscriptionName, String ruleName,
        CreateRuleOptions ruleOptions) {

        return createRuleWithResponse(topicName, subscriptionName, ruleName, ruleOptions)
            .map(response -> response.getValue());
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
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are null or empty strings.
     * @throws NullPointerException if {@code ruleOptions} is null.
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionProperties> createSubscription(String topicName, String subscriptionName) {
        try {
            return createSubscription(topicName, subscriptionName, new CreateSubscriptionOptions());
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws NullPointerException if {@code subscriptionOptions} is null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionProperties> createSubscription(String topicName, String subscriptionName,
        CreateSubscriptionOptions subscriptionOptions) {

        return createSubscriptionWithResponse(topicName, subscriptionName, subscriptionOptions)
            .map(response -> response.getValue());
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws NullPointerException if {@code subscriptionOptions} is null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SubscriptionProperties>> createSubscriptionWithResponse(String topicName,
        String subscriptionName, CreateSubscriptionOptions subscriptionOptions) {
        // Create with no default rule. RuleOptions to be set to null.
        return withContext(context -> createSubscriptionWithResponse(topicName, subscriptionName, null,
            subscriptionOptions, null, context));
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
    public Mono<TopicProperties> createTopic(String topicName) {
        try {
            return createTopic(topicName, new CreateTopicOptions());
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws NullPointerException if {@code topicOptions} is null.
     * @throws ResourceExistsException if a topic exists with the same {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TopicProperties> createTopic(String topicName, CreateTopicOptions topicOptions) {
        return createTopicWithResponse(topicName, topicOptions).map(response -> response.getValue());
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
     * @throws NullPointerException if {@code topicOptions} is null.
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
     * @throws IllegalArgumentException if {@code queueName} is null or is an empty string.
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
     * @throws IllegalArgumentException if {@code queueName} is null or is an empty string.
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
     * @throws IllegalArgumentException if {@code topicName}, {@code subscriptionName}, or {@code ruleName} is null or
     *      an empty string.
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
     * @throws IllegalArgumentException if {@code topicName}, {@code subscriptionName}, or {@code ruleName} is null or
     *     an empty string.
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is null or an empty string.
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is null or an empty string.
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
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
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
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
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
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueProperties> getQueue(String queueName) {
        return getQueueWithResponse(queueName).map(response -> response.getValue());
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
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueProperties>> getQueueWithResponse(String queueName) {
        return withContext(context -> getQueueWithResponse(queueName, context, Function.identity()));
    }

    /**
     *  Gets whether or not a queue with {@code queueName} exists in the Service Bus namespace.
     *
     * @param queueName Name of the queue.
     *
     * @return A Mono that completes indicating whether the queue exists.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     * namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> getQueueExists(String queueName) {
        return getQueueExistsWithResponse(queueName).map(response -> response.getValue());
    }

    /**
     *  Gets whether or not a queue with {@code queueName} exists in the Service Bus namespace.
     *
     * @param queueName Name of the queue.
     *
     * @return A Mono that completes indicating whether or not the queue exists along with its HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> getQueueExistsWithResponse(String queueName) {
        return getEntityExistsWithResponse(getQueueWithResponse(queueName));
    }

    /**
     * Gets runtime properties about the queue.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return A Mono that completes with runtime properties about the queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueRuntimeProperties> getQueueRuntimeProperties(String queueName) {
        return getQueueRuntimePropertiesWithResponse(queueName).map(response -> response.getValue());
    }

    /**
     * Gets runtime properties about the queue along with its HTTP response.
     *
     * @param queueName Name of queue to get information about.
     *
     * @return A Mono that completes with runtime properties about the queue and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueRuntimeProperties>> getQueueRuntimePropertiesWithResponse(String queueName) {
        return withContext(context -> getQueueWithResponse(queueName, context, QueueRuntimeProperties::new));
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
        return getNamespacePropertiesWithResponse().map(response -> response.getValue());
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist in the {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionProperties> getSubscription(String topicName, String subscriptionName) {
        return getSubscriptionWithResponse(topicName, subscriptionName).map(response -> response.getValue());
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
     * @throws IllegalArgumentException if {@code subscriptionName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> getSubscriptionExists(String topicName, String subscriptionName) {
        return getSubscriptionExistsWithResponse(topicName, subscriptionName).map(response -> response.getValue());
    }

    /**
     * Gets whether or not a subscription within a topic exists.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     *
     * @return A Mono that completes indicating whether the subscription exists along with its HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> getSubscriptionExistsWithResponse(String topicName, String subscriptionName) {
        return getEntityExistsWithResponse(getSubscriptionWithResponse(topicName, subscriptionName));
    }

    /**
     * Gets runtime properties about the subscription.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return A Mono that completes with runtime properties about the subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionRuntimeProperties> getSubscriptionRuntimeProperties(
        String topicName, String subscriptionName) {
        return getSubscriptionRuntimePropertiesWithResponse(topicName, subscriptionName)
            .map(response -> response.getValue());
    }

    /**
     * Gets runtime properties about the subscription.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     *
     * @return A Mono that completes with runtime properties about the subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SubscriptionRuntimeProperties>> getSubscriptionRuntimePropertiesWithResponse(
        String topicName, String subscriptionName) {
        return withContext(context -> getSubscriptionWithResponse(topicName, subscriptionName, context,
            SubscriptionRuntimeProperties::new));
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
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TopicProperties> getTopic(String topicName) {
        return getTopicWithResponse(topicName).map(response -> response.getValue());
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
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
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
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> getTopicExists(String topicName) {
        return getTopicExistsWithResponse(topicName).map(response -> response.getValue());
    }

    /**
     * Gets whether or not a topic with {@code topicName} exists in the Service Bus namespace.
     *
     * @param topicName Name of the topic.
     *
     * @return A Mono that completes indicating whether the topic exists along with its HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or  an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> getTopicExistsWithResponse(String topicName) {
        return getEntityExistsWithResponse(getTopicWithResponse(topicName));
    }

    /**
     * Gets runtime properties about the topic.
     *
     * @param topicName Name of topic to get information about.
     *
     * @return A Mono that completes with runtime properties about the topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TopicRuntimeProperties> getTopicRuntimeProperties(String topicName) {
        return getTopicRuntimePropertiesWithResponse(topicName).map(response -> response.getValue());
    }

    /**
     * Gets runtime properties about the topic with its HTTP response.
     *
     * @param topicName Name of topic to get information about.
     *
     * @return A Mono that completes with runtime properties about the topic and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TopicRuntimeProperties>> getTopicRuntimePropertiesWithResponse(String topicName) {
        return withContext(context -> getTopicWithResponse(topicName, context, TopicRuntimeProperties::new));
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
        return new PagedFlux<>(() -> withContext(context -> listQueuesFirstPage(context)),
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
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is null or an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, rules, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<RuleProperties> listRules(String topicName, String subscriptionName) {
        if (topicName == null) {
            return pagedFluxError(LOGGER, new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            return pagedFluxError(LOGGER, new IllegalArgumentException("'topicName' cannot be an empty string."));
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
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SubscriptionProperties> listSubscriptions(String topicName) {
        if (topicName == null) {
            return pagedFluxError(LOGGER, new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            return pagedFluxError(LOGGER, new IllegalArgumentException("'topicName' cannot be an empty string."));
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
        return updateQueueWithResponse(queue).map(response -> response.getValue());
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
     * the properties are replaced. If a property is not set the service default value is used.
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
        return updateRuleWithResponse(topicName, subscriptionName, rule).map(response -> response.getValue());
    }

    /**
     * Updates a rule with the given {@link RuleProperties}. The {@link RuleProperties} must be fully populated as all
     * the properties are replaced. If a property is not set the service default value is used.
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
        return updateSubscriptionWithResponse(subscription).map(response -> response.getValue());
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
        return updateTopicWithResponse(topic).map(response -> response.getValue());
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
     * Creates a subscription with a default rule using {@link CreateSubscriptionOptions} and {@link CreateRuleOptions}.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param ruleName Name of the default rule the subscription should be created with.
     * @param subscriptionOptions A {@link CreateSubscriptionOptions} object describing the subscription to create.
     * @param ruleOptions A {@link CreateRuleOptions} object describing the default rule.
     *                    If null, then pass-through filter will be created.
     *
     * @return A Mono that completes with information about the created subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws NullPointerException if {@code subscriptionOptions} is null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SubscriptionProperties> createSubscription(String topicName, String subscriptionName, String ruleName,
                                                           CreateSubscriptionOptions subscriptionOptions,
                                                           CreateRuleOptions ruleOptions) {

        return createSubscriptionWithResponse(topicName, subscriptionName, ruleName, subscriptionOptions, ruleOptions)
            .map(Response::getValue);
    }

    /**
     * Creates a subscription with default rule and returns the created subscription in addition to the HTTP response.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param ruleName Name of the default rule the subscription should be created with.
     * @param subscriptionOptions A {@link CreateSubscriptionOptions} object describing the subscription to create.
     * @param ruleOptions A {@link CreateRuleOptions} object describing the default rule.
     *                    If null, then pass-through filter will be created.
     *
     * @return A Mono that returns the created subscription in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     *     processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws NullPointerException if {@code subscriptionOptions} is null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SubscriptionProperties>> createSubscriptionWithResponse(String topicName,
                                                                                 String subscriptionName,
                                                                                 String ruleName,
                                                                                 CreateSubscriptionOptions subscriptionOptions,
                                                                                 CreateRuleOptions ruleOptions) {
        return withContext(context -> createSubscriptionWithResponse(topicName, subscriptionName, ruleName,
            subscriptionOptions, ruleOptions, context));
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
        if (CoreUtils.isNullOrEmpty(queueName)) {
            return monoError(LOGGER, new IllegalArgumentException("'queueName' cannot be null or empty."));
        }
        if (createQueueOptions == null) {
            return monoError(LOGGER, new NullPointerException("'createQueueOptions' cannot be null."));
        }
        context = context == null ? Context.NONE : context;
        final Context contextWithHeaders
            = getContext(context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders()));

        final String forwardTo = getForwardToEntity(createQueueOptions.getForwardTo(), contextWithHeaders);
        if (forwardTo != null) {
            createQueueOptions.setForwardTo(forwardTo);
        }
        final String forwardDlq
            = getForwardDlqEntity(createQueueOptions.getForwardDeadLetteredMessagesTo(), contextWithHeaders);
        if (forwardDlq != null) {
            createQueueOptions.setForwardDeadLetteredMessagesTo(forwardDlq);
        }
        final CreateQueueBody createEntity =
            getCreateQueueBody(EntityHelper.getQueueDescription(createQueueOptions));
        try {
            return entityClient.putWithResponseAsync(queueName, createEntity, null, contextWithHeaders)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(this::deserializeQueue);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a rule with its context.
     *
     * @param ruleOptions Rule to create.
     * @param context Context to pass into request.
     *
     *
     * @return A Mono that completes with the created {@link RuleProperties}.
     */
    Mono<Response<RuleProperties>> createRuleWithResponse(String topicName, String subscriptionName, String ruleName,
        CreateRuleOptions ruleOptions, Context context) {
        if (CoreUtils.isNullOrEmpty(topicName)) {
            return monoError(LOGGER, new IllegalArgumentException("'topicName' cannot be null or empty."));
        }

        if (CoreUtils.isNullOrEmpty(subscriptionName)) {
            return monoError(LOGGER, new IllegalArgumentException("'subscriptionName' cannot be null or empty."));
        }

        if (CoreUtils.isNullOrEmpty(ruleName)) {
            return monoError(LOGGER, new IllegalArgumentException("'ruleName' cannot be null or empty."));
        }

        if (ruleOptions == null) {
            return monoError(LOGGER, new NullPointerException("'ruleOptions' cannot be null."));
        }
        final CreateRuleBody createEntity = getCreateRuleBody(ruleName, ruleOptions);
        try {
            return managementClient.getRules().putWithResponseAsync(topicName, subscriptionName, ruleName, createEntity,
                null, getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(this::deserializeRule);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }


    /**
     * Creates a subscription with its context.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param ruleName Name of the default rule the subscription should be created with.
     * @param subscriptionOptions A {@link CreateSubscriptionOptions} object describing the subscription to create.
     * @param ruleOptions A {@link CreateRuleOptions} object describing the default rule.
     *                    If null, then pass-through filter will be created.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link SubscriptionProperties}.
     */
    Mono<Response<SubscriptionProperties>> createSubscriptionWithResponse(String topicName, String subscriptionName,
        String ruleName, CreateSubscriptionOptions subscriptionOptions, CreateRuleOptions ruleOptions, Context context) {
        if (CoreUtils.isNullOrEmpty(topicName)) {
            return monoError(LOGGER, new IllegalArgumentException("'topicName' cannot be null or empty."));
        }

        if (CoreUtils.isNullOrEmpty(subscriptionName)) {
            return monoError(LOGGER, new IllegalArgumentException("'subscriptionName' cannot be null or empty."));
        }

        if (subscriptionOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'subscriptionOptions' cannot be null."));
        }
        context = context == null ? Context.NONE : context;
        final Context contextWithHeaders
            = getContext(context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders()));

        final String forwardTo = getForwardToEntity(subscriptionOptions.getForwardTo(), contextWithHeaders);
        if (forwardTo != null) {
            subscriptionOptions.setForwardTo(forwardTo);
        }
        final String forwardDlq
            = getForwardDlqEntity(subscriptionOptions.getForwardDeadLetteredMessagesTo(), contextWithHeaders);
        if (forwardDlq != null) {
            subscriptionOptions.setForwardDeadLetteredMessagesTo(forwardDlq);
        }

        if (ruleOptions != null) {
            if (ruleOptions.getFilter() == null) {
                return monoError(LOGGER, new IllegalArgumentException("'RuleFilter' cannot be null."));
            }
            final RuleDescription rule = new RuleDescription()
                .setAction(ruleOptions.getAction() != null ? EntityHelper.toImplementation(ruleOptions.getAction()) : null)
                .setFilter(EntityHelper.toImplementation(ruleOptions.getFilter()))
                .setName(ruleName);
            subscriptionOptions.setDefaultRule(EntityHelper.toModel(rule));
        }
        final CreateSubscriptionBody createEntity =
                getCreateSubscriptionBody(EntityHelper.getSubscriptionDescription(subscriptionOptions));
        try {
            return managementClient.getSubscriptions().putWithResponseAsync(topicName, subscriptionName, createEntity,
                null, contextWithHeaders)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> deserializeSubscription(topicName, response));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
        if (CoreUtils.isNullOrEmpty(topicName)) {
            return monoError(LOGGER, new IllegalArgumentException("'topicName' cannot be null or empty."));
        }
        if (topicOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'topicOptions' cannot be null."));
        }
        final CreateTopicBody createEntity = getCreateTopicBody(EntityHelper.getTopicDescription(topicOptions));
        try {
            return entityClient.putWithResponseAsync(topicName, createEntity, null, getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(this::deserializeTopic);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
        if (CoreUtils.isNullOrEmpty(queueName)) {
            return monoError(LOGGER, new IllegalArgumentException("'queueName' cannot be null or empty."));
        }
        try {
            return entityClient.deleteWithResponseAsync(queueName, getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
        if (CoreUtils.isNullOrEmpty(topicName)) {
            return monoError(LOGGER, new IllegalArgumentException("'topicName' cannot be null or empty."));
        }

        if (CoreUtils.isNullOrEmpty(subscriptionName)) {
            return monoError(LOGGER, new IllegalArgumentException("'subscriptionName' cannot be null or empty."));
        }

        if (CoreUtils.isNullOrEmpty(ruleName)) {
            return monoError(LOGGER, new IllegalArgumentException("'ruleName' cannot be null or empty."));
        }
        try {

            return rulesClient.deleteWithResponseAsync(topicName, subscriptionName, ruleName, getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
        if (CoreUtils.isNullOrEmpty(topicName)) {
            return monoError(LOGGER, new IllegalArgumentException("'topicName' cannot be null or empty."));
        }

        if (CoreUtils.isNullOrEmpty(subscriptionName)) {
            return monoError(LOGGER, new IllegalArgumentException("'subscriptionName' cannot be null or empty."));
        }

        try {

            return managementClient.getSubscriptions().deleteWithResponseAsync(topicName, subscriptionName,
                getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
        if (CoreUtils.isNullOrEmpty(topicName)) {
            return monoError(LOGGER, new IllegalArgumentException("'topicName' cannot be null or empty."));
        }
        try {
            return entityClient.deleteWithResponseAsync(topicName, getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
        if (CoreUtils.isNullOrEmpty(queueName)) {
            return monoError(LOGGER, new IllegalArgumentException("'queueName' cannot be null or empty."));
        }
        try {
            return entityClient.getWithResponseAsync(queueName, true, getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .handle((response, sink) -> {
                    final Response<QueueProperties> deserialize = deserializeQueue(response);

                    // if this is null, then the queue could not be found.
                    if (deserialize.getValue() == null) {
                        final HttpResponse notFoundResponse = new EntityHelper.EntityNotFoundHttpResponse<>(deserialize);
                        sink.error(new ResourceNotFoundException(String.format("Queue '%s' does not exist.", queueName),
                            notFoundResponse));
                    } else {
                        final T mapped = mapper.apply(deserialize.getValue());
                        sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), mapped));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<RuleProperties>> getRuleWithResponse(String topicName, String subscriptionName,
        String ruleName, Context context) {

        try {
            return rulesClient.getWithResponseAsync(topicName, subscriptionName, ruleName, true, getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(this::deserializeRule);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
        if (CoreUtils.isNullOrEmpty(topicName)) {
            return monoError(LOGGER, new IllegalArgumentException("'topicName' cannot be null or empty."));
        }

        if (CoreUtils.isNullOrEmpty(subscriptionName)) {
            return monoError(LOGGER, new IllegalArgumentException("'subscriptionName' cannot be null or empty."));
        }
        try {

            return managementClient.getSubscriptions().getWithResponseAsync(topicName, subscriptionName, true,
                    getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .handle((response, sink) -> {
                    final Response<SubscriptionProperties> deserialize = deserializeSubscription(topicName, response);

                    // if this is null, then the queue could not be found.
                    if (deserialize.getValue() == null) {
                        final HttpResponse notFoundResponse = new EntityHelper.EntityNotFoundHttpResponse<>(deserialize);
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
            return monoError(LOGGER, ex);
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
        if (CoreUtils.isNullOrEmpty(topicName)) {
            return monoError(LOGGER, new IllegalArgumentException("'topicName' cannot be null or empty."));
        }
        try {

            return entityClient.getWithResponseAsync(topicName, true, getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .handle((response, sink) -> {
                    final Response<TopicProperties> deserialize = deserializeTopic(response);

                    // if this is null, then the queue could not be found.
                    if (deserialize.getValue() == null) {
                        final HttpResponse notFoundResponse = new EntityHelper.EntityNotFoundHttpResponse<>(deserialize);
                        sink.error(new ResourceNotFoundException(String.format("Topic '%s' does not exist.", topicName),
                            notFoundResponse));
                    } else {
                        final T mapped = mapper.apply(deserialize.getValue());
                        sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), mapped));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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

        try {
            return listQueues(0, getContext(context));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
            final int skip = Integer.parseInt(continuationToken);

            return listQueues(skip, getContext(context));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
        try {
            return listRules(topicName, subscriptionName, 0, getContext(context));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
            final int skip = Integer.parseInt(continuationToken);

            return listRules(topicName, subscriptionName, skip, getContext(context));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
        try {
            return listSubscriptions(topicName, 0, getContext(context));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
            final int skip = Integer.parseInt(continuationToken);

            return listSubscriptions(topicName, skip, getContext(context));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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

        try {
            return listTopics(0, getContext(context));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
            final int skip = Integer.parseInt(continuationToken);

            return listTopics(skip, getContext(context));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
            return monoError(LOGGER, new NullPointerException("'queue' cannot be null"));
        }
        context = context == null ? Context.NONE : context;

        final Context contextWithHeaders
            = getContext(context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders()));
        final String forwardTo = getForwardToEntity(queue.getForwardTo(), contextWithHeaders);
        if (forwardTo != null) {
            queue.setForwardTo(forwardTo);
        }
        final String forwardDlq
            = getForwardDlqEntity(queue.getForwardDeadLetteredMessagesTo(), contextWithHeaders);
        if (forwardDlq != null) {
            queue.setForwardDeadLetteredMessagesTo(forwardDlq);
        }

        final CreateQueueBody createEntity =
            getCreateQueueBody(EntityHelper.toImplementation(queue));

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return entityClient.putWithResponseAsync(queue.getName(), createEntity, "*", contextWithHeaders)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(this::deserializeQueue);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
            return monoError(LOGGER, new NullPointerException("'rule' cannot be null"));
        }

        final CreateRuleBody ruleBody = getUpdateRuleBody(rule);
        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return managementClient.getRules().putWithResponseAsync(topicName, subscriptionName, rule.getName(),
                ruleBody, "*", getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(this::deserializeRule);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
            return monoError(LOGGER, new NullPointerException("'subscription' cannot be null"));
        }
        context = context == null ? Context.NONE : context;
        final Context contextWithHeaders = context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders());
        final String forwardTo = getForwardToEntity(subscription.getForwardTo(), contextWithHeaders);
        if (forwardTo != null) {
            subscription.setForwardTo(forwardTo);
        }
        final String forwardDlq
            = getForwardDlqEntity(subscription.getForwardDeadLetteredMessagesTo(), contextWithHeaders);
        if (forwardDlq != null) {
            subscription.setForwardDeadLetteredMessagesTo(forwardDlq);
        }

        final String topicName = subscription.getTopicName();
        final String subscriptionName = subscription.getSubscriptionName();
        final CreateSubscriptionBody createEntity =
            getCreateSubscriptionBody(EntityHelper.toImplementation(subscription));

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return managementClient.getSubscriptions().putWithResponseAsync(topicName, subscriptionName, createEntity,
                "*", contextWithHeaders)
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(response -> deserializeSubscription(topicName, response));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
            return monoError(LOGGER, new NullPointerException("'topic' cannot be null"));
        }

        final CreateTopicBody createEntity = getUpdateTopicBody(topic);

        try {
            // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
            return entityClient.putWithResponseAsync(topic.getName(), createEntity, "*",
                    getContext(context))
                .onErrorMap(ServiceBusAdministrationAsyncClient::mapException)
                .map(this::deserializeTopic);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
            throw LOGGER.logExceptionAsError(new RuntimeException(String.format(
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
            LOGGER.info("entry.getContent() is null. The entity may not exist. {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent().getQueueDescription() == null) {
            final TopicDescriptionEntry entryTopic = deserialize(response.getValue(), TopicDescriptionEntry.class);
            if (entryTopic != null && entryTopic.getContent() != null && entryTopic.getContent().getTopicDescription() != null) {
                LOGGER.warning("'{}' is not a queue, it is a topic.", entryTopic.getTitle());
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
            }
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

        return getRulePropertiesSimpleResponse(response, entry);
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
        return getSubscriptionPropertiesSimpleResponse(topicName, response, entry);
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
            LOGGER.warning("entry.getContent() is null. There should have been content returned. Entry: {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent().getTopicDescription() == null) {
            final QueueDescriptionEntry entryQueue = deserialize(response.getValue(), QueueDescriptionEntry.class);
            if (entryQueue != null && entryQueue.getContent() != null && entryQueue.getContent().getQueueDescription() != null) {
                LOGGER.warning("'{}' is not a topic, it is a queue.", entryQueue.getTitle());
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
            }
        }

        final TopicProperties result = EntityHelper.toModel(entry.getContent().getTopicDescription());
        final String topicName = getTitleValue(entry.getTitle());
        EntityHelper.setTopicName(result, topicName);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), result);
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
                    LOGGER.warning("Could not deserialize QueueDescriptionFeed. skip {}, top: {}", skip,
                        NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<QueueProperties> entities = getQueues(feed);

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
                    LOGGER.warning("Could not deserialize RuleDescriptionFeed. skip {}, top: {}", skip,
                        NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<RuleProperties> entities = getRules(feed);

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
                    LOGGER.warning("Could not deserialize SubscriptionDescriptionFeed. skip {}, top: {}", skip,
                        NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<SubscriptionProperties> entities = getSubscriptions(topicName, feed);

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
                    LOGGER.warning("Could not deserialize TopicDescriptionFeed. skip {}, top: {}", skip,
                        NUMBER_OF_ELEMENTS);
                    return Mono.empty();
                }

                final List<TopicProperties> entities = getTopics(feed);
                try {
                    return Mono.just(extractPage(feedResponse, entities, feed.getLink()));
                } catch (MalformedURLException | UnsupportedEncodingException error) {
                    return Mono.error(new RuntimeException("Could not parse response into FeedPage<TopicDescription>",
                        error));
                }
            });
    }


    /**
     * Checks if the given entity is an absolute URL, if so return it.
     * Otherwise, construct the URL from the given entity and return that.
     *
     * @param entity : entity to forward messages to.
     *
     * @return Forward to Entity represented as an absolute URL
     */
    private String getAbsoluteUrlFromEntity(String entity) {
        // Check if passed entity is an absolute URL
        try {
            URL url = new URL(entity);
            return url.toString();
        } catch (MalformedURLException ex) {
            // Entity is not a URL, continue.
        }
        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.setScheme("https");
        urlBuilder.setHost(managementClient.getEndpoint());
        urlBuilder.setPath(entity);

        try {
            URL url = urlBuilder.toUrl();
            return url.toString();
        } catch (MalformedURLException ex) {
            // This is not expected.
            LOGGER.error("Failed to construct URL using the endpoint:'{}' and entity:'{}'",
                managementClient.getEndpoint(), entity);
            LOGGER.logThrowableAsError(ex);
        }
        return null;
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
        final HttpResponse errorHttpResponse = managementError.getResponse();

        final int statusCode = error != null && error.getCode() != null
            ? error.getCode()
            : errorHttpResponse.getStatusCode();
        final String errorDetail = error != null && error.getDetail() != null
            ? error.getDetail()
            : managementError.getMessage();

        switch (statusCode) {
            case 401:
                return new ClientAuthenticationException(errorDetail, managementError.getResponse(), exception);
            case 404:
                return new ResourceNotFoundException(errorDetail, managementError.getResponse(), exception);
            case 409:
                return new ResourceExistsException(errorDetail, managementError.getResponse(), exception);
            case 412:
                return new ResourceModifiedException(errorDetail, managementError.getResponse(), exception);
            default:
                return new HttpResponseException(errorDetail, managementError.getResponse(), exception);
        }
    }

    private String getForwardDlqEntity(String forwardDlqToEntity, Context contextWithHeaders) {
        if (!CoreUtils.isNullOrEmpty(forwardDlqToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardDlqToEntity, contextWithHeaders);
            return getAbsoluteUrlFromEntity(forwardDlqToEntity);
        }
        return null;
    }

    private String getForwardToEntity(String forwardToEntity, Context contextWithHeaders) {
        if (!CoreUtils.isNullOrEmpty(forwardToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardToEntity, contextWithHeaders);
            return getAbsoluteUrlFromEntity(forwardToEntity);
        }
        return null;
    }
}
