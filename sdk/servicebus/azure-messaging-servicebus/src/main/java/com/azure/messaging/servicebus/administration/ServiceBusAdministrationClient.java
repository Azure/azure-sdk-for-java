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
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.implementation.EntitiesImpl;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.RulesImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBody;
import com.azure.messaging.servicebus.administration.implementation.models.NamespacePropertiesEntry;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementErrorException;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionFeed;
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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.NUMBER_OF_ELEMENTS;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.QUEUES_ENTITY_TYPE;
import static com.azure.messaging.servicebus.administration.implementation.EntityHelper.TOPICS_ENTITY_TYPE;

/**
 * A <b>synchronous</b> client for managing a Service Bus namespace. Instantiated via
 * {@link ServiceBusAdministrationClientBuilder}.
 * <p><strong>Sample: Create the async client</strong></p>
 *
 * <p>The follow code sample demonstrates the creation of the async administration client.  The credential used in the
 * following sample is {@code DefaultAzureCredential} for authentication. It is appropriate for most scenarios,
 * including local development and production environments. Additionally, we recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.  You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation</a>.
 * </p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.instantiation -->
 * <pre>
 * HttpLogOptions logOptions = new HttpLogOptions&#40;&#41;
 *     .setLogLevel&#40;HttpLogDetailLevel.HEADERS&#41;;
 *
 * &#47;&#47; DefaultAzureCredential creates a credential based on the environment it is executed in.
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, tokenCredential&#41;
 *     .httpLogOptions&#40;logOptions&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.instantiation -->
 *
 * <p><strong>Sample: Create a queue</strong></p>
 *
 * <p>The following sample creates a queue with default values.  Default values are listed in
 * {@link CreateQueueOptions#CreateQueueOptions()}.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string -->
 * <pre>
 * QueueProperties queue = client.createQueue&#40;&quot;my-new-queue&quot;&#41;;
 * System.out.printf&#40;&quot;Queue created. Name: %s. Lock Duration: %s.%n&quot;,
 *     queue.getName&#40;&#41;, queue.getLockDuration&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string -->
 *
 * <p><strong>Sample: Edit an existing subscription</strong></p>
 *
 * <p>The following code sample demonstrates updating an existing subscription.  Users should fetch the subscription's
 * properties, modify the properties, and then pass the object to update method.</p>
 *
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
 * <p><strong>Sample: List all queues</strong></p>
 *
 * <p>The following code sample lists all the queues in the Service Bus namespace.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.listQueues -->
 * <pre>
 * client.listQueues&#40;&#41;.forEach&#40;queue -&gt; &#123;
 *     System.out.printf&#40;&quot;Queue [%s]. Lock Duration: %s.%n&quot;,
 *         queue.getName&#40;&#41;, queue.getLockDuration&#40;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.listQueues -->
 *
 * <p><strong>Sample: Delete queue</strong></p>
 *
 * <p>The code sample below demonstrates deleting an existing queue.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.deletequeue -->
 * <pre>
 * try &#123;
 *     client.deleteQueue&#40;&quot;my-existing-queue&quot;&#41;;
 * &#125; catch &#40;AzureException exception&#41; &#123;
 *     System.err.println&#40;&quot;Exception occurred deleting queue: &quot; + exception&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.deletequeue -->
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
    private final RulesImpl rulesClient;
    private final AdministrationModelConverter converter;

    /**
     * Creates a new instance with the given client.
     *
     * @param managementClient Client to make management calls.
     * @throws NullPointerException if {@code managementClient} is null.
     */
    ServiceBusAdministrationClient(ServiceBusManagementClientImpl managementClient) {

        this.managementClient = Objects.requireNonNull(managementClient, "'managementClient' cannot be null.");
        this.entityClient = managementClient.getEntities();
        this.rulesClient = managementClient.getRules();
        this.converter = new AdministrationModelConverter(LOGGER, managementClient.getEndpoint());
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
        converter.validateQueueName(queueName);
        if (queueOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'queueOptions' cannot be null."));
        }

        final Context contextWithHeaders = enableSyncContext(context);
        final CreateQueueBody createEntity = converter.getCreateQueueBody(queueOptions, contextWithHeaders);

        final Response<Object> response = executeAndThrowException(
            () -> entityClient.putWithResponse(queueName, createEntity, null, contextWithHeaders));

        return converter.deserializeQueue(response);
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
    public Response<RuleProperties> createRuleWithResponse(String topicName, String subscriptionName, String ruleName,
        CreateRuleOptions ruleOptions, Context context) {
        converter.validateTopicName(topicName);
        converter.validateSubscriptionName(subscriptionName);
        converter.validateRuleName(ruleName);

        if (ruleOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'ruleOptions' cannot be null."));
        }

        final CreateRuleBody createEntity = converter.getCreateRuleBody(ruleName, ruleOptions);

        final Response<RuleDescriptionEntry> response = executeAndThrowException(() -> managementClient.getRules()
            .putWithResponse(topicName, subscriptionName, ruleName, createEntity, null, enableSyncContext(context)));

        return converter.getRulePropertiesSimpleResponse(response);
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
        CreateSubscriptionOptions subscriptionOptions, CreateRuleOptions ruleOptions) {
        return createSubscriptionWithResponse(topicName, subscriptionName, ruleName, subscriptionOptions, ruleOptions,
            null).getValue();
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

        return createSubscriptionWithResponse(topicName, subscriptionName, null, subscriptionOptions, null, context);
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
        String ruleName, CreateSubscriptionOptions subscriptionOptions, CreateRuleOptions ruleOptions,
        Context context) {
        converter.validateTopicName(topicName);
        converter.validateSubscriptionName(subscriptionName);

        if (subscriptionOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'subscriptionOptions' cannot be null."));
        }

        final Context contextWithHeaders = converter.getContext(context);
        final CreateSubscriptionBody createEntity
            = converter.getCreateSubscriptionBody(subscriptionOptions, ruleName, ruleOptions, contextWithHeaders);

        final Response<SubscriptionDescriptionEntry> response
            = executeAndThrowException(() -> managementClient.getSubscriptions()
                .putWithResponse(topicName, subscriptionName, createEntity, null, contextWithHeaders));

        return converter.getSubscriptionPropertiesSimpleResponse(topicName, response);
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
        converter.validateTopicName(topicName);
        if (topicOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'topicOptions' cannot be null."));
        }

        final CreateTopicBody createEntity
            = converter.getCreateTopicBody(EntityHelper.getTopicDescription(topicOptions));

        final Response<Object> response = executeAndThrowException(
            () -> entityClient.putWithResponse(topicName, createEntity, null, enableSyncContext(context)));

        return converter.deserializeTopic(response);
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
        converter.validateQueueName(queueName);

        Response<Object> response
            = executeAndThrowException(() -> entityClient.deleteWithResponse(queueName, enableSyncContext(context)));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
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
    public Response<Void> deleteRuleWithResponse(String topicName, String subscriptionName, String ruleName,
        Context context) {
        converter.validateTopicName(topicName);
        converter.validateSubscriptionName(subscriptionName);
        converter.validateRuleName(ruleName);

        final Response<RuleDescriptionEntry> response = executeAndThrowException(
            () -> rulesClient.deleteWithResponse(topicName, subscriptionName, ruleName, enableSyncContext(context)));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
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
        converter.validateSubscriptionName(subscriptionName);
        converter.validateTopicName(topicName);
        final Response<SubscriptionDescriptionEntry> response
            = executeAndThrowException(() -> managementClient.getSubscriptions()
                .deleteWithResponse(topicName, subscriptionName, enableSyncContext(context)));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
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
        converter.validateTopicName(topicName);

        final Response<Object> response
            = executeAndThrowException(() -> entityClient.deleteWithResponse(topicName, enableSyncContext(context)));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
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
            final HttpResponse notFoundResponse
                = new AdministrationModelConverter.EntityNotFoundHttpResponse<>(response);
            throw LOGGER.logExceptionAsError(new ResourceNotFoundException(
                String.format("Queue '%s' does not exist.", queueName), notFoundResponse));
        }
        return response;
    }

    private Response<QueueProperties> getQueueInternal(String queueName, Context context) {
        converter.validateQueueName(queueName);
        final Response<Object> response
            = executeAndThrowException(() -> entityClient.getWithResponse(queueName, true, enableSyncContext(context)));

        return converter.deserializeQueue(response);
    }

    /**
     * Gets whether a queue with {@code queueName} exists in the Service Bus namespace.
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
     * Gets whether a queue with {@code queueName} exists in the Service Bus namespace.
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
        return getEntityExistsWithResponse(() -> getQueueInternal(queueName, context));
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
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            new QueueRuntimeProperties(response.getValue()));
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
        final Response<NamespacePropertiesEntry> response = executeAndThrowException(
            () -> managementClient.getNamespaces().getWithResponse(enableSyncContext(context)));

        final NamespacePropertiesEntry entry = response.getValue();
        if (entry == null || entry.getContent() == null) {
            throw LOGGER.logExceptionAsError(
                new AzureException("There was no content inside namespace response. Entry: " + response));
        }

        final NamespaceProperties namespaceProperties = entry.getContent().getNamespaceProperties();
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            namespaceProperties);
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
    public Response<RuleProperties> getRuleWithResponse(String topicName, String subscriptionName, String ruleName,
        Context context) {

        final Response<RuleDescriptionEntry> response = executeAndThrowException(
            () -> rulesClient.getWithResponse(topicName, subscriptionName, ruleName, true, enableSyncContext(context)));

        return converter.getRulePropertiesSimpleResponse(response);
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
    public Response<SubscriptionProperties> getSubscriptionWithResponse(String topicName, String subscriptionName,
        Context context) {
        return getSubscriptionInternal(topicName, subscriptionName, context);
    }

    private Response<SubscriptionProperties> getSubscriptionInternal(String topicName, String subscriptionName,
        Context context) {
        converter.validateTopicName(topicName);
        converter.validateSubscriptionName(subscriptionName);

        Response<SubscriptionDescriptionEntry> response
            = executeAndThrowException(() -> managementClient.getSubscriptions()
                .getWithResponse(topicName, subscriptionName, true, enableSyncContext(context)));

        return converter.getSubscriptionPropertiesSimpleResponse(topicName, response);
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
        return getSubscriptionExistsWithResponse(topicName, subscriptionName, null).getValue();
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

        return getEntityExistsWithResponse(() -> getSubscriptionInternal(topicName, subscriptionName, context));
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
    public Response<SubscriptionRuntimeProperties> getSubscriptionRuntimePropertiesWithResponse(String topicName,
        String subscriptionName, Context context) {
        final Response<SubscriptionProperties> response
            = getSubscriptionWithResponse(topicName, subscriptionName, context);
        if (response.getValue() == null) {
            final HttpResponse notFoundResponse
                = new AdministrationModelConverter.EntityNotFoundHttpResponse<>(response);
            throw LOGGER.logExceptionAsError(new ResourceNotFoundException(
                String.format("Subscription '%s' in topic '%s' does not exist.", topicName, subscriptionName),
                notFoundResponse));
        }
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            new SubscriptionRuntimeProperties(response.getValue()));
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
            final HttpResponse notFoundResponse
                = new AdministrationModelConverter.EntityNotFoundHttpResponse<>(response);
            throw LOGGER.logExceptionAsError(new ResourceNotFoundException(
                String.format("Topic '%s' does not exist.", topicName), notFoundResponse));
        }
        return response;
    }

    private Response<TopicProperties> getTopicInternal(String topicName, Context context) {
        converter.validateTopicName(topicName);
        final Response<Object> response
            = executeAndThrowException(() -> entityClient.getWithResponse(topicName, true, enableSyncContext(context)));

        return converter.deserializeTopic(response);
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
        return getEntityExistsWithResponse(() -> getTopicInternal(topicName, context));
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
            final HttpResponse notFoundResponse
                = new AdministrationModelConverter.EntityNotFoundHttpResponse<>(response);
            throw LOGGER.logExceptionAsError(new ResourceNotFoundException(
                String.format("Topic '%s' does not exist.", topicName), notFoundResponse));
        }
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            new TopicRuntimeProperties(response.getValue()));
    }

    private <T> Response<Boolean> getEntityExistsWithResponse(Supplier<Response<T>> supplier) {
        try {
            // When an entity does not exist, it does not have any description object in it.
            final Response<T> getEntityOperation = supplier.get();
            final boolean exists = getEntityOperation.getValue() != null;

            return new SimpleResponse<>(getEntityOperation.getRequest(), getEntityOperation.getStatusCode(),
                getEntityOperation.getHeaders(), exists);
        } catch (ResourceNotFoundException exception) {
            final HttpResponse response = exception.getResponse();

            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), false);
        }
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
        return new PagedIterable<>(() -> listQueues(0, context), continuationToken -> {
            if (continuationToken == null || continuationToken.isEmpty()) {
                return null;
            }
            final int skip = Integer.parseInt(continuationToken);

            return listQueues(skip, context);
        });
    }

    private PagedResponse<QueueProperties> listQueues(int skip, Context context) {
        final Response<Object> response = executeAndThrowException(() -> managementClient
            .listEntitiesWithResponse(QUEUES_ENTITY_TYPE, skip, NUMBER_OF_ELEMENTS, enableSyncContext(context)));
        final Response<QueueDescriptionFeed> feedResponse = converter.deserializeQueueFeed(response);
        final QueueDescriptionFeed feed = feedResponse.getValue();

        if (feed == null) {
            LOGGER.warning("Could not deserialize QueueDescriptionFeed. skip {}, top: {}", skip, NUMBER_OF_ELEMENTS);
            return null;
        }

        final List<QueueProperties> entities = converter.getQueues(feed);
        try {
            return converter.extractPage(response, entities, feed.getLink());
        } catch (MalformedURLException | UnsupportedEncodingException error) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Could not parse response into FeedPage<RuleDescription>", error));
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
        return new PagedIterable<>(() -> listRules(topicName, subscriptionName, 0, context), continuationToken -> {
            if (continuationToken == null || continuationToken.isEmpty()) {
                return null;
            }
            final int skip = Integer.parseInt(continuationToken);

            return listRules(topicName, subscriptionName, skip, context);
        });
    }

    private PagedResponse<RuleProperties> listRules(String topicName, String subscriptionName, int skip,
        Context context) {
        final Response<RuleDescriptionFeed> response = executeAndThrowException(() -> managementClient
            .listRulesWithResponse(topicName, subscriptionName, skip, NUMBER_OF_ELEMENTS, enableSyncContext(context)));
        final RuleDescriptionFeed feed = response.getValue();

        if (feed == null) {
            LOGGER.warning("Could not deserialize RuleDescriptionFeed. skip {}, top: {}", skip, NUMBER_OF_ELEMENTS);
            return null;
        }

        final List<RuleProperties> entities = converter.getRules(feed);
        try {
            return converter.extractPage(response, entities, feed.getLink());
        } catch (MalformedURLException | UnsupportedEncodingException error) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Could not parse response into FeedPage<RuleDescription>", error));
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
        return new PagedIterable<>(() -> listSubscriptions(topicName, 0, context), continuationToken -> {
            if (continuationToken == null || continuationToken.isEmpty()) {
                return null;
            }
            final int skip = Integer.parseInt(continuationToken);
            return listSubscriptions(topicName, skip, context);
        });
    }

    private PagedResponse<SubscriptionProperties> listSubscriptions(String topicName, int skip, Context context) {
        final Response<SubscriptionDescriptionFeed> response = executeAndThrowException(() -> managementClient
            .listSubscriptionsWithResponse(topicName, skip, NUMBER_OF_ELEMENTS, enableSyncContext(context)));
        final SubscriptionDescriptionFeed feed = response.getValue();

        if (feed == null) {
            LOGGER.warning("Could not deserialize SubscriptionDescriptionFeed. skip {}, top: {}", skip,
                NUMBER_OF_ELEMENTS);
            return null;
        }

        final List<SubscriptionProperties> entities = converter.getSubscriptions(topicName, feed);
        try {
            return converter.extractPage(response, entities, feed.getLink());
        } catch (MalformedURLException | UnsupportedEncodingException error) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Could not parse response into FeedPage<SubscriptionDescription>", error));
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
        return new PagedIterable<>(() -> listTopics(0, context), continuationToken -> {
            if (continuationToken == null || continuationToken.isEmpty()) {
                return null;
            }

            final int skip = Integer.parseInt(continuationToken);

            return listTopics(skip, context);
        });
    }

    private PagedResponse<TopicProperties> listTopics(int skip, Context context) {
        final Response<Object> response = executeAndThrowException(() -> managementClient
            .listEntitiesWithResponse(TOPICS_ENTITY_TYPE, skip, NUMBER_OF_ELEMENTS, enableSyncContext(context)));
        final Response<TopicDescriptionFeed> feedResponse = converter.deserializeTopicFeed(response);
        final TopicDescriptionFeed feed = feedResponse.getValue();

        if (feed == null) {
            LOGGER.warning("Could not deserialize TopicDescriptionFeed. skip {}, top: {}", skip, NUMBER_OF_ELEMENTS);
            return null;
        }

        final List<TopicProperties> entities = converter.getTopics(feed);
        try {
            return converter.extractPage(response, entities, feed.getLink());
        } catch (MalformedURLException | UnsupportedEncodingException error) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Could not parse response into FeedPage<TopicDescription>", error));
        }
    }

    /**
     * Updates a queue with the given {@link QueueProperties}. The {@link QueueProperties} must be fully populated as
     * all the properties are replaced. If a property is not set the service default value is used.
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
     * all the properties are replaced. If a property is not set the service default value is used.
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

        final Context contextWithHeaders = enableSyncContext(context);
        final CreateQueueBody createEntity = converter.getUpdateQueueBody(queue, contextWithHeaders);

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        final Response<Object> response = executeAndThrowException(
            () -> entityClient.putWithResponse(queue.getName(), createEntity, "*", contextWithHeaders));

        return converter.deserializeQueue(response);
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

        final Response<RuleDescriptionEntry> response = executeAndThrowException(() -> managementClient.getRules()
            .putWithResponse(topicName, subscriptionName, rule.getName(), converter.getUpdateRuleBody(rule), "*",
                enableSyncContext(context)));

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        return converter.getRulePropertiesSimpleResponse(response);
    }

    /**
     * Updates a subscription with the given {@link SubscriptionProperties}. The {@link SubscriptionProperties} must be
     * fully populated as all the properties are replaced. If a property is not set the service default value is
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
     * fully populated as all the properties are replaced. If a property is not set the service default value is
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
    public Response<SubscriptionProperties> updateSubscriptionWithResponse(SubscriptionProperties subscription,
        Context context) {

        if (subscription == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'subscription' cannot be null"));
        }

        final Context contextWithHeaders = enableSyncContext(context);
        final String topicName = subscription.getTopicName();
        final CreateSubscriptionBody createEntity
            = converter.getUpdateSubscriptionBody(subscription, contextWithHeaders);

        final Response<SubscriptionDescriptionEntry> response
            = executeAndThrowException(() -> managementClient.getSubscriptions()
                .putWithResponse(topicName, subscription.getSubscriptionName(), createEntity, "*", contextWithHeaders));

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        return converter.getSubscriptionPropertiesSimpleResponse(topicName, response);
    }

    /**
     * Updates a topic with the given {@link TopicProperties}. The {@link TopicProperties} must be fully populated as
     * all the properties are replaced. If a property is not set the service default value is used.
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
     * all the properties are replaced. If a property is not set the service default value is used.
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
        final CreateTopicBody createEntity = converter.getUpdateTopicBody(topic);

        // If-Match == "*" to unconditionally update. This is in line with the existing client library behaviour.
        final Response<Object> response = executeAndThrowException(
            () -> entityClient.putWithResponse(topic.getName(), createEntity, "*", enableSyncContext(context)));

        return converter.deserializeTopic(response);
    }

    private Context enableSyncContext(Context context) {
        return converter.getContext(context).addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    /**
     * Performs the service call and maps potential {@link ServiceBusManagementErrorException} to their corresponding
     * Azure Core HTTP exception.
     *
     * @param serviceCall Service call to make.
     * @param <T> Type of response.
     *
     * @return The response.
     */
    private static <T> Response<T> executeAndThrowException(Supplier<Response<T>> serviceCall) {
        try {
            return serviceCall.get();
        } catch (ServiceBusManagementErrorException error) {
            throw AdministrationModelConverter.mapException(error);
        }
    }
}
