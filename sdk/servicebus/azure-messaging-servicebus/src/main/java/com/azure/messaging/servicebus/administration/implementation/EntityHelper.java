// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.implementation.models.AuthorizationRuleImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBodyContent;
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
import com.azure.messaging.servicebus.administration.models.AuthorizationRule;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.RuleAction;
import com.azure.messaging.servicebus.administration.models.RuleFilter;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SharedAccessAuthorizationRule;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.azure.core.http.policy.AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY;

/**
 * Used to access internal methods on {@link QueueProperties}.
 */
public final class EntityHelper {
    private static final ClientLogger LOGGER = new ClientLogger(EntityHelper.class);
    public static final String CONTENT_TYPE = "application/xml";
    // Name of the entity type when listing queues and topics.
    public static final String QUEUES_ENTITY_TYPE = "queues";
    public static final String TOPICS_ENTITY_TYPE = "topics";

    public static final int NUMBER_OF_ELEMENTS = 100;
    private static QueueAccessor queueAccessor;
    private static SubscriptionAccessor subscriptionAccessor;
    private static TopicAccessor topicAccessor;
    private static RuleAccessor ruleAccessor;

    static {
        try {
            Class.forName(QueueProperties.class.getName(), true, QueueProperties.class.getClassLoader());
            Class.forName(SubscriptionProperties.class.getName(), true,
                SubscriptionProperties.class.getClassLoader());
            Class.forName(TopicProperties.class.getName(), true, TopicProperties.class.getClassLoader());
            Class.forName(RuleProperties.class.getName(), true, RuleProperties.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    /**
     * Gets a queue description given the options.
     *
     * @param options The options.
     *
     * @return The corresponding queue.
     */
    public static QueueDescription getQueueDescription(CreateQueueOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        final QueueDescription description = new QueueDescription()
            .setAutoDeleteOnIdle(options.getAutoDeleteOnIdle())
            .setDefaultMessageTimeToLive(options.getDefaultMessageTimeToLive())
            .setDeadLetteringOnMessageExpiration(options.isDeadLetteringOnMessageExpiration())
            .setDuplicateDetectionHistoryTimeWindow(options.getDuplicateDetectionHistoryTimeWindow())
            .setEnableBatchedOperations(options.isBatchedOperationsEnabled())
            .setEnablePartitioning(options.isPartitioningEnabled())
            .setForwardTo(options.getForwardTo())
            .setForwardDeadLetteredMessagesTo(options.getForwardDeadLetteredMessagesTo())
            .setLockDuration(options.getLockDuration())
            .setMaxDeliveryCount(options.getMaxDeliveryCount())
            .setMaxSizeInMegabytes(options.getMaxSizeInMegabytes())
            .setRequiresDuplicateDetection(options.isDuplicateDetectionRequired())
            .setRequiresSession(options.isSessionRequired())
            .setStatus(options.getStatus())
            .setUserMetadata(options.getUserMetadata());

        if (!options.getAuthorizationRules().isEmpty()) {
            description.setAuthorizationRules(toImplementation(options.getAuthorizationRules()));
        }

        if (options.getMaxMessageSizeInKilobytes() != 0) {
            description.setMaxMessageSizeInKilobytes(options.getMaxMessageSizeInKilobytes());
        }

        return description;
    }

    public static SubscriptionDescription getSubscriptionDescription(CreateSubscriptionOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return new SubscriptionDescription()
            .setAutoDeleteOnIdle(options.getAutoDeleteOnIdle())
            .setDefaultMessageTimeToLive(options.getDefaultMessageTimeToLive())
            .setDeadLetteringOnFilterEvaluationExceptions(options.isDeadLetteringOnFilterEvaluationExceptions())
            .setDeadLetteringOnMessageExpiration(options.isDeadLetteringOnMessageExpiration())
            .setEnableBatchedOperations(options.isBatchedOperationsEnabled())
            .setForwardTo(options.getForwardTo())
            .setForwardDeadLetteredMessagesTo(options.getForwardDeadLetteredMessagesTo())
            .setLockDuration(options.getLockDuration())
            .setMaxDeliveryCount(options.getMaxDeliveryCount())
            .setRequiresSession(options.isSessionRequired())
            .setStatus(options.getStatus())
            .setUserMetadata(options.getUserMetadata())
            .setDefaultRule(options.getDefaultRule() != null
                ? EntityHelper.toImplementation(options.getDefaultRule()) : null);
    }

    public static TopicDescription getTopicDescription(CreateTopicOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        final TopicDescription description = new TopicDescription()
            .setAutoDeleteOnIdle(options.getAutoDeleteOnIdle())
            .setDefaultMessageTimeToLive(options.getDefaultMessageTimeToLive())
            .setDuplicateDetectionHistoryTimeWindow(options.getDuplicateDetectionHistoryTimeWindow())
            .setEnableBatchedOperations(options.isBatchedOperationsEnabled())
            .setEnablePartitioning(options.isPartitioningEnabled())
            .setMaxSizeInMegabytes(options.getMaxSizeInMegabytes())
            .setRequiresDuplicateDetection(options.isDuplicateDetectionRequired())
            .setSupportOrdering(options.isSupportOrdering())
            .setStatus(options.getStatus())
            .setUserMetadata(options.getUserMetadata());

        if (!options.getAuthorizationRules().isEmpty()) {
            description.setAuthorizationRules(toImplementation(options.getAuthorizationRules()));
        }

        if (options.getMaxMessageSizeInKilobytes() != 0) {
            description.setMaxMessageSizeInKilobytes(options.getMaxMessageSizeInKilobytes());
        }

        return description;
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param properties Options to create queue with.
     *
     * @return A new {@link QueueProperties} with the set options.
     */
    public static QueueDescription toImplementation(QueueProperties properties) {
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        if (queueAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'queueAccessor' should not be null."));
        }

        final List<AuthorizationRuleImpl> rules = !properties.getAuthorizationRules().isEmpty()
            ? toImplementation(properties.getAuthorizationRules())
            : Collections.emptyList();

        return queueAccessor.toImplementation(properties, rules);
    }

    /**
     * Creates a new rule action given an existing rule action.
     *
     * @param properties Rule properties.
     * @return A new instance of {@link RuleActionImpl}.
     */
    public static RuleActionImpl toImplementation(RuleAction properties) {
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        if (ruleAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toImplementation(properties);
    }

    /**
     * Creates a new rule description given an existing rule.
     *
     * @param properties Rule properties.
     * @return A new instance of {@link RuleDescription}.
     */
    public static RuleDescription toImplementation(RuleProperties properties) {
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        if (ruleAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toImplementation(properties);
    }

    /**
     * Creates a new rule filter given an existing rule filter.
     *
     * @param properties Rule filter.
     * @return A new instance of {@link RuleFilter}.
     */
    public static RuleFilterImpl toImplementation(RuleFilter properties) {
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        if (ruleAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toImplementation(properties);
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param description Options to create queue with.
     *
     * @return A new {@link SubscriptionProperties} with the set options.
     */
    public static SubscriptionDescription toImplementation(SubscriptionProperties description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (subscriptionAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'subscriptionAccessor' should not be null."));
        }

        return subscriptionAccessor.toImplementation(description);
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param properties Options to create queue with.
     *
     * @return A new {@link TopicProperties} with the set options.
     */
    public static TopicDescription toImplementation(TopicProperties properties) {
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        if (topicAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'topicAccessor' should not be null."));
        }

        final List<AuthorizationRuleImpl> rules = !properties.getAuthorizationRules().isEmpty()
            ? toImplementation(properties.getAuthorizationRules())
            : Collections.emptyList();

        return topicAccessor.toImplementation(properties, rules);
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param description Options to create queue with.
     *
     * @return A new {@link QueueProperties} with the set options.
     */
    public static QueueProperties toModel(QueueDescription description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (queueAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'queueAccessor' should not be null."));
        }

        return queueAccessor.toModel(description);
    }

    /**
     * Gets a new rule action based on the existing rule description.
     *
     * @param description The implementation type.
     * @return A new {@link RuleAction} with the set options.
     */
    public static RuleAction toModel(RuleActionImpl description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (ruleAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toModel(description);
    }

    /**
     * Gets a new rule filter based on the existing rule description.
     *
     * @param description The implementation type.
     * @return A new {@link RuleFilter} with the set options.
     */
    public static RuleFilter toModel(RuleFilterImpl description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (ruleAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toModel(description);
    }

    /**
     * Gets a new rule based on the existing rule description.
     *
     * @param description The implementation type.
     * @return A new {@link RuleProperties} with the set options.
     */
    public static RuleProperties toModel(RuleDescription description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (ruleAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toModel(description);
    }

    /**
     * Creates a new subscription given the options.
     *
     * @param options Options to create topic with.
     *
     * @return A new {@link SubscriptionProperties} with the set options.
     */
    public static SubscriptionProperties toModel(SubscriptionDescription options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        if (subscriptionAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'subscriptionAccessor' should not be null."));
        }

        return subscriptionAccessor.toModel(options);
    }

    /**
     * Creates a new topic given the options.
     *
     * @param description Options to create topic with.
     *
     * @return A new {@link TopicProperties} with the set options.
     */
    public static TopicProperties toModel(TopicDescription description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (topicAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'topicAccessor' should not be null."));
        }

        return topicAccessor.toModel(description);
    }

    /**
     * Sets the queue accessor.
     *
     * @param accessor The queue accessor to set on the queue helper.
     */
    public static void setQueueAccessor(QueueAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (EntityHelper.queueAccessor != null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "'accessor' is already set."));
        }

        EntityHelper.queueAccessor = accessor;
    }

    /**
     * Sets the queue name on a {@link QueueProperties}.
     *
     * @param queueProperties Queue to set name on.
     * @param name Name of the queue.
     */
    public static void setQueueName(QueueProperties queueProperties, String name) {
        if (queueAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'queueAccessor' should not be null."));
        }

        queueAccessor.setName(queueProperties, name);
    }

    /**
     * Sets the rule accessor.
     *
     * @param accessor The rule accessor.
     */
    public static void setRuleAccessor(RuleAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (EntityHelper.ruleAccessor != null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "'ruleAccessor' is already set."));
        }

        EntityHelper.ruleAccessor = accessor;
    }

    /**
     * Sets the subscription accessor.
     *
     * @param accessor The subscription accessor.
     */
    public static void setSubscriptionAccessor(SubscriptionAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (EntityHelper.subscriptionAccessor != null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "'subscriptionAccessor' is already set."));
        }

        EntityHelper.subscriptionAccessor = accessor;
    }

    /**
     * Sets the subscription name on a {@link SubscriptionProperties}.
     *
     * @param subscription Subscription to set name on.
     * @param subscriptionName Name of the subscription.
     */
    public static void setSubscriptionName(SubscriptionProperties subscription, String subscriptionName) {
        if (subscriptionAccessor == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("'subscriptionAccessor' should not be null."));
        }

        subscriptionAccessor.setSubscriptionName(subscription, subscriptionName);
    }

    /**
     * Sets the queue accessor.
     *
     * @param accessor The queue accessor to set on the queue helper.
     */
    public static void setTopicAccessor(TopicAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (EntityHelper.topicAccessor != null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "'topicAccessor' is already set."));
        }

        EntityHelper.topicAccessor = accessor;
    }

    /**
     * Sets the topic name on a {@link SubscriptionProperties}.
     *
     * @param subscription Subscription to set name on.
     * @param topicName Name of the topic.
     */
    public static void setTopicName(SubscriptionProperties subscription, String topicName) {
        if (subscriptionAccessor == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "'subscriptionAccessor' should not be null."));
        }

        subscriptionAccessor.setTopicName(subscription, topicName);
    }

    /**
     * Sets the topic name on a {@link TopicProperties}.
     *
     * @param topicProperties Topic to set name on.
     * @param topicName Name of the topic.
     */
    public static void setTopicName(TopicProperties topicProperties, String topicName) {
        if (topicAccessor == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "'topicAccessor' should not be null."));
        }

        topicAccessor.setName(topicProperties, topicName);
    }

