// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import java.time.Duration;

public final class ClientConstants {
    public static final String AZURE_ACTIVE_DIRECTORY_SCOPE = "https://eventhubs.azure.net/.default";
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    public static final String AZ_NAMESPACE_VALUE = "Microsoft.EventHub";
    public static final Duration TOKEN_VALIDITY = Duration.ofMinutes(20);
    public static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(60);

    // Logging context keys
    // in sync with azure-core-amqp, but duplicate to minimize dependency
    public static final String CONNECTION_ID_KEY = "connectionId";
    public static final String LINK_NAME_KEY = "linkName";
    public static final String ENTITY_PATH_KEY = "entityPath";
    public static final String SIGNAL_TYPE_KEY = "signalType";
    public static final String CLIENT_IDENTIFIER_KEY = "clientIdentifier";
    public static final String EMIT_RESULT_KEY = "emitResult";

    // EventHubs specific logging context keys
    public static final String PARTITION_ID_KEY = "partitionId";
    public static final String PARTITION_KEY_KEY = "partitionKey";
    public static final String SEQUENCE_NUMBER_KEY = "sequenceNumber";
    public static final String CONSUMER_GROUP_KEY = "consumerGroup";
    public static final String OWNER_ID_KEY = "ownerId";
    public static final String TRACKING_ID_KEY = "trackingId";
    public static final String WORK_ID_KEY = "workId";
    public static final String CREDITS_KEY = "credits";
    public static final String SUBSCRIBER_ID_KEY = "subscriberId";

    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    public static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;

    /**
     * URI format for an Event Hubs FQDN.
     */
    public static final String ENDPOINT_FORMAT = "sb://%s.%s";
    public static final String AZ_TRACING_SERVICE_NAME = "EventHubs.";
}
