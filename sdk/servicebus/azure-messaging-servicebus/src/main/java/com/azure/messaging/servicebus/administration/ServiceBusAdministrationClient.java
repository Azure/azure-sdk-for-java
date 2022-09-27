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
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.NamespacePropertiesEntry;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.administration.implementation.models.RuleActionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescription;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.RuleFilterImpl;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescription;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescription;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionFeed;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.http.policy.AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient.CONTENT_TYPE;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient.NUMBER_OF_ELEMENTS;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient.QUEUES_ENTITY_TYPE;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient.TOPICS_ENTITY_TYPE;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient.addSupplementaryAuthHeader;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient.getTitleValue;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;
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
    private final ServiceBusManagementClientImpl managementClient;
    private final EntitiesImpl entityClient;
    private final ServiceBusManagementSerializer serializer;
    private final RulesImpl rulesClient;

    /**
     * Creates a new instance with the given client.
     *
     * @param managementClient Client to make management calls.
     * @param serializer Serializer to deserialize ATOM XML responses.
     *
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
        return createQueue(queueName, null);
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
        return createQueueWithResponse(queueName, queueOptions, null).getValue();
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
        return createQueueWithResponseSync(queueName, queueOptions, context != null ? context : Context.NONE);
    }

    private Response<QueueProperties> createQueueWithResponseSync(String queueName, CreateQueueOptions createQueueOptions,
                                                      Context context) {
        if (queueName == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'queueName' cannot be null."));
        } else if (queueName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'queueName' cannot be empty."));
        }

        if (createQueueOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'createQueueOptions' cannot be null."));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'context' cannot be null."));
        }
        final Context contextWithHeaders = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE)
            .addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders());

        final String forwardToEntity = createQueueOptions.getForwardTo();
        if (!CoreUtils.isNullOrEmpty(forwardToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardToEntity, contextWithHeaders);
            createQueueOptions.setForwardTo(getAbsoluteUrlFromEntity(forwardToEntity));
        }

        final String forwardDlqToEntity = createQueueOptions.getForwardDeadLetteredMessagesTo();
        if (!CoreUtils.isNullOrEmpty(forwardDlqToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardDlqToEntity, contextWithHeaders);
            createQueueOptions.setForwardDeadLetteredMessagesTo(getAbsoluteUrlFromEntity(forwardDlqToEntity));
        }

        final QueueDescription description = EntityHelper.getQueueDescription(createQueueOptions);
        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType(CONTENT_TYPE)
            .setQueueDescription(description);
        final CreateQueueBody createEntity = new CreateQueueBody()
            .setContent(content);

        try {
            return deserializeQueue(entityClient.putSyncWithResponse(queueName, createEntity, null, contextWithHeaders));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
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
        return createRule(topicName, subscriptionName, ruleName, null);
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
        return createRuleWithResponseSync(topicName, subscriptionName, ruleName, ruleOptions, context);
    }

    private Response<RuleProperties> createRuleWithResponseSync(String topicName, String subscriptionName, String ruleName,
                                                    CreateRuleOptions ruleOptions, Context context) {
        if (topicName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'topicName' cannot be empty."));
        }

        if (subscriptionName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'subscriptionName' cannot be null."));
        } else if (subscriptionName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'subscriptionName' cannot be empty."));
        }

        if (ruleName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'ruleName' cannot be null."));
        } else if (ruleName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'ruleName' cannot be empty."));
        }

        if (ruleOptions == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'rule' cannot be null."));
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

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        try {
            return deserializeRule(managementClient.getRules().putSyncWithResponse(topicName, subscriptionName, ruleName, createEntity,
                    null, withTracing));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError( ex);
        }
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
        return createSubscription(topicName, subscriptionName, null);
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
        return createSubscriptionWithResponse(topicName, subscriptionName, subscriptionOptions, null).getValue();
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
        return createSubscriptionWithResponseSync(topicName, subscriptionName, subscriptionOptions,
            context != null ? context : Context.NONE);
    }

    /**
     * Creates a subscription with its context.
     *
     * @param subscriptionOptions Subscription to create.
     * @param context Context to pass into request.
     *
     * @return A Mono that completes with the created {@link SubscriptionProperties}.
     */
    private Response<SubscriptionProperties> createSubscriptionWithResponseSync(String topicName,
            String subscriptionName, CreateSubscriptionOptions subscriptionOptions, Context context) {
        if (topicName == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'topicName' cannot be empty."));
        }

        if (subscriptionName == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'subscriptionName' cannot be null."));
        } else if (subscriptionName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'subscriptionName' cannot be empty."));
        }

        if (subscriptionOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'subscription' cannot be null."));
        }

        final Context contextWithHeaders = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE)
            .addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders());
        final String forwardToEntity = subscriptionOptions.getForwardTo();
        if (!CoreUtils.isNullOrEmpty(forwardToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardToEntity, contextWithHeaders);
            subscriptionOptions.setForwardTo(getAbsoluteUrlFromEntity(forwardToEntity));
        }

        final String forwardDlqToEntity = subscriptionOptions.getForwardDeadLetteredMessagesTo();
        if (!CoreUtils.isNullOrEmpty(forwardDlqToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardDlqToEntity, contextWithHeaders);
            subscriptionOptions.setForwardDeadLetteredMessagesTo(getAbsoluteUrlFromEntity(forwardDlqToEntity));
        }

        final SubscriptionDescription subscription = EntityHelper.getSubscriptionDescription(subscriptionOptions);
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(subscription);
        final CreateSubscriptionBody createEntity = new CreateSubscriptionBody().setContent(content);

        try {
            return deserializeSubscription(topicName,
                managementClient.getSubscriptions().putSyncWithResponse(topicName, subscriptionName, createEntity,
                    null, contextWithHeaders));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
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
        return createTopic(topicName, null);
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
        return createTopicWithResponse(topicName, topicOptions, null).getValue();
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
        return createTopicWithResponseSync(topicName, topicOptions, context != null ? context : Context.NONE);
    }

    private Response<TopicProperties> createTopicWithResponseSync(String topicName, CreateTopicOptions topicOptions,
            Context context) {
        if (topicName == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'topicName' cannot be empty."));
        }

        if (topicOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'topicOptions' cannot be null"));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'context' cannot be null."));
        }

        final TopicDescription topic = EntityHelper.getTopicDescription(topicOptions);
        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(topic);
        final CreateTopicBody createEntity = new CreateTopicBody()
            .setContent(content);

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        try {
            return deserializeTopic(entityClient.putSyncWithResponse(topicName, createEntity, null, withTracing));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
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
        deleteQueueWithResponse(queueName, null);
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
        return deleteQueueWithResponseSync(queueName, context != null ? context : Context.NONE);
    }

    private Response<Void> deleteQueueWithResponseSync(String queueName, Context context) {
        if (queueName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'queueName' cannot be null"));
        } else if (queueName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'queueName' cannot be an empty string."));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        Response<Object> response = entityClient.deleteSyncWithResponse(queueName, withTracing);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null);

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
        deleteRuleWithResponse(topicName, subscriptionName, ruleName, null).getValue();
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
        return deleteRuleWithResponseSync(topicName, subscriptionName, ruleName,
            context != null ? context : Context.NONE);
    }

    private Response<Void> deleteRuleWithResponseSync(String topicName, String subscriptionName, String ruleName,
                                                Context context) {
        if (topicName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'topicName' cannot be null"));
        } else if (topicName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (subscriptionName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'subscriptionName' cannot be null"));
        } else if (subscriptionName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'subscriptionName' cannot be an empty string."));
        } else if (ruleName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'ruleName' cannot be null"));
        } else if (ruleName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'ruleName' cannot be an empty string."));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        Response<Object> response =
            rulesClient.deleteSyncWithResponse(topicName, subscriptionName, ruleName, withTracing);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), null);
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
        deleteSubscriptionWithResponse(topicName, subscriptionName, null).getValue();
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
        return deleteSubscriptionWithResponseSync(topicName, subscriptionName,
            context != null ? context : Context.NONE);
    }

    private Response<Void> deleteSubscriptionWithResponseSync(String topicName, String subscriptionName,
            Context context) {
        if (subscriptionName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'subscriptionName' cannot be null"));
        } else if (subscriptionName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'subscriptionName' cannot be an empty string."));
        } else if (topicName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'topicName' cannot be null"));
        } else if (topicName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        Response<Object> response =
            managementClient.getSubscriptions().deleteSyncWithResponse(topicName, subscriptionName,
                withTracing);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null);

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
       deleteTopicWithResponse(topicName, null).getValue();
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
        return deleteTopicWithResponseSync(topicName, context != null ? context : Context.NONE);
    }

    private Response<Void> deleteTopicWithResponseSync(String topicName, Context context) {
        if (topicName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'topicName' cannot be null"));
        } else if (topicName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        Response<Object> response = entityClient.deleteSyncWithResponse(topicName, withTracing);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), null);
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
        return getQueueWithResponse(queueName, null).getValue();
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
        return getQueueWithResponseSync(queueName, context != null ? context : Context.NONE,
            Function.identity());
    }

    private <T> Response<T> getQueueWithResponseSync(String queueName, Context context,
                                               Function<QueueProperties, T> mapper) {
        if (queueName == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'queueName' cannot be null"));
        } else if (queueName.isEmpty()) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'queueName' cannot be empty."));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        Response<Object> response = entityClient.getSyncWithResponse(queueName, true, withTracing);
        final Response<QueueProperties> deserialize = deserializeQueue(response);

        // if this is null, then the queue could not be found.
        if (deserialize.getValue() == null) {
            final HttpResponse
                notFoundResponse = new ServiceBusAdministrationAsyncClient.EntityNotFoundHttpResponse<>(deserialize);
            throw LOGGER.logExceptionAsError(new ResourceNotFoundException(String.format("Queue '%s' does not exist.", queueName),
                notFoundResponse));
        } else {
            final T mapped = mapper.apply(deserialize.getValue());
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), mapped);
        }

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
        final Boolean exists = getQueueExistsWithResponse(queueName, null).getValue();
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
        final Response<QueueProperties> queueWithResponse =
            getQueueWithResponseSync(queueName, context != null ? context : Context.NONE, Function.identity());
        return getEntityExistsWithResponse(queueWithResponse);
    }

    private <T> Response<Boolean> getEntityExistsWithResponse(Response<T> getEntityOperation) {
        // When an entity does not exist, it does not have any description object in it.
        final boolean exists = getEntityOperation.getValue() != null;
        return new SimpleResponse<>(getEntityOperation.getRequest(), getEntityOperation.getStatusCode(),
            getEntityOperation.getHeaders(), exists);
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
        return getQueueRuntimePropertiesWithResponse(queueName, null).getValue();
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
        return getQueueWithResponseSync(queueName, context != null ? context : Context.NONE,
            QueueRuntimeProperties::new);
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
     *
     * @return Information about the namespace and the associated HTTP response.
     * @throws ClientAuthenticationException if the client's credentials do not have access to modify the
     *     namespace.
     * @throws HttpResponseException If error occurred processing the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NamespaceProperties> getNamespacePropertiesWithResponse(Context context) {
        return getNamespacePropertiesWithResponseSync(context);
    }

    private Response<NamespaceProperties> getNamespacePropertiesWithResponseSync(Context context) {
        Response<NamespacePropertiesEntry> response
            = managementClient.getNamespaces().getSyncWithResponse(context);
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
        return getRuleWithResponse(topicName, subscriptionName, ruleName, null).getValue();
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
        return getRuleWithResponseSync(topicName, subscriptionName, ruleName,
            context != null ? context : Context.NONE);
    }

    private Response<RuleProperties> getRuleWithResponseSync(String topicName, String subscriptionName,
           String ruleName, Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        Response<Object> objectResponse =
            rulesClient.getSyncWithResponse(topicName, subscriptionName, ruleName, true, withTracing);
        return deserializeRule(objectResponse);
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
        return getSubscriptionWithResponse(topicName, subscriptionName, null).getValue();
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
        return getSubscriptionWithResponse(topicName, subscriptionName,
            context != null ? context : Context.NONE, Function.identity());
    }

    private <T> Response<T> getSubscriptionWithResponse(String topicName, String subscriptionName, Context context,
            Function<SubscriptionProperties, T> mapper) {
        if (topicName == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (subscriptionName == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'subscriptionName' cannot be null."));
        } else if (subscriptionName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'subscriptionName' cannot be an empty string."));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        Response<Object> response =
            managementClient.getSubscriptions().getSyncWithResponse(topicName, subscriptionName, true,
                withTracing);
        final Response<SubscriptionProperties> deserialize = deserializeSubscription(topicName, response);
            // if this is null, then the queue could not be found.
        if (deserialize.getValue() == null) {
        final HttpResponse notFoundResponse
            = new ServiceBusAdministrationAsyncClient.EntityNotFoundHttpResponse<>(deserialize);
        throw LOGGER.logExceptionAsError(new ResourceNotFoundException(String.format(
            "Subscription '%s' in topic '%s' does not exist.", topicName, subscriptionName),
            notFoundResponse));
        } else {
            final T mapped = mapper.apply(deserialize.getValue());
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), mapped);
        }
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
        final Boolean exists = getSubscriptionExistsWithResponse(topicName, subscriptionName, null).getValue();
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
        final Response<SubscriptionProperties> subscriptionWithResponse =
            getSubscriptionWithResponse(topicName, subscriptionName,
                context != null ? context : Context.NONE, Function.identity());
        return getEntityExistsWithResponse(subscriptionWithResponse);
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
        return getSubscriptionRuntimePropertiesWithResponse(topicName, subscriptionName, null).getValue();
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
        return getSubscriptionWithResponse(topicName, subscriptionName,
            context != null ? context : Context.NONE, SubscriptionRuntimeProperties::new);
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
        return getTopicWithResponse(topicName, null).getValue();
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
        return getTopicWithResponse(topicName, context != null ? context : Context.NONE,
            Function.identity());
    }

    <T> Response<T> getTopicWithResponse(String topicName, Context context,
                                         Function<TopicProperties, T> mapper) {
        if (topicName == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'topicName' cannot be null"));
        } else if (topicName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'topicName' cannot be empty."));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'context' cannot be null."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        Response<Object> response = entityClient.getSyncWithResponse(topicName, true, withTracing);
        final Response<TopicProperties> deserialize = deserializeTopic(response);

        // if this is null, then the queue could not be found.
        if (deserialize.getValue() == null) {
            final HttpResponse notFoundResponse = new ServiceBusAdministrationAsyncClient.EntityNotFoundHttpResponse<>(deserialize);
            throw LOGGER.logExceptionAsError(new ResourceNotFoundException(String.format("Topic '%s' does not exist.", topicName),
                notFoundResponse));
        } else {
            final T mapped = mapper.apply(deserialize.getValue());
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), mapped);
        }
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
        final Boolean exists = getTopicExistsWithResponse(topicName, null).getValue();
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
        final Response<TopicProperties> topicWithResponse =
            getTopicWithResponse(topicName, context != null ? context : Context.NONE, Function.identity());
        return getEntityExistsWithResponse(topicWithResponse);
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
        return getTopicRuntimePropertiesWithResponse(topicName, null).getValue();
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
        return getTopicWithResponse(topicName, context != null ? context : Context.NONE,
            TopicRuntimeProperties::new);
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
        return listQueues(null);
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
        return new PagedIterable<>(() -> listQueuesFirstPage(context),
            continuationToken -> listQueuesNextPage(continuationToken, context != null ? context : Context.NONE));

    }


    private PagedResponse<QueueProperties> listQueuesFirstPage(Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
        return listQueues(0, withTracing);
    }

    private PagedResponse<QueueProperties> listQueuesNextPage(String continuationToken, Context context) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return null;
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
        final int skip = Integer.parseInt(continuationToken);

        return listQueues(skip, withTracing);
    }

    private PagedResponse<QueueProperties> listQueues(int skip, Context context) {
        Response<Object> response =
            managementClient.listEntitiesSyncWithResponse(QUEUES_ENTITY_TYPE, skip, NUMBER_OF_ELEMENTS, context);
        final QueueDescriptionFeed feed = deserialize(response, QueueDescriptionFeed.class);
        if (feed == null) {
            LOGGER.warning("Could not deserialize QueueDescriptionFeed. skip {}, top: {}", skip,
                NUMBER_OF_ELEMENTS);
            return null;
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
        return listRules(topicName, subscriptionName, null);
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
    public PagedIterable<RuleProperties> listRules(String topicName, String subscriptionName, Context context) {
        return new PagedIterable<>(() -> listRulesFirstPage(topicName, subscriptionName,
            context != null ? context : Context.NONE),
            continuationToken -> listRulesNextPage(topicName, subscriptionName, continuationToken,
                context != null ? context : Context.NONE));
    }

    private PagedResponse<RuleProperties> listRulesFirstPage(String topicName, String subscriptionName, Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
        return listRules(topicName, subscriptionName, 0, withTracing);
    }

    private PagedResponse<RuleProperties> listRulesNextPage(String topicName, String subscriptionName,
                                                          String continuationToken, Context context) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return null;
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
        final int skip = Integer.parseInt(continuationToken);

        return listRules(topicName, subscriptionName, skip, withTracing);
    }

    private PagedResponse<RuleProperties> listRules(String topicName, String subscriptionName, int skip,
                                                    Context context) {
        Response<Object> response =
            managementClient.listRulesSyncWithResponse(topicName, subscriptionName, skip, NUMBER_OF_ELEMENTS,
                context);
        final RuleDescriptionFeed feed = deserialize(response, RuleDescriptionFeed.class);

        if (feed == null) {
            LOGGER.warning("Could not deserialize RuleDescriptionFeed. skip {}, top: {}", skip,
                NUMBER_OF_ELEMENTS);
            return null;
        }

        final List<RuleProperties> entities = feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getRuleDescription() != null)
            .map(e -> EntityHelper.toModel(e.getContent().getRuleDescription()))
            .collect(Collectors.toList());
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
        return listSubscriptions(topicName, null);
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
        return new PagedIterable<>(
            () -> listSubscriptionsFirstPage(topicName, context != null ? context : Context.NONE),
            continuationToken -> listSubscriptionsNextPage(topicName, continuationToken,
                context != null ? context : Context.NONE));

    }


    private PagedResponse<SubscriptionProperties> listSubscriptionsFirstPage(String topicName, Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
        return listSubscriptions(topicName, 0, withTracing);
    }

    private PagedResponse<SubscriptionProperties> listSubscriptionsNextPage(String topicName, String continuationToken,
                                                                          Context context) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return null;
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
        final int skip = Integer.parseInt(continuationToken);

        return listSubscriptions(topicName, skip, withTracing);
    }

    private PagedResponse<SubscriptionProperties> listSubscriptions(String topicName, int skip,
            Context context) {
        Response<Object> response =
            managementClient.listSubscriptionsSyncWithResponse(topicName, skip, NUMBER_OF_ELEMENTS, context);
            final SubscriptionDescriptionFeed feed = deserialize(response, SubscriptionDescriptionFeed.class);

            if (feed == null) {
                LOGGER.warning("Could not deserialize SubscriptionDescriptionFeed. skip {}, top: {}", skip,
                    NUMBER_OF_ELEMENTS);
                return null;
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
     *     authorization rules</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TopicProperties> listTopics() {
        return listTopics(null);
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
        return new PagedIterable<>(
            () -> listTopicsFirstPage(context != null ? context : Context.NONE),
            continuationToken -> listTopicsNextPage(continuationToken, context != null ? context : Context.NONE));
    }

    private PagedResponse<TopicProperties> listTopicsFirstPage(Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
        return listTopics(0, withTracing);
    }

    private PagedResponse<TopicProperties> listTopicsNextPage(String continuationToken, Context context) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return null;
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
        final int skip = Integer.parseInt(continuationToken);

        return listTopics(skip, withTracing);
    }

    private PagedResponse<TopicProperties> listTopics(int skip, Context context) {
        Response<Object> response =
            managementClient.listEntitiesSyncWithResponse(TOPICS_ENTITY_TYPE, skip, NUMBER_OF_ELEMENTS, context);
        final TopicDescriptionFeed feed = deserialize(response, TopicDescriptionFeed.class);
        if (feed == null) {
            LOGGER.warning("Could not deserialize TopicDescriptionFeed. skip {}, top: {}", skip,
                NUMBER_OF_ELEMENTS);
            return null;
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
            return extractPage(response, entities, feed.getLink());
        } catch (MalformedURLException | UnsupportedEncodingException error) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Could not parse response into FeedPage<TopicDescription>", error));
        }
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
        return updateQueueWithResponse(queue, null).getValue();
    }

    /**
     * Updates a queue with the given {@link QueueProperties}. The {@link QueueProperties} must be fully populated as
     * all the properties are replaced. If a property is not set the service default value is used.
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
        return updateQueueWithResponseSync(queue, context != null ? context : Context.NONE);
    }

    private Response<QueueProperties> updateQueueWithResponseSync(QueueProperties queue, Context context) {
        if (queue == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'queue' cannot be null"));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'context' cannot be null."));
        }

        final Context contextWithHeaders = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE)
            .addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders());
        final String forwardToEntity = queue.getForwardTo();
        if (!CoreUtils.isNullOrEmpty(forwardToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardToEntity, contextWithHeaders);
            queue.setForwardTo(getAbsoluteUrlFromEntity(forwardToEntity));
        }

        final String forwardDlqToEntity = queue.getForwardDeadLetteredMessagesTo();
        if (!CoreUtils.isNullOrEmpty(forwardDlqToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardDlqToEntity, contextWithHeaders);
            queue.setForwardDeadLetteredMessagesTo(getAbsoluteUrlFromEntity(forwardDlqToEntity));
        }

        final QueueDescription queueDescription = EntityHelper.toImplementation(queue);
        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType(CONTENT_TYPE)
            .setQueueDescription(queueDescription);
        final CreateQueueBody createEntity = new CreateQueueBody()
            .setContent(content);

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        Response<Object> response =
            entityClient.putSyncWithResponse(queue.getName(), createEntity, "*", contextWithHeaders);
        return deserializeQueue(response);
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
        return updateRuleWithResponse(topicName, subscriptionName, rule, null).getValue();
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
        return updateRuleWithResponseSync(topicName, subscriptionName, rule,
            context != null ? context : Context.NONE);
    }

    private Response<RuleProperties> updateRuleWithResponseSync(String topicName, String subscriptionName,
            RuleProperties rule, Context context) {
        if (rule == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'rule' cannot be null"));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'context' cannot be null."));
        }

        final RuleDescription implementation = EntityHelper.toImplementation(rule);
        final CreateRuleBodyContent content = new CreateRuleBodyContent()
            .setType(CONTENT_TYPE)
            .setRuleDescription(implementation);
        final CreateRuleBody ruleBody = new CreateRuleBody()
            .setContent(content);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        Response<Object> response =
            managementClient.getRules().putSyncWithResponse(topicName, subscriptionName, rule.getName(),
                ruleBody, "*", withTracing);
        return deserializeRule(response);
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
        return updateSubscriptionWithResponse(subscription, null).getValue();
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
        return updateSubscriptionWithResponseSync(subscription, context != null ? context : Context.NONE);
    }

    private Response<SubscriptionProperties> updateSubscriptionWithResponseSync(SubscriptionProperties subscription,
            Context context) {
        if (subscription == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'subscription' cannot be null"));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'context' cannot be null."));
        }
        final Context contextWithHeaders = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE)
            .addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders());
        final String forwardToEntity = subscription.getForwardTo();
        if (!CoreUtils.isNullOrEmpty(forwardToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardToEntity, contextWithHeaders);
            subscription.setForwardTo(getAbsoluteUrlFromEntity(forwardToEntity));
        }

        final String forwardDlqToEntity = subscription.getForwardDeadLetteredMessagesTo();
        if (!CoreUtils.isNullOrEmpty(forwardDlqToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardDlqToEntity, contextWithHeaders);
            subscription.setForwardDeadLetteredMessagesTo(getAbsoluteUrlFromEntity(forwardDlqToEntity));
        }

        final String topicName = subscription.getTopicName();
        final String subscriptionName = subscription.getSubscriptionName();
        final SubscriptionDescription implementation = EntityHelper.toImplementation(subscription);
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(implementation);
        final CreateSubscriptionBody createEntity = new CreateSubscriptionBody()
            .setContent(content);

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        Response<Object> response =
            managementClient.getSubscriptions().putSyncWithResponse(topicName, subscriptionName, createEntity,
                "*", contextWithHeaders);
        return deserializeSubscription(topicName, response);
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
        return updateTopicWithResponse(topic, null).getValue();
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
        return updateTopicWithResponseSync(topic, context != null ? context : Context.NONE);
    }

    private Response<TopicProperties> updateTopicWithResponseSync(TopicProperties topic, Context context) {
        if (topic == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'topic' cannot be null"));
        } else if (context == null) {
            throw LOGGER.logExceptionAsError( new NullPointerException("'context' cannot be null."));
        }

        final TopicDescription implementation = EntityHelper.toImplementation(topic);
        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(implementation);
        final CreateTopicBody createEntity = new CreateTopicBody()
            .setContent(content);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        Response<Object> response = entityClient.putSyncWithResponse(topic.getName(), createEntity, "*",
            withTracing);
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

    private Response<RuleProperties> deserializeRule(Response<Object> response) {
        final RuleDescriptionEntry entry = deserialize(response.getValue(), RuleDescriptionEntry.class);

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            LOGGER.info("entry.getContent() is null. The entity may not exist. {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final RuleDescription description = entry.getContent().getRuleDescription();
        final RuleProperties result = EntityHelper.toModel(description);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), result);
    }

    private Response<SubscriptionProperties> deserializeSubscription(String topicName, Response<Object> response) {
        final SubscriptionDescriptionEntry entry = deserialize(response.getValue(), SubscriptionDescriptionEntry.class);

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            LOGGER.warning("entry.getContent() is null. There should have been content returned. Entry: {}", entry);
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
     * Creates a {@link ServiceBusAdministrationAsyncClient.FeedPage} given the elements and a set of response links to get the next link from.
     *
     * @param entities Entities in the feed.
     * @param responseLinks Links returned from the feed.
     * @param <TResult> Type of Service Bus entities in page.
     *
     * @return A {@link ServiceBusAdministrationAsyncClient.FeedPage} indicating whether this can be continued or not.
     * @throws MalformedURLException if the "next" page link does not contain a well-formed URL.
     */
    private <TResult, TFeed> ServiceBusAdministrationAsyncClient.FeedPage<TResult> extractPage(Response<TFeed> response,
            List<TResult> entities, List<ResponseLink> responseLinks)
            throws MalformedURLException, UnsupportedEncodingException {
        final Optional<ResponseLink> nextLink = responseLinks.stream()
            .filter(link -> link.getRel().equalsIgnoreCase("next"))
            .findFirst();

        if (nextLink.isEmpty()) {
            return new ServiceBusAdministrationAsyncClient.FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
        }

        final URL url = new URL(nextLink.get().getHref());
        final String decode = URLDecoder.decode(url.getQuery(), StandardCharsets.UTF_8);
        final Optional<Integer> skipParameter = Arrays.stream(decode.split("&amp;|&"))
            .map(part -> part.split("=", 2))
            .filter(parts -> parts[0].equalsIgnoreCase("$skip") && parts.length == 2)
            .map(parts -> Integer.valueOf(parts[1]))
            .findFirst();

        if (skipParameter.isPresent()) {
            return new ServiceBusAdministrationAsyncClient.FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities,
                skipParameter.get());
        } else {
            LOGGER.warning("There should have been a skip parameter for the next page.");
            return new ServiceBusAdministrationAsyncClient.FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
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
}