    private static List<AuthorizationRuleImpl> toImplementation(List<AuthorizationRule> rules) {
        return rules.stream().map(rule -> {
            final AuthorizationRuleImpl implementation = new AuthorizationRuleImpl()
                .setClaimType(rule.getClaimType())
                .setClaimValue(rule.getClaimValue())
                .setCreatedTime(rule.getCreatedAt())
                .setKeyName(rule.getKeyName())
                .setModifiedTime(rule.getModifiedAt())
                .setPrimaryKey(rule.getPrimaryKey())
                .setSecondaryKey(rule.getSecondaryKey())
                .setRights(rule.getAccessRights());

            if (rule instanceof SharedAccessAuthorizationRule) {
                // This is the type name constant.
                implementation.setType("SharedAccessAuthorizationRule");
            } else {
                final String className = rule.getClass().getName();
                LOGGER.warning("AuthorizationRule type '{}' is unknown.", className);
                implementation.setType(className);
            }

            return implementation;
        }).collect(Collectors.toList());
    }

    /**
     * Interface for accessing methods on a queue.
     */
    public interface QueueAccessor {
        /**
         * Creates a new queue from the given {@code queueDescription}.
         *
         * @param queueDescription Queue description to use.
         *
         * @return A new queue with the properties set.
         */
        QueueDescription toImplementation(QueueProperties queueDescription, List<AuthorizationRuleImpl> rules);

