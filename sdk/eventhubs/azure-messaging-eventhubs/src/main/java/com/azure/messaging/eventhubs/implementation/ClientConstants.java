// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpMessageConstant;
import org.apache.qpid.proton.amqp.Symbol;

import java.time.Duration;

import static com.azure.core.amqp.implementation.AmqpConstants.VENDOR;

public final class ClientConstants {
    public static final String AZURE_ACTIVE_DIRECTORY_SCOPE = "https://eventhubs.azure.net/.default";
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    public static final String AZ_NAMESPACE_VALUE = "Microsoft.EventHub";
    public static final Duration TOKEN_VALIDITY = Duration.ofMinutes(20);
    public static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(60);

    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    public static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;

    /**
     * URI format for an Event Hubs FQDN.
     */
    public static final String ENDPOINT_FORMAT = "sb://%s.%s";
    public static final String AZ_TRACING_SERVICE_NAME = "EventHubs.";


    // Symbols used on links
    public static final Symbol EPOCH = Symbol.valueOf(VENDOR + ":epoch");
    public static final Symbol ENABLE_RECEIVER_RUNTIME_METRIC_NAME = Symbol.valueOf(
        VENDOR + ":enable-receiver-runtime-metric");
    public static final Symbol ENABLE_IDEMPOTENT_PRODUCER = Symbol.valueOf(VENDOR + ":idempotent-producer");

    public static final Symbol PRODUCER_EPOCH = Symbol.valueOf(
        AmqpMessageConstant.PRODUCER_EPOCH_ANNOTATION_NAME.getValue());
    public static final Symbol PRODUCER_ID = Symbol.valueOf(AmqpMessageConstant.PRODUCER_ID_ANNOTATION_NAME.getValue());
    public static final Symbol PRODUCER_SEQUENCE_NUMBER = Symbol.valueOf(
        AmqpMessageConstant.PRODUCER_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
}
