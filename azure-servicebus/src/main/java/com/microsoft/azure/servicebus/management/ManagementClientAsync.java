package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.primitives.*;
import com.microsoft.azure.servicebus.rules.RuleDescription;
import com.microsoft.azure.servicebus.security.SecurityToken;
import com.microsoft.azure.servicebus.security.TokenProvider;
import org.asynchttpclient.*;
import org.asynchttpclient.util.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/**
 * Asynchronous client to perform management operations on Service Bus entities.
 * Operations return CompletableFuture which asynchronously return the responses.
 */
public class ManagementClientAsync {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ManagementClientAsync.class);

    private static final int ONE_BOX_HTTPS_PORT = 4446;
    private static final String API_VERSION_QUERY = "api-version=2017-04";
    private static final String USER_AGENT_HEADER_NAME = "User-Agent";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE = "application/atom+xml";
    private static final Duration CONNECTION_TIMEOUT = Duration.ofMinutes(1);
    private static final String USER_AGENT = String.format("%s/%s(%s)", ClientConstants.PRODUCT_NAME, ClientConstants.CURRENT_JAVACLIENT_VERSION, ClientConstants.PLATFORM_INFO);

    private ClientSettings clientSettings;
    private URI namespaceEndpointURI;
    private AsyncHttpClient asyncHttpClient;

    /**
     * Creates a new {@link ManagementClientAsync}.
     * User should call {@link ManagementClientAsync#close()} at the end of life of the client.
     * @param connectionStringBuilder - connectionStringBuilder containing namespace information and client settings.
     */
    public ManagementClientAsync(ConnectionStringBuilder connectionStringBuilder) {
        this(connectionStringBuilder.getEndpoint(), Util.getClientSettingsFromConnectionStringBuilder(connectionStringBuilder));
    }

    /**
     * Creates a new {@link ManagementClientAsync}.
     * User should call {@link ManagementClientAsync#close()} at the end of life of the client.
     * @param namespaceEndpointURI - URI of the namespace connecting to.
     * @param clientSettings - client settings.
     */
    public ManagementClientAsync(URI namespaceEndpointURI, ClientSettings clientSettings) {
        this.namespaceEndpointURI = namespaceEndpointURI;
        this.clientSettings = clientSettings;
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout((int)CONNECTION_TIMEOUT.toMillis())
                .setRequestTimeout((int)this.clientSettings.getOperationTimeout().toMillis());
        this.asyncHttpClient = asyncHttpClient(clientBuilder);
    }

    /**
     * Retrieves a queue from the service namespace
     * @param path - The path of the queue relative to service bus namespace.
     * @return - QueueDescription containing information about the queue.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     */
    public CompletableFuture<QueueDescription> getQueueAsync(String path) {
        EntityNameHelper.checkValidQueueName(path);

        CompletableFuture<String> contentFuture = getEntityAsync(path, null, false);
        CompletableFuture<QueueDescription> qdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                qdFuture.completeExceptionally(ex);
            } else {
                try {
                    qdFuture.complete(QueueDescriptionSerializer.parseFromContent(content));
                } catch (MessagingEntityNotFoundException e) {
                    qdFuture.completeExceptionally(e);
                }
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return qdFuture;
    }

    /**
     * Retrieves the runtime information of a queue.
     * @param path - The path of the queue relative to service bus namespace.
     * @return - QueueRuntimeInfo containing runtime information about the queue.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     */
    public CompletableFuture<QueueRuntimeInfo> getQueueRuntimeInfoAsync(String path) {
        EntityNameHelper.checkValidQueueName(path);

        CompletableFuture<String> contentFuture = getEntityAsync(path, null, true);
        CompletableFuture<QueueRuntimeInfo> qdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                qdFuture.completeExceptionally(ex);
            } else {
                try {
                    qdFuture.complete(QueueRuntimeInfoSerializer.parseFromContent(content));
                } catch (MessagingEntityNotFoundException e) {
                    qdFuture.completeExceptionally(e);
                }
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return qdFuture;
    }

    /**
     * Retrieves a topic from the service namespace
     * @param path - The path of the queue relative to service bus namespace.
     * @return - Description containing information about the topic.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     */
    public CompletableFuture<TopicDescription> getTopicAsync(String path) {
        EntityNameHelper.checkValidTopicName(path);

        CompletableFuture<String> contentFuture = getEntityAsync(path, null, false);
        CompletableFuture<TopicDescription> tdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                tdFuture.completeExceptionally(ex);
            } else {
                try {
                    tdFuture.complete(TopicDescriptionSerializer.parseFromContent(content));
                } catch (MessagingEntityNotFoundException e) {
                    tdFuture.completeExceptionally(e);
                }
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return tdFuture;
    }

    /**
     * Retrieves the runtime information of a topic
     * @param path - The path of the queue relative to service bus namespace.
     * @return - TopicRuntimeInfo containing runtime information about the topic.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     */
    public CompletableFuture<TopicRuntimeInfo> getTopicRuntimeInfoAsync(String path) {
        EntityNameHelper.checkValidTopicName(path);

        CompletableFuture<String> contentFuture = getEntityAsync(path, null, true);
        CompletableFuture<TopicRuntimeInfo> tdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                tdFuture.completeExceptionally(ex);
            } else {
                try {
                    tdFuture.complete(TopicRuntimeInfoSerializer.parseFromContent(content));
                } catch (MessagingEntityNotFoundException e) {
                    tdFuture.completeExceptionally(e);
                }
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return tdFuture;
    }

    /**
     * Retrieves a subscription for a given topic from the service namespace
     * @param topicPath - The path of the topic relative to service bus namespace.
     * @param subscriptionName - The name of the subscription
     * @return - SubscriptionDescription containing information about the subscription.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     */
    public CompletableFuture<SubscriptionDescription> getSubscriptionAsync(String topicPath, String subscriptionName) {
        EntityNameHelper.checkValidTopicName(topicPath);
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);

        String path = EntityNameHelper.formatSubscriptionPath(topicPath, subscriptionName);
        CompletableFuture<String> contentFuture = getEntityAsync(path, null, false);
        CompletableFuture<SubscriptionDescription> sdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                sdFuture.completeExceptionally(ex);
            } else {
                try {
                    sdFuture.complete(SubscriptionDescriptionSerializer.parseFromContent(topicPath, content));
                } catch (MessagingEntityNotFoundException e) {
                    sdFuture.completeExceptionally(e);
                }
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return sdFuture;
    }

    /**
     * Retrieves the runtime information of a subscription in a given topic
     * @param topicPath - The path of the topic relative to service bus namespace.
     * @param subscriptionName - The name of the subscription
     * @return - SubscriptionRuntimeInfo containing the runtime information about the subscription.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     */
    public CompletableFuture<SubscriptionRuntimeInfo> getSubscriptionRuntimeInfoAsync(String topicPath, String subscriptionName) {
        EntityNameHelper.checkValidTopicName(topicPath);
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);

        String path = EntityNameHelper.formatSubscriptionPath(topicPath, subscriptionName);
        CompletableFuture<String> contentFuture = getEntityAsync(path, null, true);
        CompletableFuture<SubscriptionRuntimeInfo> sdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                sdFuture.completeExceptionally(ex);
            } else {
                try {
                    sdFuture.complete(SubscriptionRuntimeInfoSerializer.parseFromContent(topicPath, content));
                } catch (MessagingEntityNotFoundException e) {
                    sdFuture.completeExceptionally(e);
                }
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return sdFuture;
    }

    /**
     * Retrieves a rule for a given topic and subscription from the service namespace
     * @param topicPath - The path of the topic relative to service bus namespace.
     * @param subscriptionName - The name of the subscription.
     * @param ruleName - The name of the rule.
     * @return - RuleDescription containing information about the subscription.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     */
    public CompletableFuture<RuleDescription> getRuleAsync(String topicPath, String subscriptionName, String ruleName) {
        EntityNameHelper.checkValidTopicName(topicPath);
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);
        EntityNameHelper.checkValidRuleName(ruleName);

        String path = EntityNameHelper.formatRulePath(topicPath, subscriptionName, ruleName);
        CompletableFuture<String> contentFuture = getEntityAsync(path, null, false);
        CompletableFuture<RuleDescription> rdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                rdFuture.completeExceptionally(ex);
            } else {
                try {
                    rdFuture.complete(RuleDescriptionSerializer.parseFromContent(content));
                } catch (MessagingEntityNotFoundException e) {
                    rdFuture.completeExceptionally(e);
                }
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return rdFuture;
    }

    /**
     * Retrieves the list of queues present in the namespace.
     * @return the first 100 queues.
     */
    public CompletableFuture<List<QueueDescription>> getQueuesAsync() {
        return getQueuesAsync(100, 0);
    }

    /**
     * Retrieves the list of queues present in the namespace.
     * You can simulate pages of list of entities by manipulating count and skip parameters.
     * skip(0)+count(100) gives first 100 entities. skip(100)+count(100) gives the next 100 entities.
     * @return the list of queues.
     * @param count - The number of queues to fetch. Defaults to 100. Maximum value allowed is 100.
     * @param skip - The number of queues to skip. Defaults to 0. Cannot be negative.
     */
    public CompletableFuture<List<QueueDescription>> getQueuesAsync(int count, int skip) {
        if (count > 100 || count < 1) {
            throw new IllegalArgumentException("Count should be between 1 and 100");
        }

        if (skip < 0) {
            throw new IllegalArgumentException("Skip cannot be negative");
        }

        CompletableFuture<String> contentFuture = getEntityAsync("$Resources/queues", String.format("$skip=%d&$top=%d", skip, count), false);
        CompletableFuture<List<QueueDescription>> qdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                qdFuture.completeExceptionally(ex);
            } else {
                qdFuture.complete(QueueDescriptionSerializer.parseCollectionFromContent(content));
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return qdFuture;
    }

    /**
     * Retrieves the list of topics present in the namespace.
     * @return the first 100 topics.
     */
    public CompletableFuture<List<TopicDescription>> getTopicsAsync() {
        return getTopicsAsync(100, 0);
    }

    /**
     * Retrieves the list of topics present in the namespace.
     * You can simulate pages of list of entities by manipulating count and skip parameters.
     * skip(0)+count(100) gives first 100 entities. skip(100)+count(100) gives the next 100 entities.
     * @return the list of topics.
     * @param count - The number of topics to fetch. Defaults to 100. Maximum value allowed is 100.
     * @param skip - The number of topics to skip. Defaults to 0. Cannot be negative.
     */
    public CompletableFuture<List<TopicDescription>> getTopicsAsync(int count, int skip) {
        if (count > 100 || count < 1) {
            throw new IllegalArgumentException("Count should be between 1 and 100");
        }

        if (skip < 0) {
            throw new IllegalArgumentException("Skip cannot be negative");
        }

        CompletableFuture<String> contentFuture = getEntityAsync("$Resources/topics", String.format("$skip=%d&$top=%d", skip, count), false);
        CompletableFuture<List<TopicDescription>> tdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                tdFuture.completeExceptionally(ex);
            } else {
                tdFuture.complete(TopicDescriptionSerializer.parseCollectionFromContent(content));
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return tdFuture;
    }

    /**
     * Retrieves the list of subscriptions for a given topic in the namespace.
     * @return the first 100 subscriptions.
     */
    public CompletableFuture<List<SubscriptionDescription>> getSubscriptionsAsync(String topicName) {
        return getSubscriptionsAsync(topicName, 100, 0);
    }

    /**
     * Retrieves the list of subscriptions for a given topic in the namespace.
     * You can simulate pages of list of entities by manipulating count and skip parameters.
     * skip(0)+count(100) gives first 100 entities. skip(100)+count(100) gives the next 100 entities.
     * @return the list of subscriptions.
     * @param count - The number of subscriptions to fetch. Defaults to 100. Maximum value allowed is 100.
     * @param skip - The number of subscriptions to skip. Defaults to 0. Cannot be negative.
     */
    public CompletableFuture<List<SubscriptionDescription>> getSubscriptionsAsync(String topicName, int count, int skip) {
        if (count > 100 || count < 1) {
            throw new IllegalArgumentException("Count should be between 1 and 100");
        }

        if (skip < 0) {
            throw new IllegalArgumentException("Skip cannot be negative");
        }

        EntityNameHelper.checkValidTopicName(topicName);

        CompletableFuture<String> contentFuture = getEntityAsync(String.format("%s/Subscriptions", topicName), String.format("$skip=%d&$top=%d", skip, count), false);
        CompletableFuture<List<SubscriptionDescription>> sdFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                sdFuture.completeExceptionally(ex);
            } else {
                sdFuture.complete(SubscriptionDescriptionSerializer.parseCollectionFromContent(topicName, content));
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return sdFuture;
    }

    /**
     * Retrieves the list of rules for a given topic-subscription in the namespace.
     * @return the first 100 rules.
     */
    public CompletableFuture<List<RuleDescription>> getRulesAsync(String topicName, String subscriptionName) {
        return getRulesAsync(topicName, subscriptionName, 100, 0);
    }

    /**
     * Retrieves the list of rules for a given topic-subscription in the namespace.
     * You can simulate pages of list of entities by manipulating count and skip parameters.
     * skip(0)+count(100) gives first 100 entities. skip(100)+count(100) gives the next 100 entities.
     * @return the list of rules.
     * @param count - The number of rules to fetch. Defaults to 100. Maximum value allowed is 100.
     * @param skip - The number of rules to skip. Defaults to 0. Cannot be negative.
     */
    public CompletableFuture<List<RuleDescription>> getRulesAsync(String topicName, String subscriptionName, int count, int skip) {
        if (count > 100 || count < 1) {
            throw new IllegalArgumentException("Count should be between 1 and 100");
        }

        if (skip < 0) {
            throw new IllegalArgumentException("Skip cannot be negative");
        }

        EntityNameHelper.checkValidTopicName(topicName);
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);

        CompletableFuture<String> contentFuture = getEntityAsync(
                String.format("%s/Subscriptions/%s/rules", topicName, subscriptionName),
                String.format("$skip=%d&$top=%d", skip, count),
                false);
        CompletableFuture<List<RuleDescription>> rulesFuture = new CompletableFuture<>();
        contentFuture.handleAsync((content, ex) -> {
            if (ex != null) {
                rulesFuture.completeExceptionally(ex);
            } else {
                rulesFuture.complete(RuleDescriptionSerializer.parseCollectionFromContent(content));
            }
            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return rulesFuture;
    }

    private CompletableFuture<String> getEntityAsync(String path, String query, boolean enrich) {
        String queryString = API_VERSION_QUERY + "&enrich=" + enrich;
        if (query != null) {
            queryString = queryString + "&" + query;
        }

        URL entityURL = null;
        try {
            entityURL = getManagementURL(this.namespaceEndpointURI, path, queryString);
        } catch (ServiceBusException e) {
            final CompletableFuture<String> exceptionFuture = new CompletableFuture<>();
            exceptionFuture.completeExceptionally(e);
            return exceptionFuture;
        }

        return sendManagementHttpRequestAsync(HttpConstants.Methods.GET, entityURL, null, null);
    }

    /**
     * Creates a new queue in the service namespace with the given name.
     * See {@link QueueDescription} for default values of queue properties.
     * @param queuePath - The name of the queue relative to the service namespace base address.
     * @return {@link QueueDescription} of the newly created queue.
     * @throws IllegalArgumentException - Entity name is null, empty, too long or uses illegal characters.
     */
    public CompletableFuture<QueueDescription> createQueueAsync(String queuePath) {
        return this.createQueueAsync(new QueueDescription(queuePath));
    }

    /**
     * Creates a new queue in the service namespace with the given name.
     * See {@link QueueDescription} for default values of queue properties.
     * @param queueDescription - A {@link QueueDescription} object describing the attributes with which the new queue will be created.
     * @return {@link QueueDescription} of the newly created queue.
     */
    public CompletableFuture<QueueDescription> createQueueAsync(QueueDescription queueDescription) {
        return putQueueAsync(queueDescription, false);
    }

    /**
     * Updates an existing queue.
     * @param queueDescription - A {@link QueueDescription} object describing the attributes with which the queue will be updated.
     * @return {@link QueueDescription} of the updated queue.
     * @throws IllegalArgumentException - descriptor is null.
     */
    public CompletableFuture<QueueDescription> updateQueueAsync(QueueDescription queueDescription) {
        return putQueueAsync(queueDescription, true);
    }

    private CompletableFuture<QueueDescription> putQueueAsync(QueueDescription queueDescription, boolean isUpdate) {
        if (queueDescription == null) {
            throw new IllegalArgumentException("queueDescription passed cannot be null");
        }

        QueueDescriptionSerializer.normalizeDescription(queueDescription, this.namespaceEndpointURI);
        String atomRequest = null;
        try {
            atomRequest = QueueDescriptionSerializer.serialize(queueDescription);
        } catch (ServiceBusException e) {
            final CompletableFuture<QueueDescription> exceptionFuture = new CompletableFuture<>();
            exceptionFuture.completeExceptionally(e);
            return exceptionFuture;
        }

        CompletableFuture<QueueDescription> responseFuture = new CompletableFuture<>();
        putEntityAsync(queueDescription.path, atomRequest, isUpdate, queueDescription.getForwardTo(), queueDescription.getForwardDeadLetteredMessagesTo())
                .handleAsync((content, ex) -> {
                    if (ex != null) {
                        responseFuture.completeExceptionally(ex);
                    } else {
                        try {
                            responseFuture.complete(QueueDescriptionSerializer.parseFromContent(content));
                        } catch (MessagingEntityNotFoundException e) {
                            responseFuture.completeExceptionally(e);
                        }
                    }

                    return null;
                }, MessagingFactory.INTERNAL_THREAD_POOL);

        return responseFuture;
    }

    /**
     * Creates a new topic in the service namespace with the given name.
     * See {@link TopicDescription} for default values of topic properties.
     * @param topicPath - The name of the topic relative to the service namespace base address.
     * @return {@link TopicDescription} of the newly created topic.
     * @throws IllegalArgumentException - Entity name is null, empty, too long or uses illegal characters.
     */
    public CompletableFuture<TopicDescription> createTopicAsync(String topicPath) {
        return this.createTopicAsync(new TopicDescription(topicPath));
    }

    /**
     * Creates a new topic in the service namespace with the given name.
     * See {@link TopicDescription} for default values of topic properties.
     * @param topicDescription - A {@link QueueDescription} object describing the attributes with which the new topic will be created.
     * @return {@link TopicDescription} of the newly created topic.
     */
    public CompletableFuture<TopicDescription> createTopicAsync(TopicDescription topicDescription) {
        return putTopicAsync(topicDescription, false);
    }

    /**
     * Updates an existing topic.
     * @param topicDescription - A {@link TopicDescription} object describing the attributes with which the topic will be updated.
     * @return {@link TopicDescription} of the updated topic.
     * @throws IllegalArgumentException - descriptor is null.
     */
    public CompletableFuture<TopicDescription> updateTopicAsync(TopicDescription topicDescription) {
        return putTopicAsync(topicDescription, true);
    }

    private CompletableFuture<TopicDescription> putTopicAsync(TopicDescription topicDescription, boolean isUpdate) {
        if (topicDescription == null) {
            throw new IllegalArgumentException("topicDescription passed cannot be null");
        }

        String atomRequest = null;
        try {
            atomRequest = TopicDescriptionSerializer.serialize(topicDescription);
        } catch (ServiceBusException e) {
            final CompletableFuture<TopicDescription> exceptionFuture = new CompletableFuture<>();
            exceptionFuture.completeExceptionally(e);
            return exceptionFuture;
        }

        CompletableFuture<TopicDescription> responseFuture = new CompletableFuture<>();
        putEntityAsync(topicDescription.path, atomRequest, isUpdate, null, null)
                .handleAsync((content, ex) -> {
                    if (ex != null) {
                        responseFuture.completeExceptionally(ex);
                    } else {
                        try {
                            responseFuture.complete(TopicDescriptionSerializer.parseFromContent(content));
                        } catch (MessagingEntityNotFoundException e) {
                            responseFuture.completeExceptionally(e);
                        }
                    }

                    return null;
                }, MessagingFactory.INTERNAL_THREAD_POOL);

        return responseFuture;
    }

    /**
     * Creates a new subscription for a given topic in the service namespace with the given name.
     * See {@link SubscriptionDescription} for default values of subscription properties.
     * @param topicPath - The name of the topic relative to the service namespace base address.
     * @param subscriptionName - The name of the subscription.
     * @return {@link SubscriptionDescription} of the newly created subscription.
     * @throws IllegalArgumentException - Entity name is null, empty, too long or uses illegal characters.
     */
    public CompletableFuture<SubscriptionDescription> createSubscriptionAsync(String topicPath, String subscriptionName) {
        return this.createSubscriptionAsync(new SubscriptionDescription(topicPath, subscriptionName));
    }

    /**
     * Creates a new subscription in the service namespace with the given name.
     * See {@link SubscriptionDescription} for default values of subscription properties.
     * @param subscriptionDescription - A {@link SubscriptionDescription} object describing the attributes with which the new subscription will be created.
     * @return {@link SubscriptionDescription} of the newly created subscription.
     */
    public CompletableFuture<SubscriptionDescription> createSubscriptionAsync(SubscriptionDescription subscriptionDescription) {
        return this.createSubscriptionAsync(subscriptionDescription, null);
    }

    /**
     * Creates a new subscription in the service namespace with the provided default rule.
     * See {@link SubscriptionDescription} for default values of subscription properties.
     * @param subscriptionDescription - A {@link SubscriptionDescription} object describing the attributes with which the new subscription will be created.
     * @param defaultRule - A {@link RuleDescription} object describing the default rule. If null, then pass-through filter will be created.
     * @return {@link SubscriptionDescription} of the newly created subscription.
     */
    public CompletableFuture<SubscriptionDescription> createSubscriptionAsync(SubscriptionDescription subscriptionDescription, RuleDescription defaultRule) {
        subscriptionDescription.defaultRule = defaultRule;
        return putSubscriptionAsync(subscriptionDescription, false);
    }

    /**
     * Updates an existing subscription.
     * @param subscriptionDescription - A {@link SubscriptionDescription} object describing the attributes with which the subscription will be updated.
     * @return {@link SubscriptionDescription} of the updated subscription.
     * @throws IllegalArgumentException - descriptor is null.
     */
    public CompletableFuture<SubscriptionDescription> updateSubscriptionAsync(SubscriptionDescription subscriptionDescription) {
        return putSubscriptionAsync(subscriptionDescription, true);
    }

    private CompletableFuture<SubscriptionDescription> putSubscriptionAsync(SubscriptionDescription subscriptionDescription, boolean isUpdate) {
        if (subscriptionDescription == null) {
            throw new IllegalArgumentException("queueDescription passed cannot be null");
        }

        SubscriptionDescriptionSerializer.normalizeDescription(subscriptionDescription, this.namespaceEndpointURI);
        String atomRequest = null;
        try {
            atomRequest = SubscriptionDescriptionSerializer.serialize(subscriptionDescription);
        } catch (ServiceBusException e) {
            final CompletableFuture<SubscriptionDescription> exceptionFuture = new CompletableFuture<>();
            exceptionFuture.completeExceptionally(e);
            return exceptionFuture;
        }

        CompletableFuture<SubscriptionDescription> responseFuture = new CompletableFuture<>();
        String path = EntityNameHelper.formatSubscriptionPath(subscriptionDescription.getTopicPath(), subscriptionDescription.getSubscriptionName());
        putEntityAsync(path, atomRequest, isUpdate, subscriptionDescription.getForwardTo(), subscriptionDescription.getForwardDeadLetteredMessagesTo())
                .handleAsync((content, ex) -> {
                    if (ex != null) {
                        responseFuture.completeExceptionally(ex);
                    } else {
                        try {
                            responseFuture.complete(SubscriptionDescriptionSerializer.parseFromContent(subscriptionDescription.getTopicPath(), content));
                        } catch (MessagingEntityNotFoundException e) {
                            responseFuture.completeExceptionally(e);
                        }
                    }

                    return null;
                }, MessagingFactory.INTERNAL_THREAD_POOL);

        return responseFuture;
    }

    /**
     * Creates a new rule for a given topic - subscription.
     * See {@link RuleDescription} for default values of subscription properties.
     * @param topicName - Name of the topic.
     * @param subscriptionName - Name of the subscription.
     * @param ruleDescription - A {@link RuleDescription} object describing the attributes with which the new rule will be created.
     * @return {@link RuleDescription} of the newly created rule.
     */
    public CompletableFuture<RuleDescription> createRuleAsync(String topicName, String subscriptionName, RuleDescription ruleDescription) {
        return putRuleAsync(topicName, subscriptionName, ruleDescription, false);
    }

    /**
     * Updates an existing rule.
     * @param topicName - Name of the topic.
     * @param subscriptionName - Name of the subscription.
     * @param ruleDescription - A {@link RuleDescription} object describing the attributes with which the rule will be updated.
     * @return {@link RuleDescription} of the updated rule.
     * @throws IllegalArgumentException - descriptor is null.
     */
    public CompletableFuture<RuleDescription> updateRuleAsync(String topicName, String subscriptionName, RuleDescription ruleDescription) {
        return putRuleAsync(topicName, subscriptionName, ruleDescription, true);
    }

    private CompletableFuture<RuleDescription> putRuleAsync(String topicName, String subscriptionName, RuleDescription ruleDescription, boolean isUpdate) {
        EntityNameHelper.checkValidTopicName(topicName);
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);
        if (ruleDescription == null) {
            throw new IllegalArgumentException("queueDescription passed cannot be null");
        }

        String atomRequest = null;
        try {
            atomRequest = RuleDescriptionSerializer.serialize(ruleDescription);
        } catch (ServiceBusException e) {
            final CompletableFuture<RuleDescription> exceptionFuture = new CompletableFuture<>();
            exceptionFuture.completeExceptionally(e);
            return exceptionFuture;
        }

        CompletableFuture<RuleDescription> responseFuture = new CompletableFuture<>();
        String path = EntityNameHelper.formatRulePath(topicName, subscriptionName, ruleDescription.getName());
        putEntityAsync(path, atomRequest, isUpdate, null, null)
                .handleAsync((content, ex) -> {
                    if (ex != null) {
                        responseFuture.completeExceptionally(ex);
                    } else {
                        try {
                            responseFuture.complete(RuleDescriptionSerializer.parseFromContent(content));
                        } catch (MessagingEntityNotFoundException e) {
                            responseFuture.completeExceptionally(e);
                        }
                    }

                    return null;
                }, MessagingFactory.INTERNAL_THREAD_POOL);

        return responseFuture;
    }

    private CompletableFuture<String> putEntityAsync(String path, String requestBody, boolean isUpdate, String forwardTo, String fwdDeadLetterTo) {
        URL entityURL = null;
        try {
            entityURL = getManagementURL(this.namespaceEndpointURI, path, API_VERSION_QUERY);
        } catch (ServiceBusException e) {
            final CompletableFuture<String> exceptionFuture = new CompletableFuture<>();
            exceptionFuture.completeExceptionally(e);
            return exceptionFuture;
        }

        HashMap<String, String> additionalHeaders = new HashMap<>();
        if (isUpdate) {
            additionalHeaders.put("If-Match", "*");
        }

        if (forwardTo != null && !forwardTo.isEmpty()) {
            try {
                String securityToken = getSecurityToken(this.clientSettings.getTokenProvider(), forwardTo);
                additionalHeaders.put(ManagementClientConstants.ServiceBusSupplementartyAuthorizationHeaderName, securityToken);
            } catch (InterruptedException | ExecutionException e) {
                final CompletableFuture<String> exceptionFuture = new CompletableFuture<>();
                exceptionFuture.completeExceptionally(e);
                return exceptionFuture;
            }
        }

        if (fwdDeadLetterTo != null && !fwdDeadLetterTo.isEmpty()) {
            try {
                String securityToken = getSecurityToken(this.clientSettings.getTokenProvider(), fwdDeadLetterTo);
                additionalHeaders.put(ManagementClientConstants.ServiceBusDlqSupplementaryAuthorizationHeaderName, securityToken);
            } catch (InterruptedException | ExecutionException e) {
                final CompletableFuture<String> exceptionFuture = new CompletableFuture<>();
                exceptionFuture.completeExceptionally(e);
                return exceptionFuture;
            }
        }

        return sendManagementHttpRequestAsync(HttpConstants.Methods.PUT, entityURL, requestBody, additionalHeaders);
    }

    /**
     * Checks whether a given queue exists or not.
     * @param path - Path of the entity to check
     * @return - True if the entity exists. False otherwise.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     */
    public CompletableFuture<Boolean> queueExistsAsync(String path) {
        EntityNameHelper.checkValidQueueName(path);

        CompletableFuture<Boolean> existsFuture = new CompletableFuture<>();
        this.getQueueAsync(path).handleAsync((qd, ex) -> {
            if (ex != null) {
                if (ex instanceof MessagingEntityNotFoundException) {
                    existsFuture.complete(Boolean.FALSE);
                    return false;
                }

                existsFuture.completeExceptionally(ex);
                return false;
            }

            existsFuture.complete(Boolean.TRUE);
            return true;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return existsFuture;
    }

    /**
     * Checks whether a given topic exists or not.
     * @param path - Path of the entity to check
     * @return - True if the entity exists. False otherwise.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     */
    public CompletableFuture<Boolean> topicExistsAsync(String path) {
        EntityNameHelper.checkValidTopicName(path);

        CompletableFuture<Boolean> existsFuture = new CompletableFuture<>();
        this.getTopicAsync(path).handleAsync((qd, ex) -> {
            if (ex != null) {
                if (ex instanceof MessagingEntityNotFoundException) {
                    existsFuture.complete(Boolean.FALSE);
                    return false;
                }

                existsFuture.completeExceptionally(ex);
                return false;
            }

            existsFuture.complete(Boolean.TRUE);
            return true;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return existsFuture;
    }

    /**
     * Checks whether a given subscription exists or not.
     * @param topicPath - Path of the topic
     * @param subscriptionName - Name of the subscription.
     * @return - True if the entity exists. False otherwise.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     */
    public CompletableFuture<Boolean> subscriptionExistsAsync(String topicPath, String subscriptionName) {
        EntityNameHelper.checkValidTopicName(topicPath);
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);

        CompletableFuture<Boolean> existsFuture = new CompletableFuture<>();
        this.getSubscriptionAsync(topicPath, subscriptionName).handleAsync((qd, ex) -> {
            if (ex != null) {
                if (ex instanceof MessagingEntityNotFoundException) {
                    existsFuture.complete(Boolean.FALSE);
                    return false;
                }

                existsFuture.completeExceptionally(ex);
                return false;
            }

            existsFuture.complete(Boolean.TRUE);
            return true;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return existsFuture;
    }

    /**
     * Checks whether a given rule exists or not for a given subscription.
     * @param topicPath - Path of the topic
     * @param subscriptionName - Name of the subscription.
     * @param ruleName - Name of the rule
     * @return - True if the entity exists. False otherwise.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     */
    public CompletableFuture<Boolean> ruleExistsAsync(String topicPath, String subscriptionName, String ruleName) {
        EntityNameHelper.checkValidTopicName(topicPath);
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);
        EntityNameHelper.checkValidRuleName(ruleName);

        CompletableFuture<Boolean> existsFuture = new CompletableFuture<>();
        this.getRuleAsync(topicPath, subscriptionName, ruleName).handleAsync((qd, ex) -> {
            if (ex != null) {
                if (ex instanceof MessagingEntityNotFoundException) {
                    existsFuture.complete(Boolean.FALSE);
                    return false;
                }

                existsFuture.completeExceptionally(ex);
                return false;
            }

            existsFuture.complete(Boolean.TRUE);
            return true;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return existsFuture;
    }

    /**
     * Deletes the queue described by the path relative to the service namespace base address.
     * @param path - The name of the entity relative to the service namespace base address.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     */
    public CompletableFuture<Void> deleteQueueAsync(String path) {
        EntityNameHelper.checkValidQueueName(path);
        return deleteEntityAsync(path);
    }

    /**
     * Deletes the topic described by the path relative to the service namespace base address.
     * @param path - The name of the entity relative to the service namespace base address.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     */
    public CompletableFuture<Void> deleteTopicAsync(String path) {
        EntityNameHelper.checkValidTopicName(path);
        return deleteEntityAsync(path);
    }

    /**
     * Deletes the subscription described by the topicPath and the subscriptionName.
     * @param topicPath - The name of the topic.
     * @param subscriptionName - The name of the subscription.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     */
    public CompletableFuture<Void> deleteSubscriptionAsync(String topicPath, String subscriptionName) {
        EntityNameHelper.checkValidTopicName(topicPath);
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);
        String path = EntityNameHelper.formatSubscriptionPath(topicPath, subscriptionName);
        return deleteEntityAsync(path);
    }

    /**
     * Deletes the rule for a given topic-subscription.
     * @param topicPath - The name of the topic.
     * @param subscriptionName - The name of the subscription.
     * @param ruleName - The name of the rule.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     */
    public CompletableFuture<Void> deleteRuleAsync(String topicPath, String subscriptionName, String ruleName) {
        EntityNameHelper.checkValidTopicName(topicPath);
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);
        EntityNameHelper.checkValidRuleName(ruleName);
        String path = EntityNameHelper.formatRulePath(topicPath, subscriptionName, ruleName);
        return deleteEntityAsync(path);
    }

    private CompletableFuture<Void> deleteEntityAsync(String path) {
        URL entityURL = null;
        try {
            entityURL = getManagementURL(this.namespaceEndpointURI, path, API_VERSION_QUERY);
        } catch (ServiceBusException e) {
            final CompletableFuture<Void> exceptionFuture = new CompletableFuture<>();
            exceptionFuture.completeExceptionally(e);
            return exceptionFuture;
        }

        return sendManagementHttpRequestAsync(HttpConstants.Methods.DELETE, entityURL, null, null).thenAccept(c -> {});
    }

    /**
     * Disposes and closes the managementClient.
     * @throws IOException
     */
    public void close() throws IOException {
        this.asyncHttpClient.close();
    }

    private static URL getManagementURL(URI namespaceEndpontURI, String entityPath, String query) throws ServiceBusException {
        try {
            URI httpURI = new URI("https", null, namespaceEndpontURI.getHost(), getPortNumberFromHost(namespaceEndpontURI.getHost()), "/"+entityPath, query, null);
            return httpURI.toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new ServiceBusException(false, e);
        }
    }

    private CompletableFuture<String> sendManagementHttpRequestAsync(String httpMethod, URL url, String atomEntryString, HashMap<String, String> additionalHeaders) {
        String securityToken = null;
        try {
            securityToken = getSecurityToken(this.clientSettings.getTokenProvider(), url.toString());
        } catch (InterruptedException | ExecutionException e) {
            final CompletableFuture<String> exceptionFuture = new CompletableFuture<>();
            exceptionFuture.completeExceptionally(e);
            return exceptionFuture;
        }

        RequestBuilder requestBuilder = new RequestBuilder(httpMethod)
                .setUrl(url.toString())
                .setBody(atomEntryString)
                .addHeader(USER_AGENT_HEADER_NAME, USER_AGENT)
                .addHeader(AUTHORIZATION_HEADER_NAME, securityToken)
                .addHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE);

        if (additionalHeaders != null) {
            for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request unboundRequest = requestBuilder.build();

        ListenableFuture<Response> listenableFuture = this.asyncHttpClient
                .executeRequest(unboundRequest);

        CompletableFuture<String> outputFuture = new CompletableFuture<>();
        listenableFuture.toCompletableFuture()
                .handleAsync((response, ex) ->
                {
                    if (ex != null) {
                        outputFuture.completeExceptionally(ex);
                    } else {
                        try {
                            validateHttpResponse(unboundRequest, response);
                            outputFuture.complete(response.getResponseBody());
                        } catch (ServiceBusException e) {
                            outputFuture.completeExceptionally(e);
                        }
                    }
                    return null;
                }, MessagingFactory.INTERNAL_THREAD_POOL);

        return outputFuture;
    }

    private static void validateHttpResponse(Request request, Response response) throws ServiceBusException, UnsupportedOperationException {
        if (response.hasResponseStatus() && response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            return;
        }

        String exceptionMessage = response.getResponseBody();
        exceptionMessage = parseDetailIfAvailable(exceptionMessage);
        if (exceptionMessage == null) {
            exceptionMessage = response.getStatusText();
        }

        ServiceBusException exception = null;
        switch (response.getStatusCode())
        {
            case 401:   /*UnAuthorized*/
                exception = new AuthorizationFailedException(exceptionMessage);
                break;

            case 404: /*NotFound*/
            case 204: /*NoContent*/
                exception = new MessagingEntityNotFoundException(exceptionMessage);
                break;

            case 409: /*Conflict*/
                if (request.getMethod() == HttpConstants.Methods.DELETE) {
                    exception = new ServiceBusException(true, exceptionMessage);
                    break;
                }

                if (request.getMethod() == HttpConstants.Methods.PUT && request.getHeaders().contains("IfMatch")) {
                    /*Update request*/
                    exception = new ServiceBusException(true, exceptionMessage);
                    break;
                }

                if (exceptionMessage.contains(ManagementClientConstants.ConflictOperationInProgressSubCode)) {
                    exception = new ServiceBusException(true, exceptionMessage);
                    break;
                }

                exception = new MessagingEntityAlreadyExistsException(exceptionMessage);
                break;

            case 403: /*Forbidden*/
                if (exceptionMessage.contains(ManagementClientConstants.ForbiddenInvalidOperationSubCode)) {
                    //todo: log
                    throw new UnsupportedOperationException(exceptionMessage);
                }
                else {
                    exception = new QuotaExceededException(exceptionMessage);
                }
                break;

            case 400: /*BadRequest*/
                exception = new ServiceBusException(false, new IllegalArgumentException(exceptionMessage));
                break;

            case 503: /*ServiceUnavailable*/
                exception = new ServerBusyException(exceptionMessage);
                break;

            default:
                exception = new ServiceBusException(true, exceptionMessage + "; Status code: " + response.getStatusCode());
        }

        //todo: log
        throw exception;
    }

    private static String parseDetailIfAvailable(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(content.getBytes("utf-8")));
            Element doc = dom.getDocumentElement();
            doc.normalize();
            NodeList entries = doc.getChildNodes();
            for (int i = 0; i < entries.getLength(); i++) {
                Node node = entries.item(i);
                if (node.getNodeName().equals("Detail")) {
                    return node.getFirstChild().getTextContent();
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            if (TRACE_LOGGER.isErrorEnabled()) {
                TRACE_LOGGER.error("Exception while parsing response.", e);
            }

            if (TRACE_LOGGER.isDebugEnabled()) {
                TRACE_LOGGER.debug("XML which failed to parse: \n %s", content);
            }
        }

        return null;
    }

    private static String getSecurityToken(TokenProvider tokenProvider, String url ) throws InterruptedException, ExecutionException {
        SecurityToken token = tokenProvider.getSecurityTokenAsync(url).get();
        return token.getTokenValue();
    }
    
    private static int getPortNumberFromHost(String host) {
        if(host.endsWith("onebox.windows-int.net"))
        {
            return ONE_BOX_HTTPS_PORT;
        }
        else
        {
            return -1;
        }
    }
}
