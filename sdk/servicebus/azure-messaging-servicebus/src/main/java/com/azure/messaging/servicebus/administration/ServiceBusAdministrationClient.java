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
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
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
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescription;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementErrorException;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static com.azure.core.http.policy.AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.NUMBER_OF_ELEMENTS;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.QUEUES_ENTITY_TYPE;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.TOPICS_ENTITY_TYPE;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.addSupplementaryAuthHeader;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.extractPage;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getCreateQueueBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getCreateRuleBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getCreateSubscriptionBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getCreateTopicBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getQueueProperties;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getQueues;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getRulePropertiesSimpleResponse;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getRules;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getSubscriptionPropertiesSimpleResponse;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getSubscriptions;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getTopicProperties;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getTopics;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getContext;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getUpdateRuleBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getUpdateSubscriptionBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.getUpdateTopicBody;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.validateQueueName;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.validateRuleName;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.validateSubscriptionName;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.validateTopicName;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;

/**
 * A <b>synchronous</b> client for managing a Service Bus namespace.
 *
 * <p><strong>Create a queue</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string -->
 * <pre>
 * QueueProperties queue = client.createQueue&#40;&quot;my-new-queue&quot;&#41;;
 * System.out.printf&#40;&quot;Queue created. Name: %s. Lock Duration: %s.%n&quot;,
 *     queue.getName&#40;&#41;, queue.getLockDuration&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string -->
 *
 * <p><strong>Edit an existing subscription</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatesubscription#subscriptionproperties -->
 * <pre>
 * &#47;&#47; To update the subscription we have to:
 * &#47;&#47; 1. Get the subscription info from the service.
 * &#47;&#47; 2. Update the SubscriptionProperties we want to change.
 * &#47;&#47; 3. Call the updateSubscription&#40;&#41; with the updated object.
 * SubscriptionProperties subscription = client.getSubscription&#40;&quot;my-topic&quot;, &quot;my-subscription&quot;&#41;;
 *
 * System.out.println&#40;&quot;Original delivery count: &quot; + subscription.getMaxDeliveryCount&#40;&#41;&#41;;
 *
 * &#47;&#47; Updating it to a new value.
 * subscription.setMaxDeliveryCount&#40;5&#41;;
 *
 * &#47;&#47; Persisting the updates to the subscription object.
 * SubscriptionProperties updated = client.updateSubscription&#40;subscription&#41;;
 *
 * System.out.printf&#40;&quot;Subscription updated. Name: %s. Delivery count: %s.%n&quot;,
 *     updated.getSubscriptionName&#40;&#41;, updated.getMaxDeliveryCount&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatesubscription#subscriptionproperties -->
 *
 * <p><strong>List all queues</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.listQueues -->
 * <pre>
 * client.listQueues&#40;&#41;.forEach&#40;queue -&gt; &#123;
 *     System.out.printf&#40;&quot;Queue [%s]. Lock Duration: %s.%n&quot;,
 *         queue.getName&#40;&#41;, queue.getLockDuration&#40;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.listQueues -->
 *
 * @see ServiceBusAdministrationClientBuilder
 * @see ServiceBusAdministrationAsyncClient ServiceBusAdministrationAsyncClient for an asynchronous client.
 */
