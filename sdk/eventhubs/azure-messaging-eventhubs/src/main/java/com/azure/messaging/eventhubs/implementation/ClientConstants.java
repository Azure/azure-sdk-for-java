// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import java.time.Duration;

public final class ClientConstants {
    public static final String AZURE_ACTIVE_DIRECTORY_SCOPE = "https://eventhubs.azure.net/.default";
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
}