        /**
         * Creates a new queue from the given {@code queueDescription}.
         *
         * @param queueDescription Queue description to use.
         *
         * @return A new queue with the properties set.
         */
        QueueProperties toModel(QueueDescription queueDescription);

        /**
         * Sets the name on a queueDescription.
         *
         * @param queueProperties Queue to set name on.
         * @param name Name of the queue.
         */
        void setName(QueueProperties queueProperties, String name);
    }

    /**
     * Interface for accessing methods on a rule.
     */
    public interface RuleAccessor {
        RuleProperties toModel(RuleDescription ruleDescription);

        RuleAction toModel(RuleActionImpl implementation);

        RuleFilter toModel(RuleFilterImpl implementation);

        RuleDescription toImplementation(RuleProperties ruleProperties);

        RuleActionImpl toImplementation(RuleAction model);

        RuleFilterImpl toImplementation(RuleFilter model);
    }

    /**
     * Interface for accessing methods on a subscription.
     */
    public interface SubscriptionAccessor {
        /**
         * Creates a model subscription with the given implementation.
         *
         * @param subscription Options used to create subscription.
         *
         * @return A new subscription.
         */
        SubscriptionProperties toModel(SubscriptionDescription subscription);

        /**
         * Creates the implementation subscription with the given subscription.
         *
         * @param subscription Options used to create subscription.
         *
         * @return A new subscription.
         */
        SubscriptionDescription toImplementation(SubscriptionProperties subscription);

