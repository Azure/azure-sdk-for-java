package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.Utils;
import com.microsoft.azure.servicebus.primitives.*;
import com.microsoft.azure.servicebus.rules.RuleDescription;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Synchronous client to perform management operations on Service Bus entities.
 * Use {@link ManagementClientAsync} for asynchronous operations.
 */
public class ManagementClient {
    private ManagementClientAsync asyncClient;

    public ManagementClient(ConnectionStringBuilder connectionStringBuilder) {
        this(connectionStringBuilder.getEndpoint(), Util.getClientSettingsFromConnectionStringBuilder(connectionStringBuilder));
    }

    public ManagementClient(URI namespaceEndpointURI, ClientSettings clientSettings) {
        this.asyncClient = new ManagementClientAsync(namespaceEndpointURI, clientSettings);
    }

    /**
     * Retrieves information related to the namespace.
     * Works with any claim (Send/Listen/Manage).
     * @return - {@link NamespaceInfo} containing namespace information.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public NamespaceInfo getNamespaceInfo() throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getNamespaceInfoAsync());
    }

    /**
     * Retrieves a queue from the service namespace
     * @param path - The path of the queue relative to service bus namespace.
     * @return - QueueDescription containing information about the queue.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws MessagingEntityNotFoundException - Entity with this name doesn't exist.
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public QueueDescription getQueue(String path) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getQueueAsync(path));
    }

    /**
     * Retrieves the runtime information of a queue.
     * @param path - The path of the queue relative to service bus namespace.
     * @return - QueueRuntimeInfo containing runtime information about the queue.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws MessagingEntityNotFoundException - Entity with this name doesn't exist.
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public QueueRuntimeInfo getQueueRuntimeInfo(String path) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getQueueRuntimeInfoAsync(path));
    }

    /**
     * Retrieves a topic from the service namespace
     * @param path - The path of the queue relative to service bus namespace.
     * @return - Description containing information about the topic.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws MessagingEntityNotFoundException - Entity with this name doesn't exist.
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public TopicDescription getTopic(String path) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getTopicAsync(path));
    }

    /**
     * Retrieves the runtime information of a topic
     * @param path - The path of the queue relative to service bus namespace.
     * @return - TopicRuntimeInfo containing runtime information about the topic.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws MessagingEntityNotFoundException - Entity with this name doesn't exist.
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public TopicRuntimeInfo getTopicRuntimeInfo(String path) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getTopicRuntimeInfoAsync(path));
    }

    /**
     * Retrieves a subscription for a given topic from the service namespace
     * @param topicPath - The path of the topic relative to service bus namespace.
     * @param subscriptionName - The name of the subscription
     * @return - SubscriptionDescription containing information about the subscription.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws MessagingEntityNotFoundException - Entity with this name doesn't exist.
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public SubscriptionDescription getSubscription(String topicPath, String subscriptionName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getSubscriptionAsync(topicPath, subscriptionName));
    }

    /**
     * Retrieves the runtime information of a subscription in a given topic
     * @param topicPath - The path of the topic relative to service bus namespace.
     * @param subscriptionName - The name of the subscription
     * @return - SubscriptionRuntimeInfo containing the runtime information about the subscription.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws MessagingEntityNotFoundException - Entity with this name doesn't exist.
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public SubscriptionRuntimeInfo getSubscriptionRuntimeInfo(String topicPath, String subscriptionName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getSubscriptionRuntimeInfoAsync(topicPath, subscriptionName));
    }

    /**
     * Retrieves a rule for a given topic and subscription from the service namespace
     * @param topicPath - The path of the topic relative to service bus namespace.
     * @param subscriptionName - The name of the subscription.
     * @param ruleName - The name of the rule.
     * @return - RuleDescription containing information about the subscription.
     * @throws IllegalArgumentException - Thrown if path is null, empty, or not in right format or length.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws MessagingEntityNotFoundException - Entity with this name doesn't exist.
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public RuleDescription getRule(String topicPath, String subscriptionName, String ruleName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getRuleAsync(topicPath, subscriptionName, ruleName));
    }

    /**
     * Retrieves the list of queues present in the namespace.
     * @return the first 100 queues.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public List<QueueDescription> getQueues() throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getQueuesAsync());
    }

    /**
     * Retrieves the list of queues present in the namespace.
     * You can simulate pages of list of entities by manipulating count and skip parameters.
     * skip(0)+count(100) gives first 100 entities. skip(100)+count(100) gives the next 100 entities.
     * @return the list of queues.
     * @param count - The number of queues to fetch. Defaults to 100. Maximum value allowed is 100.
     * @param skip - The number of queues to skip. Defaults to 0. Cannot be negative.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public List<QueueDescription> getQueues(int count, int skip) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getQueuesAsync(count, skip));
    }

    /**
     * Retrieves the list of topics present in the namespace.
     * @return the first 100 topics.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public List<TopicDescription> getTopics() throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getTopicsAsync());
    }

    /**
     * Retrieves the list of topics present in the namespace.
     * You can simulate pages of list of entities by manipulating count and skip parameters.
     * skip(0)+count(100) gives first 100 entities. skip(100)+count(100) gives the next 100 entities.
     * @return the list of topics.
     * @param count - The number of topics to fetch. Defaults to 100. Maximum value allowed is 100.
     * @param skip - The number of topics to skip. Defaults to 0. Cannot be negative.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public List<TopicDescription> getTopics(int count, int skip) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getTopicsAsync(count, skip));
    }

    /**
     * Retrieves the list of subscriptions for a given topic in the namespace.
     * @param topicName - The name of the topic.
     * @return the first 100 subscriptions.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public List<SubscriptionDescription> getSubscriptions(String topicName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getSubscriptionsAsync(topicName));
    }

    /**
     * Retrieves the list of subscriptions for a given topic in the namespace.
     * You can simulate pages of list of entities by manipulating count and skip parameters.
     * skip(0)+count(100) gives first 100 entities. skip(100)+count(100) gives the next 100 entities.
     * @return the list of subscriptions.
     * @param topicName - The name of the topic.
     * @param count - The number of subscriptions to fetch. Defaults to 100. Maximum value allowed is 100.
     * @param skip - The number of subscriptions to skip. Defaults to 0. Cannot be negative.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public List<SubscriptionDescription> getSubscriptions(String topicName, int count, int skip) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getSubscriptionsAsync(topicName, count, skip));
    }

    /**
     * Retrieves the list of rules for a given topic-subscription in the namespace.
     * @param topicName - The name of the topic.
     * @param subscriptionName - The name of the subscription.
     * @return the first 100 rules.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public List<RuleDescription> getRules(String topicName, String subscriptionName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getRulesAsync(topicName, subscriptionName));
    }

    /**
     * Retrieves the list of rules for a given topic-subscription in the namespace.
     * You can simulate pages of list of entities by manipulating count and skip parameters.
     * skip(0)+count(100) gives first 100 entities. skip(100)+count(100) gives the next 100 entities.
     * @return the list of rules.
     * @param topicName - The name of the topic.
     * @param subscriptionName - The name of the subscription.
     * @param count - The number of rules to fetch. Defaults to 100. Maximum value allowed is 100.
     * @param skip - The number of rules to skip. Defaults to 0. Cannot be negative.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public List<RuleDescription> getRules(String topicName, String subscriptionName, int count, int skip) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.getRulesAsync(topicName, subscriptionName, count, skip));
    }

    /**
     * Creates a new queue in the service namespace with the given name.
     * See {@link QueueDescription} for default values of queue properties.
     * @param queuePath - The name of the queue relative to the service namespace base address.
     * @return {@link QueueDescription} of the newly created queue.
     * @throws IllegalArgumentException - Entity name is null, empty, too long or uses illegal characters.
     * @throws MessagingEntityAlreadyExistsException - An entity with the same name exists under the same service namespace.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public QueueDescription createQueue(String queuePath) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.createQueueAsync(queuePath));
    }

    /**
     * Creates a new queue in the service namespace with the given name.
     * See {@link QueueDescription} for default values of queue properties.
     * @param queueDescription - A {@link QueueDescription} object describing the attributes with which the new queue will be created.
     * @return {@link QueueDescription} of the newly created queue.
     * @throws MessagingEntityAlreadyExistsException - An entity with the same name exists under the same service namespace.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public QueueDescription createQueue(QueueDescription queueDescription) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.createQueueAsync(queueDescription));
    }

    /**
     * Updates an existing queue.
     * @param queueDescription - A {@link QueueDescription} object describing the attributes with which the queue will be updated.
     * @return {@link QueueDescription} of the updated queue.
     * @throws MessagingEntityNotFoundException - Described entity was not found.
     * @throws IllegalArgumentException - descriptor is null.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public QueueDescription updateQueue(QueueDescription queueDescription) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.updateQueueAsync(queueDescription));
    }

    /**
     * Creates a new topic in the service namespace with the given name.
     * See {@link TopicDescription} for default values of topic properties.
     * @param topicPath - The name of the topic relative to the service namespace base address.
     * @return {@link TopicDescription} of the newly created topic.
     * @throws IllegalArgumentException - Entity name is null, empty, too long or uses illegal characters.
     * @throws MessagingEntityAlreadyExistsException - An entity with the same name exists under the same service namespace.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public TopicDescription createTopic(String topicPath) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.createTopicAsync(topicPath));
    }

    /**
     * Creates a new topic in the service namespace with the given name.
     * See {@link TopicDescription} for default values of topic properties.
     * @param topicDescription - A {@link QueueDescription} object describing the attributes with which the new topic will be created.
     * @return {@link TopicDescription} of the newly created topic.
     * @throws MessagingEntityAlreadyExistsException - An entity with the same name exists under the same service namespace.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public TopicDescription createTopic(TopicDescription topicDescription) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.createTopicAsync(topicDescription));
    }

    /**
     * Updates an existing topic.
     * @param topicDescription - A {@link TopicDescription} object describing the attributes with which the topic will be updated.
     * @return {@link TopicDescription} of the updated topic.
     * @throws MessagingEntityNotFoundException - Described entity was not found.
     * @throws IllegalArgumentException - descriptor is null.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public TopicDescription updateTopic(TopicDescription topicDescription) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.updateTopicAsync(topicDescription));
    }

    /**
     * Creates a new subscription for a given topic in the service namespace with the given name.
     * See {@link SubscriptionDescription} for default values of subscription properties.
     * @param topicPath - The name of the topic relative to the service namespace base address.
     * @param subscriptionName - The name of the subscription.
     * @return {@link SubscriptionDescription} of the newly created subscription.
     * @throws IllegalArgumentException - Entity name is null, empty, too long or uses illegal characters.
     * @throws MessagingEntityAlreadyExistsException - An entity with the same name exists under the same service namespace.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public SubscriptionDescription createSubscription(String topicPath, String subscriptionName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.createSubscriptionAsync(topicPath, subscriptionName));
    }

    /**
     * Creates a new subscription in the service namespace with the given name.
     * See {@link SubscriptionDescription} for default values of subscription properties.
     * @param subscriptionDescription - A {@link SubscriptionDescription} object describing the attributes with which the new subscription will be created.
     * @return {@link SubscriptionDescription} of the newly created subscription.
     * @throws MessagingEntityAlreadyExistsException - An entity with the same name exists under the same service namespace.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public SubscriptionDescription createSubscription(SubscriptionDescription subscriptionDescription) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.createSubscriptionAsync(subscriptionDescription));
    }

    /**
     * Creates a new subscription in the service namespace with the provided default rule.
     * See {@link SubscriptionDescription} for default values of subscription properties.
     * @param subscriptionDescription - A {@link SubscriptionDescription} object describing the attributes with which the new subscription will be created.
     * @param defaultRule - A {@link RuleDescription} object describing the default rule. If null, then pass-through filter will be created.
     * @return {@link SubscriptionDescription} of the newly created subscription.
     * @throws MessagingEntityAlreadyExistsException - An entity with the same name exists under the same service namespace.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public SubscriptionDescription createSubscription(SubscriptionDescription subscriptionDescription, RuleDescription defaultRule) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.createSubscriptionAsync(subscriptionDescription, defaultRule));
    }

    /**
     * Updates an existing subscription.
     * @param subscriptionDescription - A {@link SubscriptionDescription} object describing the attributes with which the subscription will be updated.
     * @return {@link SubscriptionDescription} of the updated subscription.
     * @throws MessagingEntityNotFoundException - Described entity was not found.
     * @throws IllegalArgumentException - descriptor is null.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public SubscriptionDescription updateSubscription(SubscriptionDescription subscriptionDescription) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.updateSubscriptionAsync(subscriptionDescription));
    }

    /**
     * Creates a new rule for a given topic - subscription.
     * See {@link RuleDescription} for default values of subscription properties.
     * @param topicName - Name of the topic.
     * @param subscriptionName - Name of the subscription.
     * @param ruleDescription - A {@link RuleDescription} object describing the attributes with which the new rule will be created.
     * @return {@link RuleDescription} of the newly created rule.
     * @throws MessagingEntityAlreadyExistsException - An entity with the same name exists under the same service namespace.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public RuleDescription createRule(String topicName, String subscriptionName, RuleDescription ruleDescription) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.createRuleAsync(topicName, subscriptionName, ruleDescription));
    }

    /**
     * Updates an existing rule.
     * @param topicName - Name of the topic.
     * @param subscriptionName - Name of the subscription.
     * @param ruleDescription - A {@link RuleDescription} object describing the attributes with which the rule will be updated.
     * @return {@link RuleDescription} of the updated rule.
     * @throws MessagingEntityNotFoundException - Described entity was not found.
     * @throws IllegalArgumentException - descriptor is null.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws QuotaExceededException - Either the specified size in the description is not supported or the maximum allowed quota has been reached.
     * @throws InterruptedException if the current thread was interrupted
     */
    public RuleDescription updateRule(String topicName, String subscriptionName, RuleDescription ruleDescription) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.updateRuleAsync(topicName, subscriptionName, ruleDescription));
    }

    /**
     * Checks whether a given queue exists or not.
     * @param path - Path of the entity to check
     * @return - True if the entity exists. False otherwise.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public Boolean queueExists(String path) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.queueExistsAsync(path));
    }

    /**
     * Checks whether a given topic exists or not.
     * @param path - Path of the entity to check
     * @return - True if the entity exists. False otherwise.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public Boolean topicExists(String path) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.topicExistsAsync(path));
    }

    /**
     * Checks whether a given subscription exists or not.
     * @param topicPath - Path of the topic
     * @param subscriptionName - Name of the subscription.
     * @return - True if the entity exists. False otherwise.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public Boolean subscriptionExists(String topicPath, String subscriptionName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.subscriptionExistsAsync(topicPath, subscriptionName));
    }

    /**
     * Checks whether a given rule exists or not for a given subscription.
     * @param topicPath - Path of the topic
     * @param subscriptionName - Name of the subscription.
     * @param ruleName - Name of the rule
     * @return - True if the entity exists. False otherwise.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws InterruptedException if the current thread was interrupted
     */
    public Boolean ruleExists(String topicPath, String subscriptionName, String ruleName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.ruleExistsAsync(topicPath, subscriptionName, ruleName));
    }

    /**
     * Deletes the queue described by the path relative to the service namespace base address.
     * @param path - The name of the entity relative to the service namespace base address.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws MessagingEntityNotFoundException - An entity with this name does not exist.
     * @throws InterruptedException if the current thread was interrupted
     */
    public Void deleteQueue(String path) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.deleteQueueAsync(path));
    }

    /**
     * Deletes the topic described by the path relative to the service namespace base address.
     * @param path - The name of the entity relative to the service namespace base address.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws MessagingEntityNotFoundException - An entity with this name does not exist.
     * @throws InterruptedException if the current thread was interrupted
     */
    public Void deleteTopic(String path) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.deleteTopicAsync(path));
    }

    /**
     * Deletes the subscription described by the topicPath and the subscriptionName.
     * @param topicPath - The name of the topic.
     * @param subscriptionName - The name of the subscription.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws MessagingEntityNotFoundException - An entity with this name does not exist.
     * @throws InterruptedException if the current thread was interrupted
     */
    public Void deleteSubscription(String topicPath, String subscriptionName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.deleteSubscriptionAsync(topicPath, subscriptionName));
    }

    /**
     * Deletes the rule for a given topic-subscription.
     * @param topicPath - The name of the topic.
     * @param subscriptionName - The name of the subscription.
     * @param ruleName - The name of the rule.
     * @throws IllegalArgumentException - path is not null / empty / too long / invalid.
     * @throws TimeoutException - The operation times out. The timeout period is initiated through ClientSettings.operationTimeout
     * @throws AuthorizationFailedException - No sufficient permission to perform this operation. Please check ClientSettings.tokenProvider has correct details.
     * @throws ServerBusyException - The server is busy. You should wait before you retry the operation.
     * @throws ServiceBusException - An internal error or an unexpected exception occurred.
     * @throws MessagingEntityNotFoundException - An entity with this name does not exist.
     * @throws InterruptedException if the current thread was interrupted
     */
    public Void deleteRule(String topicPath, String subscriptionName, String ruleName) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.asyncClient.deleteRuleAsync(topicPath, subscriptionName, ruleName));
    }

    /**
     * Disposes and closes the managementClient.
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        this.asyncClient.close();
    }
}
