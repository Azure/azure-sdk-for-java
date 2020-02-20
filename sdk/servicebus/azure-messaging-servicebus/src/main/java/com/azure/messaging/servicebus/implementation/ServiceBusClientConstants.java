// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.time.Duration;

/**
 *  Constants used by Service Bus SDK.
 */
public class ServiceBusClientConstants {
    public static final String AZURE_ACTIVE_DIRECTORY_SCOPE = "https://servicebus.azure.net/.default";
    public static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(60);
    public static final Duration TOKEN_VALIDITY = Duration.ofMinutes(20);
}