        /**
         * Sets the topic name on a subscription.
         *
         * @param subscriptionProperties Subscription to set name on.
         * @param topicName Name of the topic.
         */
        void setTopicName(SubscriptionProperties subscriptionProperties, String topicName);

        /**
         * Sets the subscription name on a subscription description.
         *
         * @param subscriptionProperties Subscription to set name on.
         * @param subscriptionName Name of the subscription.
         */
        void setSubscriptionName(SubscriptionProperties subscriptionProperties, String subscriptionName);
    }

    /**
     * Interface for accessing methods on a topic.
     */
    public interface TopicAccessor {
        /**
         * Sets properties on the TopicProperties based on the CreateTopicOptions.
         *
         * @param topic The model topic.
         *
         * @return A new topic with the properties set.
         */
        TopicDescription toImplementation(TopicProperties topic, List<AuthorizationRuleImpl> rules);

        /**
         * Sets properties on the TopicProperties based on the CreateTopicOptions.
         *
         * @param topic The implementation topic.
         *
         * @return A new topic with the properties set.
         */
        TopicProperties toModel(TopicDescription topic);

        /**
         * Sets the name on a topicDescription.
         *
         * @param topicProperties Topic to set name.
         * @param name Name of the topic.
         */
        void setName(TopicProperties topicProperties, String name);
    }

    /**
     * Check that the additional headers field is present and add the additional auth header
     *
     * @param headerName name of the header to be added
     * @param context current request context
     */
    public static void addSupplementaryAuthHeader(String headerName, String entity, Context context) {
        context.getData(AZURE_REQUEST_HTTP_HEADERS_KEY)
            .ifPresent(headers -> {
                if (headers instanceof HttpHeaders) {
                    HttpHeaders customHttpHeaders = (HttpHeaders) headers;
                    customHttpHeaders.add(headerName, entity);
                }
            });
    }

