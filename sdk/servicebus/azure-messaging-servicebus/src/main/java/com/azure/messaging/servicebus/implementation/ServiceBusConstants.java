// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.time.Duration;

/**
 * Constants defined to be used for interaction with Service Bus.
 */
public class ServiceBusConstants {

    public static final String AZURE_ACTIVE_DIRECTORY_SCOPE = "https://servicebus.azure.net/.default";
    public static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(60);
    public static final Duration TOKEN_VALIDITY = Duration.ofMinutes(20);

    public static final String REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER = "from-sequence-number";
    public static final String REQUEST_RESPONSE_MESSAGE_COUNT = "message-count";
    public static final String REQUEST_RESPONSE_SESSION_ID = "session-id";
}
