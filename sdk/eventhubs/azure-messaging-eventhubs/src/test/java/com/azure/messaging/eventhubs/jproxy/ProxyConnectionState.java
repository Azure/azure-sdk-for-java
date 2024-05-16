// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.jproxy;

/**
 * The set of states that the {@link ProxyServer} can be in.
 */
enum ProxyConnectionState {
    PROXY_NOT_STARTED,
    PROXY_INITIATED,
    PROXY_CONNECTED,
    PROXY_FAILED,
    PROXY_CLOSED
}