    /**
     * Create Queue Body
     *
     * @param createQueueOptions Create Queue Body options
     * @return {@link CreateQueueBody}
     */
    public static CreateQueueBody getCreateQueueBody(QueueDescription createQueueOptions) {
        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType(CONTENT_TYPE)
            .setQueueDescription(createQueueOptions);
        return new CreateQueueBody()
            .setContent(content);
    }

    public static CreateTopicBody getUpdateTopicBody(TopicProperties topic) {
        final TopicDescription implementation = EntityHelper.toImplementation(topic);
        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(implementation);
        return new CreateTopicBody()
            .setContent(content);
    }

    public static CreateTopicBody getCreateTopicBody(TopicDescription topicOptions) {
        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(topicOptions);
        return new CreateTopicBody()
            .setContent(content);
    }

    public static CreateRuleBody getUpdateRuleBody(RuleProperties rule) {
        final RuleDescription implementation = EntityHelper.toImplementation(rule);
        final CreateRuleBodyContent content = new CreateRuleBodyContent()
            .setType(CONTENT_TYPE)
            .setRuleDescription(implementation);
        return new CreateRuleBody()
            .setContent(content);
    }

    public static CreateSubscriptionBody getUpdateSubscriptionBody(SubscriptionProperties subscription) {
        final SubscriptionDescription implementation = EntityHelper.toImplementation(subscription);
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(implementation);
        return new CreateSubscriptionBody()
            .setContent(content);
    }

    public static CreateSubscriptionBody getCreateSubscriptionBody(SubscriptionDescription subscriptionDescription) {
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(subscriptionDescription);
        return new CreateSubscriptionBody().setContent(content);
    }

    public static CreateRuleBody getCreateRuleBody(String ruleName, CreateRuleOptions ruleOptions) {
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
        return new CreateRuleBody().setContent(content);
    }

