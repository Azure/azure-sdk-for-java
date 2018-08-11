package com.microsoft.azure.servicebus.management;

import com.sun.org.apache.xpath.internal.Arg;

/**
 * This class can be used to format the path for different Service Bus entity types.
 */
public class EntityNameHelper {
    private static final String pathDelimiter = "/";
    private static final String subscriptionsSubPath = "Subscriptions";
    private static final String rulesSubPath = "Rules";
    private static final String subQueuePrefix = "$";
    private static final String deadLetterQueueSuffix = "DeadLetterQueue";
    private static final String deadLetterQueueName = subQueuePrefix + deadLetterQueueSuffix;
    private static final String transfer = "Transfer";
    private static final String transferDeadLetterQueueName = subQueuePrefix + transfer + pathDelimiter + deadLetterQueueName;

    /**
     * Formats the dead letter path for either a queue, or a subscription.
     * @param entityPath - The name of the queue, or path of the subscription.
     * @return - The path as a String of the dead letter entity.
     */
    public static String formatDeadLetterPath(String entityPath) {
        return formatSubQueuePath(entityPath, deadLetterQueueName);
    }

    /**
     * Formats the subscription path, based on the topic path and subscription name.
     * @param topicPath - The name of the topic, including slashes.
     * @param subscriptionName - The name of the subscription.
     */
    public static String formatSubscriptionPath(String topicPath, String subscriptionName) {
        return String.join(pathDelimiter, topicPath, subscriptionsSubPath, subscriptionName);
    }

    /**
     * Formats the rule path, based on the topic path, subscription name and the rule name.
     * @param topicPath - The name of the topic, including slashes.
     * @param subscriptionName - The name of the subscription.
     * @param ruleName - The name of the rule.
     */
    public static String formatRulePath(String topicPath, String subscriptionName, String ruleName) {
        return String.join(pathDelimiter,
                topicPath,
                subscriptionsSubPath,
                subscriptionName,
                rulesSubPath,
                ruleName);
    }

    /**
     * Formats the transfer dead letter path.
     * @param entityPath - Path of the entity whose transfer dead letter needs to be formatted.
     */
    public static String formatTransferDeadLetterPath(String entityPath) {
        return String.join(pathDelimiter, entityPath, transferDeadLetterQueueName);
    }

    static String formatSubQueuePath(String entityPath, String subQueueName) {
        return entityPath + EntityNameHelper.pathDelimiter + subQueueName;
    }

    static void checkValidQueueName(String queueName) {
        checkValidEntityName(queueName, ManagementClientConstants.QUEUE_NAME_MAX_LENGTH, true);
    }

    static void checkValidTopicName(String topicName) {
        checkValidEntityName(topicName, ManagementClientConstants.TOPIC_NAME_MAX_LENGTH, true);
    }

    static void checkValidSubscriptionName(String subscriptionName) {
        checkValidEntityName(subscriptionName, ManagementClientConstants.SUBSCRIPTION_NAME_MAX_LENGTH, false);
    }

    static void checkValidRuleName(String ruleName) {
        checkValidEntityName(ruleName, ManagementClientConstants.RULE_NAME_MAX_LENGTH, false);
    }

    private static void checkValidEntityName(String entityName, int maxEntityNameLength, boolean allowSeparator) {
        if (entityName == null || entityName.isEmpty()) {
            throw new IllegalArgumentException("Entity name cannot be null");
        }

        String tempName = entityName.replace('\\', '/');
        if (tempName.length() > maxEntityNameLength) {
            throw new IllegalArgumentException("Entity path " + entityName + " exceeds the " + maxEntityNameLength + " character limit");
        }

        if (tempName.startsWith(pathDelimiter) || tempName.endsWith(pathDelimiter)) {
            throw new IllegalArgumentException("The entity name cannot contain '/' as prefix or suffix.");
        }

        if (!allowSeparator && tempName.contains(pathDelimiter)) {
            throw new IllegalArgumentException("The entity name contains an invalid character '" + pathDelimiter + "'");
        }

        for (char key : ManagementClientConstants.InvalidEntityPathCharacters) {
            if (entityName.indexOf(key) >= 0) {
                throw new IllegalArgumentException(entityName + " contains character '" + key + "' which is not allowed" +
                        "because it is reserved in the Uri scheme.");
            }
        }
    }
}
