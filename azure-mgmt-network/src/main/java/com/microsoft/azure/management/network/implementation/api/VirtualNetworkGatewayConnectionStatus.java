/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VirtualNetworkGatewayConnectionStatus.
 */
public class VirtualNetworkGatewayConnectionStatus {
    /** Static value Unknown for VirtualNetworkGatewayConnectionStatus. */
    public static final String UNKNOWN = "Unknown";

    /** Static value Connecting for VirtualNetworkGatewayConnectionStatus. */
    public static final String CONNECTING = "Connecting";

    /** Static value Connected for VirtualNetworkGatewayConnectionStatus. */
    public static final String CONNECTED = "Connected";

    /** Static value NotConnected for VirtualNetworkGatewayConnectionStatus. */
    public static final String NOTCONNECTED = "NotConnected";

}