@ServiceClient(builder = ServiceBusAdministrationClientBuilder.class)
public final class ServiceBusAdministrationClient {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusAdministrationClient.class);
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    private final ServiceBusManagementClientImpl managementClient;
    private final EntitiesImpl entityClient;
    private final ServiceBusManagementSerializer serializer;
    private final RulesImpl rulesClient;

    /**
     * Creates a new instance with the given client.
     *
     * @param managementClient Client to make management calls.
     * @param serializer Serializer to deserialize ATOM XML responses.
     * @throws NullPointerException if any one of {@code managementClient, serializer, credential} is null.
     */
    ServiceBusAdministrationClient(ServiceBusManagementClientImpl managementClient,
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
     * @return The created queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} null or an empty string.
     * @throws ResourceExistsException if a queue exists with the same {@code queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueProperties createQueue(String queueName) {
        return createQueue(queueName, new CreateQueueOptions());
    }

    /**
     * Creates a queue with the {@link CreateQueueOptions}.
     *
     * @param queueName Name of the queue to create.
     * @param queueOptions Information about the queue to create.
     * @return The created queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} null or an empty string.
     * @throws ResourceExistsException if a queue exists with the same {@link QueueProperties#getName() queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueProperties createQueue(String queueName, CreateQueueOptions queueOptions) {
        return createQueueWithResponse(queueName, queueOptions, null).getValue();
    }

    /**
     * Creates a queue and returns the created queue in addition to the HTTP response.
     *
     * @param queueName Name of the queue to create.
     * @param queueOptions Information about the queue to create.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The created queue in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the queue quota is exceeded, or an error
     *     occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} null or an empty string.
     * @throws ResourceExistsException if a queue exists with the same {@link QueueProperties#getName() queueName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueProperties> createQueueWithResponse(String queueName, CreateQueueOptions queueOptions,
                                                             Context context) {
        validateQueueName(queueName);
        if (queueOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'queueOptions' cannot be null."));
        }
        context = context == null ? Context.NONE : context;

        final Context contextWithHeaders
            = enableSyncContext(context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders()));
        final String forwardTo = getForwardToEntity(queueOptions.getForwardTo(), contextWithHeaders);
        if (forwardTo != null) {
            queueOptions.setForwardTo(forwardTo);
        }
        final String forwardDlq
            = getForwardDlqEntity(queueOptions.getForwardDeadLetteredMessagesTo(), contextWithHeaders);
        if (forwardDlq != null) {
            queueOptions.setForwardDeadLetteredMessagesTo(forwardDlq);
        }
        final CreateQueueBody createEntity =
            getCreateQueueBody(EntityHelper.getQueueDescription(queueOptions));
        return deserializeQueue(entityClient.putSyncWithResponse(queueName, createEntity, null,
            contextWithHeaders));
    }

    /**
     * Creates a rule under the given topic and subscription
     *
     * @param topicName Name of the topic associated with rule.
     * @param subscriptionName Name of the subscription associated with the rule.
     * @param ruleName Name of the rule.
     * @return Information about the created rule.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     * processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are null or empty strings.
     * @throws ResourceExistsException if a rule exists with the same topic, subscription, and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RuleProperties createRule(String topicName, String subscriptionName, String ruleName) {
        return createRule(topicName, ruleName, subscriptionName, new CreateRuleOptions());
    }

    /**
     * Creates a rule with the {@link CreateRuleOptions}.
     *
     * @param topicName Name of the topic associated with rule.
     * @param subscriptionName Name of the subscription associated with the rule.
     * @param ruleName Name of the rule.
     * @param ruleOptions Information about the rule to create.
     * @return Information about the created rule.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     * processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are null or empty strings.
     * @throws NullPointerException {@code ruleOptions} are null.
     * @throws ResourceExistsException if a rule exists with the same topic and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RuleProperties createRule(String topicName, String ruleName, String subscriptionName,
                                     CreateRuleOptions ruleOptions) {
        return createRuleWithResponse(topicName, subscriptionName, ruleName, ruleOptions, null).getValue();
    }

    /**
     * Creates a rule and returns the created rule in addition to the HTTP response.
     *
     * @param topicName Name of the topic associated with rule.
     * @param subscriptionName Name of the subscription associated with the rule.
     * @param ruleName Name of the rule.
     * @param ruleOptions Information about the rule to create.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The created rule in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     * processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code ruleName} are null or empty strings.
     * @throws NullPointerException {@code ruleOptions} are null.
     * @throws ResourceExistsException if a rule exists with the same topic and rule name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RuleProperties> createRuleWithResponse(String topicName, String subscriptionName,
                                                           String ruleName, CreateRuleOptions ruleOptions,
                                                           Context context) {
        validateTopicName(topicName);
        validateSubscriptionName(subscriptionName);
        validateRuleName(ruleName);
        if (ruleOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'ruleOptions' cannot be null."));
        }
        final CreateRuleBody createEntity = getCreateRuleBody(ruleName, ruleOptions);
        return deserializeRule(
            managementClient.getRules().putSyncWithResponse(topicName, subscriptionName, ruleName, createEntity,
                null, enableSyncContext(context)));
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
     * processing the request.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionProperties createSubscription(String topicName, String subscriptionName) {
        return createSubscription(topicName, subscriptionName, new CreateSubscriptionOptions());
    }

    /**
     * Creates a subscription with the {@link CreateSubscriptionOptions}.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     *
     * @param subscriptionOptions Information about the subscription to create.
     * @return Information about the created subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     * processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionProperties createSubscription(String topicName, String subscriptionName,
        CreateSubscriptionOptions subscriptionOptions) {
        return createSubscriptionWithResponse(topicName, subscriptionName, subscriptionOptions, null).getValue();
    }

    /**
     * Creates a subscription with default rule using the {@link CreateSubscriptionOptions} and
     * {@link CreateRuleOptions}.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param ruleName Name of the default rule the subscription should be created with.
     * @param subscriptionOptions A {@link CreateSubscriptionOptions} object describing the subscription to create.
     * @param ruleOptions A {@link CreateRuleOptions} object describing the default rule.
     *                    If null, then pass-through filter will be created.
     *
     * @return Information about the created subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     * processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionProperties createSubscription(String topicName, String subscriptionName, String ruleName,
                                                     CreateSubscriptionOptions subscriptionOptions,
                                                     CreateRuleOptions ruleOptions) {
        return createSubscriptionWithResponse(topicName, subscriptionName, ruleName, subscriptionOptions,
            ruleOptions, null).getValue();
    }

    /**
     * Creates a subscription and returns the created subscription in addition to the HTTP response.
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
     * processing the request.
     * @throws NullPointerException if {@code subscriptionOptions} is null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SubscriptionProperties> createSubscriptionWithResponse(String topicName, String subscriptionName,
        CreateSubscriptionOptions subscriptionOptions, Context context) {
        validateTopicName(topicName);
        validateSubscriptionName(subscriptionName);
        if (subscriptionOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'subscriptionOptions' cannot be null."));
        }
        context = context == null ? Context.NONE : context;
        final Context contextWithHeaders
            = enableSyncContext(getContext(context).addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders()));
        final String forwardTo = getForwardToEntity(subscriptionOptions.getForwardTo(), contextWithHeaders);
        if (forwardTo != null) {
            subscriptionOptions.setForwardTo(forwardTo);
        }
        final String forwardDlq
            = getForwardDlqEntity(subscriptionOptions.getForwardDeadLetteredMessagesTo(), contextWithHeaders);
        if (forwardDlq != null) {
            subscriptionOptions.setForwardDeadLetteredMessagesTo(forwardDlq);
        }

        final CreateSubscriptionBody createEntity =
            getCreateSubscriptionBody(EntityHelper.getSubscriptionDescription(subscriptionOptions));
        return deserializeSubscription(topicName,
            managementClient.getSubscriptions().putSyncWithResponse(topicName, subscriptionName, createEntity,
                null, contextWithHeaders));

    }

    /**
     * Creates a subscription with default rule configured and returns the created subscription
     * in addition to the HTTP response.
     *
     * @param topicName Name of the topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param ruleName Name of the default rule the subscription should be created with.
     * @param subscriptionOptions A {@link CreateSubscriptionOptions} object describing the subscription to create.
     * @param ruleOptions A {@link CreateRuleOptions} object describing the default rule.
     *                    If null, then pass-through filter will be created.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The created subscription in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the quota is exceeded, or an error occurred
     * processing the request.
     * @throws NullPointerException if {@code subscriptionOptions} is null.
     * @throws ResourceExistsException if a subscription exists with the same topic and subscription name.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SubscriptionProperties> createSubscriptionWithResponse(String topicName, String subscriptionName,
                                                                           String ruleName,
                                                                           CreateSubscriptionOptions subscriptionOptions,
                                                                           CreateRuleOptions ruleOptions,
                                                                           Context context) {
        if (ruleOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'CreateRuleOptions' cannot be null."));
        }
        Objects.requireNonNull(ruleOptions.getFilter(), "'RuleFilter' cannot be null.");
        final RuleDescription rule = new RuleDescription()
            .setAction(ruleOptions.getAction() != null ? EntityHelper.toImplementation(ruleOptions.getAction()) : null)
            .setFilter(EntityHelper.toImplementation(ruleOptions.getFilter()))
            .setName(ruleName);
        subscriptionOptions.setDefaultRule(EntityHelper.toModel(rule));
        return createSubscriptionWithResponse(topicName, subscriptionName, subscriptionOptions, context);

    }

    /**
     * Creates a topic with the given name.
     *
     * @param topicName Name of the topic to create.
     * @return Information about the created topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topic quota is exceeded, or an error
     *     occurred processing the request.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws ResourceExistsException if a topic exists with the same {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TopicProperties createTopic(String topicName) {
        return createTopic(topicName, new CreateTopicOptions());
    }

    /**
     * Creates a topic with the {@link CreateTopicOptions}.
     *
     * @param topicName Name of the topic to create.
     * @param topicOptions Information about the topic to create.
     * @return Information about the created topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the topicOptions quota is exceeded, or an
     * error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws NullPointerException if {@code topicOptions} is null.
     * @throws ResourceExistsException if a topic exists with the same {@code topicName}.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TopicProperties createTopic(String topicName, CreateTopicOptions topicOptions) {
        return createTopicWithResponse(topicName, topicOptions, null).getValue();
    }

    /**
     * Creates a topic and returns the created topic in addition to the HTTP response.
     *
     * @param topicName Name of the topic to create.
     * @param topicOptions Information about the topic to create.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The created topic in addition to the HTTP response.
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
    public Response<TopicProperties> createTopicWithResponse(String topicName, CreateTopicOptions topicOptions,
                                                             Context context) {
        validateTopicName(topicName);
        if (topicOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'topicOptions' cannot be null."));
        }

        final CreateTopicBody createEntity = getCreateTopicBody(EntityHelper.getTopicDescription(topicOptions));
        return deserializeTopic(entityClient.putSyncWithResponse(topicName, createEntity, null,
            enableSyncContext(context)));
    }

    /**
     * Deletes a queue the matching {@code queueName}.
     *
     * @param queueName Name of queue to delete.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or is an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-queue">Delete Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteQueue(String queueName) {
        deleteQueueWithResponse(queueName, null);
    }

    /**
     * Deletes a queue the matching {@code queueName} and returns the HTTP response.
     *
     * @param queueName Name of queue to delete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response when the queue is successfully deleted.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or is an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-queue">Delete Queue</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String queueName, Context context) {
        validateQueueName(queueName);
        Response<Object> response = entityClient.deleteSyncWithResponse(queueName, enableSyncContext(context));
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), null);
    }

    /**
     * Deletes a rule the matching {@code ruleName}.
     *
     * @param topicName Name of topic associated with rule to delete.
     * @param subscriptionName Name of the subscription associated with the rule to delete.
     * @param ruleName Name of rule to delete.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName}, {@code subscriptionName}, or {@code ruleName} is null or
     *      an empty string.
     * @throws ResourceNotFoundException if the {@code ruleName} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRule(String topicName, String subscriptionName, String ruleName) {
        deleteRuleWithResponse(topicName, subscriptionName, ruleName, null).getValue();
    }

    /**
     * Deletes a rule the matching {@code ruleName} and returns the HTTP response.
     *
     * @param topicName Name of topic associated with rule to delete.
     * @param subscriptionName Name of the subscription associated with the rule to delete.
     * @param ruleName Name of rule to delete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     *  @throws IllegalArgumentException if {@code topicName}, {@code subscriptionName}, or {@code ruleName} is null or
     *      an empty string.
     * @throws ResourceNotFoundException if the {@code ruleName} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRuleWithResponse(String topicName, String subscriptionName,
                                                 String ruleName, Context context) {
        validateTopicName(topicName);
        validateSubscriptionName(subscriptionName);
        validateRuleName(ruleName);

        final Response<Object> response =
            rulesClient.deleteSyncWithResponse(topicName, subscriptionName, ruleName, enableSyncContext(context));
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), null);
    }

    /**
     * Deletes a subscription matching the {@code subscriptionName} in topic {@code topicName}.
     *
     * @param topicName Name of topic associated with subscription to delete.
     * @param subscriptionName Name of subscription to delete.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-subscription">Delete Subscription</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSubscription(String topicName, String subscriptionName) {
        deleteSubscriptionWithResponse(topicName, subscriptionName, null).getValue();
    }

    /**
     * Deletes a subscription the matching {@code subscriptionName} and returns the HTTP response.
     *
     * @param topicName Name of topic associated with subscription to delete.
     * @param subscriptionName Name of subscription to delete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-subscription">Delete Subscription</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteSubscriptionWithResponse(String topicName, String subscriptionName, Context context) {
        validateSubscriptionName(subscriptionName);
        validateTopicName(topicName);
        final Response<Object> response =
            managementClient.getSubscriptions().deleteSyncWithResponse(topicName, subscriptionName,
                enableSyncContext(context));
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), null);
    }

    /**
     * Deletes a topic the matching {@code topicName}.
     *
     * @param topicName Name of topic to delete.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-topic">Delete Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTopic(String topicName) {
        deleteTopicWithResponse(topicName, null).getValue();
    }

    /**
     * Deletes a topic the matching {@code topicName} and returns the HTTP response.
     *
     * @param topicName Name of topic to delete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/delete-topic">Delete Topic</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTopicWithResponse(String topicName, Context context) {
        validateTopicName(topicName);
        final Response<Object> response = entityClient.deleteSyncWithResponse(topicName, enableSyncContext(context));
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), null);
    }

    /**
     * Gets information about the queue.
     *
     * @param queueName Name of queue to get information about.
     * @return Information about the queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueProperties getQueue(String queueName) {
        return getQueueWithResponse(queueName, null).getValue();
    }

    /**
     * Gets information about the queue along with its HTTP response.
     *
     * @param queueName Name of queue to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return Information about the queue and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueProperties> getQueueWithResponse(String queueName, Context context) {
        final Response<QueueProperties> response = getQueueInternal(queueName, context);
        if (response.getValue() == null) {
            final HttpResponse
                notFoundResponse = new EntityHelper.EntityNotFoundHttpResponse<>(response);
            throw LOGGER.logExceptionAsError(
                new ResourceNotFoundException(String.format("Queue '%s' does not exist.", queueName),
                    notFoundResponse));
        }
        return response;
    }

    private Response<QueueProperties> getQueueInternal(String queueName, Context context) {
        validateQueueName(queueName);
        final Response<Object> response = entityClient.getSyncWithResponse(queueName, true,
            enableSyncContext(context));
        return deserializeQueue(response);
    }

    /**
     * Gets whether or not a queue with {@code queueName} exists in the Service Bus namespace.
     *
     * @param queueName Name of the queue.
     * @return {@code true} if the queue exists; otherwise {@code false}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean getQueueExists(String queueName) {
        final Boolean exists = getQueueExistsWithResponse(queueName, null).getValue();
        return exists != null && exists;
    }

    /**
     * Gets whether or not a queue with {@code queueName} exists in the Service Bus namespace.
     *
     * @param queueName Name of the queue.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response and {@code true} if the queue exists; otherwise {@code false}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> getQueueExistsWithResponse(String queueName, Context context) {
        final Response<QueueProperties> queueWithResponse = getQueueInternal(queueName, context);
        return getEntityExistsWithResponse(queueWithResponse);
    }

    /**
     * Gets runtime properties about the queue.
     *
     * @param queueName Name of queue to get information about.
     * @return Runtime properties about the queue.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueRuntimeProperties getQueueRuntimeProperties(String queueName) {
        return getQueueRuntimePropertiesWithResponse(queueName, null).getValue();
    }

    /**
     * Gets runtime properties about the queue along with its HTTP response.
     *
     * @param queueName Name of queue to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return Runtime properties about the queue and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code queueName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code queueName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueRuntimeProperties> getQueueRuntimePropertiesWithResponse(String queueName, Context context) {
        final Response<QueueProperties> response = getQueueWithResponse(queueName, context);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), new QueueRuntimeProperties(response.getValue()));
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
        return getNamespacePropertiesWithResponse(null).getValue();
    }

    /**
     * Gets information about the Service Bus namespace along with its HTTP response.
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return Information about the namespace and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NamespaceProperties> getNamespacePropertiesWithResponse(Context context) {
        final Response<NamespacePropertiesEntry> response
            = managementClient.getNamespaces().getSyncWithResponse(enableSyncContext(context));
        final NamespacePropertiesEntry entry = response.getValue();
        if (entry == null || entry.getContent() == null) {
            throw LOGGER.logExceptionAsError(new AzureException(
                "There was no content inside namespace response. Entry: " + response));
        }

        final NamespaceProperties namespaceProperties = entry.getContent().getNamespaceProperties();
        return new SimpleResponse<>(response.getRequest(),
            response.getStatusCode(), response.getHeaders(), namespaceProperties);
    }

    /**
     * Gets a rule from the service namespace.
     * Only following data types are deserialized in Filters and Action parameters - string, int, long, boolean, double,
     * and OffsetDateTime. Other data types would return its string value.
     *
     * @param topicName The name of the topic relative to service bus namespace.
     * @param subscriptionName The subscription name the rule belongs to.
     * @param ruleName The name of the rule to retrieve.
     * @return The associated rule.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RuleProperties getRule(String topicName, String subscriptionName, String ruleName) {
        return getRuleWithResponse(topicName, subscriptionName, ruleName, null).getValue();
    }

    /**
     * Gets a rule from the service namespace.
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
        final Response<Object> objectResponse =
            rulesClient.getSyncWithResponse(topicName, subscriptionName, ruleName, true,
                enableSyncContext(context));
        return deserializeRule(objectResponse);
    }


    /**
     * Gets information about the queue.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
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
        return getSubscriptionWithResponse(topicName, subscriptionName, null).getValue();
    }

    /**
     * Gets information about the subscription along with its HTTP response.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return Information about the subscription and the associated HTTP response.

     * @throws ServiceBusManagementErrorException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are null or empty strings.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SubscriptionProperties> getSubscriptionWithResponse(String topicName,
                                                                        String subscriptionName, Context context) {
        return getSubscriptionInternal(topicName, subscriptionName, context);
    }

    private Response<SubscriptionProperties> getSubscriptionInternal(String topicName,
                                                                     String subscriptionName, Context context) {
        validateTopicName(topicName);
        validateSubscriptionName(subscriptionName);

        final Response<Object> response = managementClient.getSubscriptions()
            .getSyncWithResponse(topicName, subscriptionName, true, enableSyncContext(context));
        return deserializeSubscription(topicName, response);
    }

    /**
     * Gets whether a subscription within a topic exists.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @return {@code true} if the subscription exists.
     *
     * @throws ServiceBusManagementErrorException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is null or an empty string.
     * @throws NullPointerException if {@code subscriptionName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean getSubscriptionExists(String topicName, String subscriptionName) {
        final Boolean exists = getSubscriptionExistsWithResponse(topicName, subscriptionName, null).getValue();
        return exists != null && exists;
    }

    /**
     * Gets whether a subscription within a topic exists.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of the subscription.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response and {@code true} if the subscription exists; otherwise {@code false}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> getSubscriptionExistsWithResponse(String topicName, String subscriptionName,
                                                               Context context) {
        final Response<SubscriptionProperties> subscriptionWithResponse =
            getSubscriptionInternal(topicName, subscriptionName, context);
        return getEntityExistsWithResponse(subscriptionWithResponse);
    }

    /**
     * Gets runtime properties about the subscription.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     * @return Runtime properties about the subscription.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code subscriptionName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code subscriptionName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionRuntimeProperties getSubscriptionRuntimeProperties(String topicName, String subscriptionName) {
        return getSubscriptionRuntimePropertiesWithResponse(topicName, subscriptionName, null).getValue();
    }

    /**
     * Gets runtime properties about the subscription.
     *
     * @param topicName Name of topic associated with subscription.
     * @param subscriptionName Name of subscription to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
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
        final Response<SubscriptionProperties> response = getSubscriptionWithResponse(topicName, subscriptionName, context);
        if (response.getValue() == null) {
            final HttpResponse notFoundResponse
                = new EntityHelper.EntityNotFoundHttpResponse<>(response);
            throw LOGGER.logExceptionAsError(new ResourceNotFoundException(String.format(
                "Subscription '%s' in topic '%s' does not exist.", topicName, subscriptionName),
                notFoundResponse));
        }
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), new SubscriptionRuntimeProperties(response.getValue()));
    }

    /**
     * Gets information about the topic.
     *
     * @param topicName Name of topic to get information about.
     * @return Information about the topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TopicProperties getTopic(String topicName) {
        return getTopicWithResponse(topicName, null).getValue();
    }

    /**
     * Gets information about the topic along with its HTTP response.
     *
     * @param topicName Name of topic to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return Information about the topic and the associated HTTP response.
     *
     * @throws ServiceBusManagementErrorException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TopicProperties> getTopicWithResponse(String topicName, Context context) {
        final Response<TopicProperties> response = getTopicInternal(topicName, context);
        if (response.getValue() == null) {
            final HttpResponse notFoundResponse =
                new EntityHelper.EntityNotFoundHttpResponse<>(response);
            throw LOGGER.logExceptionAsError(
                new ResourceNotFoundException(String.format("Topic '%s' does not exist.", topicName),
                    notFoundResponse));
        }
        return response;
    }

    private Response<TopicProperties> getTopicInternal(String topicName, Context context) {
        validateTopicName(topicName);
        final Response<Object> response = entityClient.getSyncWithResponse(topicName,
            true, enableSyncContext(context));
        return deserializeTopic(response);
    }

    /**
     * Gets whether a topic with {@code topicName} exists in the Service Bus namespace.
     *
     * @param topicName Name of the topic.
     * @return {@code true} if the topic exists.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean getTopicExists(String topicName) {
        final Boolean exists = getTopicExistsWithResponse(topicName, null).getValue();
        return exists != null && exists;
    }

    /**
     * Gets whether a topic with {@code topicName} exists in the Service Bus namespace.
     *
     * @param topicName Name of the topic.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response and {@code true} if the topic exists; otherwise {@code false}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> getTopicExistsWithResponse(String topicName, Context context) {
        final Response<TopicProperties> topicWithResponse = getTopicInternal(topicName, context);
        return getEntityExistsWithResponse(topicWithResponse);
    }

    /**
     * Gets runtime properties about the topic.
     *
     * @param topicName Name of topic to get information about.
     * @return Runtime properties about the topic.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or  an empty string.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TopicRuntimeProperties getTopicRuntimeProperties(String topicName) {
        return getTopicRuntimePropertiesWithResponse(topicName, null).getValue();
    }

    /**
     * Gets runtime properties about the topic with its HTTP response.
     *
     * @param topicName Name of topic to get information about.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return Runtime properties about the topic and the associated HTTP response.
     *
     * @throws ServiceBusManagementErrorException If error occurred processing the request.
     * @throws IllegalArgumentException if {@code topicName} is null or  an empty string.
     * @throws ResourceNotFoundException if the {@code topicName} does not exist.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/get-entity">Get Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TopicRuntimeProperties> getTopicRuntimePropertiesWithResponse(String topicName, Context context) {
        final Response<TopicProperties> response = getTopicWithResponse(topicName, context);
        if (response.getValue() == null) {
            final HttpResponse notFoundResponse =
                new EntityHelper.EntityNotFoundHttpResponse<>(response);
            throw LOGGER.logExceptionAsError(
                new ResourceNotFoundException(String.format("Topic '%s' does not exist.", topicName),
                    notFoundResponse));
        }
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), new TopicRuntimeProperties(response.getValue()));
    }

    private <T> Response<Boolean> getEntityExistsWithResponse(Response<T> getEntityOperation) {
        // When an entity does not exist, it does not have any description object in it.
        final boolean exists = getEntityOperation.getValue() != null;
        return new SimpleResponse<>(getEntityOperation.getRequest(), getEntityOperation.getStatusCode(),
            getEntityOperation.getHeaders(), exists);
    }

    /**
     * Fetches all the queues in the Service Bus namespace.
     *
     * @return A PagedIterable of {@link QueueProperties queues} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List Queues, Subscriptions, or
     * Authorization Rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<QueueProperties> listQueues() {
        return listQueues(null);
    }

    /**
     * Fetches all the queues in the Service Bus namespace.
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A PagedIterable of {@link QueueProperties queues} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List Queues, Subscriptions, or
     * Authorization Rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<QueueProperties> listQueues(Context context) {
        return new PagedIterable<>(() -> listQueues(0, context),
            continuationToken -> {
                if (continuationToken == null || continuationToken.isEmpty()) {
                    return null;
                }
                final int skip = Integer.parseInt(continuationToken);

                return listQueues(skip, context);
            });
    }

    private PagedResponse<QueueProperties> listQueues(int skip, Context context) {
        final Response<Object> response =
            managementClient.listEntitiesSyncWithResponse(QUEUES_ENTITY_TYPE, skip, NUMBER_OF_ELEMENTS,
                enableSyncContext(context));
        final QueueDescriptionFeed feed = deserialize(response.getValue(), QueueDescriptionFeed.class);
        if (feed == null) {
            LOGGER.warning("Could not deserialize QueueDescriptionFeed. skip {}, top: {}", skip,
                NUMBER_OF_ELEMENTS);
            return null;
        }

        final List<QueueProperties> entities = getQueues(feed);
        try {
            return extractPage(response, entities, feed.getLink());
        } catch (MalformedURLException | UnsupportedEncodingException error) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "Could not parse response into FeedPage<RuleDescription>", error));
        }
    }

    /**
     * Fetches all the rules for a topic and subscription.
     *
     * @param topicName The topic name under which all the rules need to be retrieved.
     * @param subscriptionName The name of the subscription for which all rules need to be retrieved.
     * @return An iterable of {@link RuleProperties rules} for the {@code topicName} and {@code subscriptionName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} is null.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, rules, or
     * authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<RuleProperties> listRules(String topicName, String subscriptionName) {
        return listRules(topicName, subscriptionName, null);
    }

    /**
     * Fetches all the rules for a topic and subscription.
     *
     * @param topicName The topic name under which all the rules need to be retrieved.
     * @param subscriptionName The name of the subscription for which all rules need to be retrieved.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return An iterable of {@link RuleProperties rules} for the {@code topicName} and {@code subscriptionName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} is null.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, rules, or
     * authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<RuleProperties> listRules(String topicName, String subscriptionName, Context context) {
        return new PagedIterable<>(() -> listRules(topicName, subscriptionName, 0, context),
            continuationToken -> {
                if (continuationToken == null || continuationToken.isEmpty()) {
                    return null;
                }
                final int skip = Integer.parseInt(continuationToken);

                return listRules(topicName, subscriptionName, skip, context);
            });
    }

    private PagedResponse<RuleProperties> listRules(String topicName, String subscriptionName, int skip,
                                                    Context context) {
        final Response<Object> response =
            managementClient.listRulesSyncWithResponse(topicName, subscriptionName, skip, NUMBER_OF_ELEMENTS,
                enableSyncContext(context));
        final RuleDescriptionFeed feed = deserialize(response.getValue(), RuleDescriptionFeed.class);

        if (feed == null) {
            LOGGER.warning("Could not deserialize RuleDescriptionFeed. skip {}, top: {}", skip,
                NUMBER_OF_ELEMENTS);
            return null;
        }

        final List<RuleProperties> entities = getRules(feed);
        try {
            return extractPage(response, entities, feed.getLink());
        } catch (MalformedURLException | UnsupportedEncodingException error) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "Could not parse response into FeedPage<RuleDescription>", error));
        }
    }

    /**
     * Fetches all the subscriptions for a topic.
     *
     * @param topicName The topic name under which all the subscriptions need to be retrieved.
     * @return A paged iterable of {@link SubscriptionProperties subscriptions} for the {@code topicName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     * authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SubscriptionProperties> listSubscriptions(String topicName) {
        return listSubscriptions(topicName, null);
    }

    /**
     * Fetches all the subscriptions for a topic.
     *
     * @param topicName The topic name under which all the subscriptions need to be retrieved.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A paged iterable of {@link SubscriptionProperties subscriptions} for the {@code topicName}.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws NullPointerException if {@code topicName} is null.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     * authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SubscriptionProperties> listSubscriptions(String topicName, Context context) {
        return new PagedIterable<>(
            () -> listSubscriptions(topicName, 0, context),
            continuationToken -> {
                if (continuationToken == null || continuationToken.isEmpty()) {
                    return null;
                }
                final int skip = Integer.parseInt(continuationToken);
                return listSubscriptions(topicName, skip, context);
            });
    }

    private PagedResponse<SubscriptionProperties> listSubscriptions(String topicName, int skip,
                                                                    Context context) {
        final Response<Object> response =
            managementClient.listSubscriptionsSyncWithResponse(topicName, skip, NUMBER_OF_ELEMENTS,
                enableSyncContext(context));
        final SubscriptionDescriptionFeed feed = deserialize(response.getValue(), SubscriptionDescriptionFeed.class);

        if (feed == null) {
            LOGGER.warning("Could not deserialize SubscriptionDescriptionFeed. skip {}, top: {}", skip,
                NUMBER_OF_ELEMENTS);
            return null;
        }

        final List<SubscriptionProperties> entities = getSubscriptions(topicName, feed);
        try {
            return extractPage(response, entities, feed.getLink());
        } catch (MalformedURLException | UnsupportedEncodingException error) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "Could not parse response into FeedPage<SubscriptionDescription>", error));
        }
    }


    /**
     * Fetches all the topics in the Service Bus namespace.
     *
     * @return A paged iterable of {@link TopicProperties topics} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     * authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TopicProperties> listTopics() {
        return listTopics(null);
    }

    /**
     * Fetches all the topics in the Service Bus namespace.
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A paged iterable of {@link TopicProperties topics} in the Service Bus namespace.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/enumeration">List entities, subscriptions, or
     * authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TopicProperties> listTopics(Context context) {
        return new PagedIterable<>(
            () -> listTopics(0, context),
            continuationToken -> {
                if (continuationToken == null || continuationToken.isEmpty()) {
                    return null;
                }

                final int skip = Integer.parseInt(continuationToken);

                return listTopics(skip, context);
            });
    }

    private PagedResponse<TopicProperties> listTopics(int skip, Context context) {
        final Response<Object> response =
            managementClient.listEntitiesSyncWithResponse(TOPICS_ENTITY_TYPE, skip, NUMBER_OF_ELEMENTS,
                enableSyncContext(context));
        final TopicDescriptionFeed feed = deserialize(response.getValue(), TopicDescriptionFeed.class);
        if (feed == null) {
            LOGGER.warning("Could not deserialize TopicDescriptionFeed. skip {}, top: {}", skip,
                NUMBER_OF_ELEMENTS);
            return null;
        }

        final List<TopicProperties> entities = getTopics(feed);
        try {
            return extractPage(response, entities, feed.getLink());
        } catch (MalformedURLException | UnsupportedEncodingException error) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Could not parse response into FeedPage<TopicDescription>", error));
        }
    }

    /**
     * Updates a queue with the given {@link QueueProperties}. The {@link QueueProperties} must be fully populated as
     * all of the properties are replaced. If a property is not set the service default value is used.
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
        return updateQueueWithResponse(queue, null).getValue();
    }

    /**
     * Updates a queue with the given {@link QueueProperties}. The {@link QueueProperties} must be fully populated as
     * all of the properties are replaced. If a property is not set the service default value is used.
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
        if (queue == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'queue' cannot be null"));
        }
        context = context == null ? Context.NONE : context;

        final Context contextWithHeaders
            = enableSyncContext(context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders()));
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

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        final Response<Object> response =
            entityClient.putSyncWithResponse(queue.getName(), createEntity, "*", contextWithHeaders);
        return deserializeQueue(response);
    }


    /**
     * Updates a rule with the given {@link RuleProperties}. The {@link RuleProperties} must be fully populated as all
     * the properties are replaced. If a property is not set the service default value is used.
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
        return updateRuleWithResponse(topicName, subscriptionName, rule, null).getValue();
    }

    /**
     * Updates a rule with the given {@link RuleProperties}. The {@link RuleProperties} must be fully populated as all
     * the properties are replaced. If a property is not set the service default value is used.
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
        if (rule == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'rule' cannot be null"));
        }

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        final Response<Object> response =
            managementClient.getRules().putSyncWithResponse(topicName, subscriptionName, rule.getName(),
                getUpdateRuleBody(rule), "*", enableSyncContext(context));
        return deserializeRule(response);
    }

    /**
     * Updates a subscription with the given {@link SubscriptionProperties}. The {@link SubscriptionProperties} must be
     * fully populated as all of the properties are replaced. If a property is not set the service default value is
     * used.
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
     * @return Updated subscription in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the subscription quota is exceeded, or an
     * error occurred processing the request.
     * @throws IllegalArgumentException if {@link SubscriptionProperties#getTopicName()} or {@link
     *      SubscriptionProperties#getSubscriptionName()} is null or an empty string.
     * @throws NullPointerException if {@code subscription} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SubscriptionProperties updateSubscription(SubscriptionProperties subscription) {
        return updateSubscriptionWithResponse(subscription, null).getValue();
    }

    /**
     * Updates a subscription with the given {@link SubscriptionProperties}. The {@link SubscriptionProperties} must be
     * fully populated as all of the properties are replaced. If a property is not set the service default value is
     * used.
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
     * @return Updated subscription in addition to the HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If the request body was invalid, the subscription quota is exceeded, or an
     * error occurred processing the request.
     * @throws IllegalArgumentException if {@link SubscriptionProperties#getTopicName()} or {@link
     *      SubscriptionProperties#getSubscriptionName()} is null or an empty string.
     * @throws NullPointerException if {@code subscription} is null.
     * @see <a href="https://docs.microsoft.com/rest/api/servicebus/update-entity">Create or Update Entity</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SubscriptionProperties> updateSubscriptionWithResponse(
        SubscriptionProperties subscription, Context context) {

        if (subscription == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'subscription' cannot be null"));
        }
        context = context == null ? Context.NONE : context;
        final Context contextWithHeaders
            = enableSyncContext(context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders()));

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

        final CreateSubscriptionBody createEntity = getUpdateSubscriptionBody(subscription);

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        Response<Object> response =
            managementClient.getSubscriptions()
                .putSyncWithResponse(topicName, subscription.getSubscriptionName(), createEntity,
                    "*", contextWithHeaders);
        return deserializeSubscription(topicName, response);
    }

    /**
     * Updates a topic with the given {@link TopicProperties}. The {@link TopicProperties} must be fully populated as
     * all of the properties are replaced. If a property is not set the service default value is used.
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
        return updateTopicWithResponse(topic, null).getValue();
    }

    /**
     * Updates a topic with the given {@link TopicProperties}. The {@link TopicProperties} must be fully populated as
     * all of the properties are replaced. If a property is not set the service default value is used.
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
        if (topic == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'topic' cannot be null"));
        }
        final CreateTopicBody createEntity = getUpdateTopicBody(topic);

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        final Response<Object> response = entityClient.putSyncWithResponse(topic.getName(), createEntity, "*",
            enableSyncContext(context));
        return deserializeTopic(response);
    }

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
            if (entryTopic != null && entryTopic.getContent() != null
                && entryTopic.getContent().getTopicDescription() != null) {
                LOGGER.warning("'{}' is not a queue, it is a topic.", entryTopic.getTitle());
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    null);
            }
        }

        final QueueProperties result = getQueueProperties(entry);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), result);
    }

    private Response<RuleProperties> deserializeRule(Response<Object> response) {
        final RuleDescriptionEntry entry = deserialize(response.getValue(), RuleDescriptionEntry.class);

        return getRulePropertiesSimpleResponse(response, entry);
    }

    private Response<SubscriptionProperties> deserializeSubscription(String topicName, Response<Object> response) {
        final SubscriptionDescriptionEntry entry = deserialize(response.getValue(), SubscriptionDescriptionEntry.class);
        return getSubscriptionPropertiesSimpleResponse(topicName, response, entry);
    }

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
            if (entryQueue != null && entryQueue.getContent() != null
                && entryQueue.getContent().getQueueDescription() != null) {
                LOGGER.warning("'{}' is not a topic, it is a queue.", entryQueue.getTitle());
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    null);
            }
        }

        final TopicProperties result = getTopicProperties(entry);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), result);
    }

    private static Context enableSyncContext(Context context) {
        return getContext(context).addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
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
