/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for RouteNextHopType.
 */
public class RouteNextHopType {
    /** Static value VirtualNetworkGateway for RouteNextHopType. */
    public static final String VIRTUALNETWORKGATEWAY = "VirtualNetworkGateway";

    /** Static value VnetLocal for RouteNextHopType. */
    public static final String VNETLOCAL = "VnetLocal";

    /** Static value Internet for RouteNextHopType. */
    public static final String INTERNET = "Internet";

    /** Static value VirtualAppliance for RouteNextHopType. */
    public static final String VIRTUALAPPLIANCE = "VirtualAppliance";

    /** Static value None for RouteNextHopType. */
    public static final String NONE = "None";

}
