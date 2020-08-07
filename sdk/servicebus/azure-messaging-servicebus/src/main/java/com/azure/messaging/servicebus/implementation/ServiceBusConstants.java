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

    /**
     * Represents the maximum ttl for a message or entity.
     */
    public static final Duration MAX_DURATION = Duration.parse("P10675199DT2H48M5.4775807S");
    /**
     * Represents the default lock duration for a message.
     */
    public static final Duration DEFAULT_LOCK_DURATION = Duration.ofSeconds(60);
}