    public static List<TopicProperties> getTopics(TopicDescriptionFeed feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getTopicDescription() != null)
            .map(EntityHelper::getTopicProperties)
            .collect(Collectors.toList());
    }

    public static List<QueueProperties> getQueues(QueueDescriptionFeed feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getQueueDescription() != null)
            .map(EntityHelper::getQueueProperties)
            .collect(Collectors.toList());
    }

    public static QueueProperties getQueueProperties(QueueDescriptionEntry e) {
        final String queueName = getTitleValue(e.getTitle());
        final QueueProperties queueProperties = EntityHelper.toModel(
            e.getContent().getQueueDescription());

        EntityHelper.setQueueName(queueProperties, queueName);

        return queueProperties;
    }

    public static List<RuleProperties> getRules(RuleDescriptionFeed feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getRuleDescription() != null)
            .map(e -> EntityHelper.toModel(e.getContent().getRuleDescription()))
            .collect(Collectors.toList());
    }

    public static List<SubscriptionProperties> getSubscriptions(String topicName,
                                                                SubscriptionDescriptionFeed feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getSubscriptionDescription() != null)
            .map(e -> getSubscriptionProperties(topicName, e))
            .collect(Collectors.toList());
    }

    public static SubscriptionProperties getSubscriptionProperties(String topicName,
                                                                   SubscriptionDescriptionEntry entry) {
        final SubscriptionProperties subscription = EntityHelper.toModel(
            entry.getContent().getSubscriptionDescription());
        final String subscriptionName = getTitleValue(entry.getTitle());
        EntityHelper.setSubscriptionName(subscription, subscriptionName);
        EntityHelper.setTopicName(subscription, topicName);
        return subscription;
    }

    public static TopicProperties getTopicProperties(TopicDescriptionEntry entry) {
        final TopicProperties result = EntityHelper.toModel(entry.getContent().getTopicDescription());
        final String topicName = getTitleValue(entry.getTitle());
        EntityHelper.setTopicName(result, topicName);
        return result;
    }

    public static SimpleResponse<SubscriptionProperties> getSubscriptionPropertiesSimpleResponse(String topicName,
                                                                                                 Response<Object> response,
                                                                                                 SubscriptionDescriptionEntry entry) {
        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            LOGGER.warning("entry.getContent() is null. There should have been content returned. Entry: {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }
        final SubscriptionProperties subscription = getSubscriptionProperties(topicName, entry);
        final String subscriptionName = getTitleValue(entry.getTitle());
        EntityHelper.setSubscriptionName(subscription, subscriptionName);
        EntityHelper.setTopicName(subscription, topicName);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            subscription);
    }

    public static SimpleResponse<RuleProperties> getRulePropertiesSimpleResponse(Response<Object> response,
                                                                                 RuleDescriptionEntry entry) {
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

    /**
     * Given an XML title element, returns the XML text inside. Jackson deserializes Objects as LinkedHashMaps. XML text
     * is represented as an entry with an empty string as the key.
     * <p>
     * For example, the text returned from this {@code <title text="text/xml">QueueName</title>} is "QueueName".
     *
     * @param responseTitle XML title element.
     * @return The XML text inside the title. {@code null} is returned if there is no value.
     */
    @SuppressWarnings("unchecked")
    public static String getTitleValue(Object responseTitle) {
        if (!(responseTitle instanceof Map)) {
            return null;
        }

        final Map<String, String> map;
        try {
            map = (Map<String, String>) responseTitle;
            return map.get("");
        } catch (ClassCastException error) {
            LOGGER.warning("Unable to cast to Map<String,String>. Title: {}", responseTitle, error);
            return null;
        }
    }

    public static void validateQueueName(String queueName) {
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'queueName' cannot be null or empty."));
        }
    }

    public static void validateRuleName(String ruleName) {
        if (CoreUtils.isNullOrEmpty(ruleName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'ruleName' cannot be null or empty."));
        }
    }

    public static void validateTopicName(String topicName) {
        if (CoreUtils.isNullOrEmpty(topicName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'topicName' cannot be null or empty."));
        }
    }

    public static void validateSubscriptionName(String subscriptionName) {
        if (CoreUtils.isNullOrEmpty(subscriptionName)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'subscriptionName' cannot be null or empty."));
        }
    }

    public static Context getContext(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders());
    }

    /**
     * A page of Service Bus entities.
     *
     * @param <T> The entity description from Service Bus.
     */
    public static final class FeedPage<T> implements PagedResponse<T> {
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
        public FeedPage(int statusCode, HttpHeaders header, HttpRequest request, List<T> entries) {
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
        public FeedPage(int statusCode, HttpHeaders header, HttpRequest request, List<T> entries, int skip) {
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

    public static final class EntityNotFoundHttpResponse<T> extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;

        public EntityNotFoundHttpResponse(Response<T> response) {
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

    /**
     * Creates a {@link FeedPage} given the elements and a set of response links to get the next link from.
     *
     * @param entities Entities in the feed.
     * @param responseLinks Links returned from the feed.
     * @param <TResult> Type of Service Bus entities in page.
     * @return A {@link FeedPage} indicating whether this can be continued or not.
     * @throws MalformedURLException if the "next" page link does not contain a well-formed URL.
     */
    public static <TResult, TFeed> FeedPage<TResult> extractPage(Response<TFeed> response, List<TResult> entities,
                                                                 List<ResponseLink> responseLinks)
        throws MalformedURLException, UnsupportedEncodingException {
        final Optional<ResponseLink> nextLink = responseLinks.stream()
            .filter(link -> link.getRel().equalsIgnoreCase("next"))
            .findFirst();

        if (!nextLink.isPresent()) {
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
        }

        final URL url = new URL(nextLink.get().getHref());
        final String decode = URLDecoder.decode(url.getQuery(), StandardCharsets.UTF_8.toString());
        final Optional<Integer> skipParameter = Arrays.stream(decode.split("&amp;|&"))
            .map(part -> part.split("=", 2))
            .filter(parts -> parts[0].equalsIgnoreCase("$skip") && parts.length == 2)
            .map(parts -> Integer.valueOf(parts[1]))
            .findFirst();

        if (skipParameter.isPresent()) {
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities,
                skipParameter.get());
        } else {
            LOGGER.warning("There should have been a skip parameter for the next page.");
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
        }
    }
}
