// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.apache.qpid.proton.amqp.Symbol;

import java.time.Duration;

import static com.azure.core.amqp.implementation.AmqpConstants.VENDOR;

/**
 * Constants defined to be used for interaction with Service Bus.
 */
public class ServiceBusConstants {
    public static final String AZURE_ACTIVE_DIRECTORY_SCOPE = "https://servicebus.azure.net/.default";
    public static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(60);
    public static final Duration TOKEN_VALIDITY = Duration.ofMinutes(20);
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    public static final String AZ_TRACING_NAMESPACE_VALUE = "Microsoft.ServiceBus";

    public static final String SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME = "ServiceBusSupplementaryAuthorization";
    public static final String SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME = "ServiceBusDlqSupplementaryAuthorization";

    /**
     * Represents the maximum ttl for a message or entity.
     */
    public static final Duration MAX_DURATION = Duration.parse("P10675199DT2H48M5.4775807S");
    /**
     * Represents the default lock duration for a message.
     */
    public static final Duration DEFAULT_LOCK_DURATION = Duration.ofSeconds(60);

    /**
     * Represents the default duplicate detection duration.
     */
    public static final Duration DEFAULT_DUPLICATE_DETECTION_DURATION = Duration.ofSeconds(60);

    /**
     * Represents the default max delivery count for a message.
     */
    public static final int DEFAULT_MAX_DELIVERY_COUNT = 10;

    /**
     * Represents the default queue size in megabytes.
     */
    public static final int DEFAULT_QUEUE_SIZE = 1024;

    /**
     * Represents the default topic size in megabytes.
     */
    public static final int DEFAULT_TOPIC_SIZE = 1024;

    /**
     * Represents the max lock renewal delay buffer in seconds.
     */
    public static final Duration MAX_RENEWAL_BUFFER_DURATION = Duration.ofSeconds(10);

    // Logging keys:

    /**
     * Identifies lock token in logs.
     */
    public static final String LOCK_TOKEN_KEY = "lockToken";

    /**
     * Identifies work id in logs.
     */
    public static final String WORK_ID_KEY = "workId";

    /**
     * Identifies session id in logs.
     */
    public static final String SESSION_ID_KEY = "sessionId";

    /**
     * Identifies message id in logs.
     */
    public static final String MESSAGE_ID_LOGGING_KEY = "messageId";

    /**
     * Identifies entity type in logs.
     */
    public static final String ENTITY_TYPE_KEY = "entityType";

    /**
     * Identifies sequence number in logs.
     */
    public static final String SEQUENCE_NUMBER_KEY = "sequenceNumber";

    /**
     * Identifies delivery state in logs.
     */
    public static final String DELIVERY_STATE_KEY = "deliveryState";

    /**
     * Identifies number of requested messages in logs.
     */
    public static final String NUMBER_OF_REQUESTED_MESSAGES_KEY = "requested";

    /**
     * Identifies disposition status in logs.
     */
    public static final String DISPOSITION_STATUS_KEY = "dispositionStatus";


    /**
     * Amqp symbol name.
     */
    public static final String URI_NAME = VENDOR + ":uri";
    public static final String DURATION_NAME = VENDOR + ":timespan";
    public static final String OFFSETDATETIME_NAME = VENDOR + ":datetime-offset";

    /**
     * Amqp symbol for specific type.
     */
    public static final Symbol URI_SYMBOL = Symbol.valueOf(URI_NAME);
    public static final Symbol DURATION_SYMBOL = Symbol.valueOf(DURATION_NAME);
    public static final Symbol OFFSETDATETIME_SYMBOL = Symbol.valueOf(OFFSETDATETIME_NAME);

    /**
     * Constants for conversion between ticks and nanosecond.
     */
    public static final Long EPOCH_TICKS = 621355968000000000L;
    public static final Long TICK_PER_SECOND = 10_000_000L;
    public static final Long NANO_PER_SECOND = 1000_000_000L;
    public static final Long TIME_LENGTH_DELTA = NANO_PER_SECOND / TICK_PER_SECOND;
}
