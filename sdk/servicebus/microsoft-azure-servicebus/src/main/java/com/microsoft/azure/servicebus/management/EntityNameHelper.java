// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

/**
 * This class can be used to format the path for different Service Bus entity types.
 */
public class EntityNameHelper {
    private static final String PATH_DELIMITER = "/";
    private static final String SUBSCRIPTIONS_SUB_PATH = "Subscriptions";
    private static final String RULES_SUB_PATH = "Rules";
    private static final String SUB_QUEUE_PREFIX = "$";
    private static final String DEAD_LETTER_QUEUE_SUFFIX = "DeadLetterQueue";
    private static final String DEAD_LETTER_QUEUE_NAME = SUB_QUEUE_PREFIX + DEAD_LETTER_QUEUE_SUFFIX;
    private static final String TRANSFER = "Transfer";
    private static final String TRANSFER_DEAD_LETTER_QUEUE_NAME = SUB_QUEUE_PREFIX + TRANSFER + PATH_DELIMITER + DEAD_LETTER_QUEUE_NAME;

    /**
     * Formats the dead letter path for either a queue, or a subscription.
     * @param entityPath - The name of the queue, or path of the subscription.
     * @return - The path as a String of the dead letter entity.
     */
    public static String formatDeadLetterPath(String entityPath) {
        return formatSubQueuePath(entityPath, DEAD_LETTER_QUEUE_NAME);
    }

    /**
     * Formats the subscription path, based on the topic path and subscription name.
     * @param topicPath - The name of the topic, including slashes.
     * @param subscriptionName - The name of the subscription.
     * @return The path of the subscription.
     */
    public static String formatSubscriptionPath(String topicPath, String subscriptionName) {
        return String.join(PATH_DELIMITER, topicPath, SUBSCRIPTIONS_SUB_PATH, subscriptionName);
    }

    /**
     * Formats the rule path, based on the topic path, subscription name and the rule name.
     * @param topicPath - The name of the topic, including slashes.
     * @param subscriptionName - The name of the subscription.
     * @param ruleName - The name of the rule.
     * @return The path of the rule
     */
    public static String formatRulePath(String topicPath, String subscriptionName, String ruleName) {
        return String.join(PATH_DELIMITER,
                topicPath,
                SUBSCRIPTIONS_SUB_PATH,
                subscriptionName,
                RULES_SUB_PATH,
                ruleName);
    }

    /**
     * Formats the transfer dead letter path.
     * @param entityPath - Path of the entity whose transfer dead letter needs to be formatted.
     * @return The path of the transfer dead letter sub-queue for the entity
     */
    public static String formatTransferDeadLetterPath(String entityPath) {
        return String.join(PATH_DELIMITER, entityPath, TRANSFER_DEAD_LETTER_QUEUE_NAME);
    }

    static String formatSubQueuePath(String entityPath, String subQueueName) {
        return entityPath + EntityNameHelper.PATH_DELIMITER + subQueueName;
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
            throw new IllegalArgumentException("Entity name cannot be null.");
        }

        String tempName = entityName.replace('\\', '/');
        if (tempName.length() > maxEntityNameLength) {
            throw new IllegalArgumentException("Entity path " + entityName + " exceeds the " + maxEntityNameLength + " character limit");
        }

        if (tempName.startsWith(PATH_DELIMITER) || tempName.endsWith(PATH_DELIMITER)) {
            throw new IllegalArgumentException("The entity name cannot contain '/' as prefix or suffix.");
        }

        if (!allowSeparator && tempName.contains(PATH_DELIMITER)) {
            throw new IllegalArgumentException("The entity name contains an invalid character '" + PATH_DELIMITER + "'");
        }

        for (char key : ManagementClientConstants.INVALID_ENTITY_PATH_CHARACTERS) {
            if (entityName.indexOf(key) >= 0) {
                throw new IllegalArgumentException(entityName + " contains character '" + key + "' which is not allowed"
                    + "because it is reserved in the Uri scheme.");
            }
        }
    }
}
