// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.jedis;

/**
 * Constants used for logging context.
 * They are in sync with azure-core-amqp, but duplicate to minimize dependency.
 *
 * @see <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ClientConstants.java">ClientConstants.java</a>
 */
class ClientConstants {
    static final String CONSUMER_GROUP_KEY = "consumerGroup";
    static final String ENTITY_NAME_KEY = "entityName";
    static final String HOSTNAME_KEY = "hostName";
}
