/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VirtualNetworkGatewaySkuTier.
 */
public class VirtualNetworkGatewaySkuTier {
    /** Static value Basic for VirtualNetworkGatewaySkuTier. */
    public static final String BASIC = "Basic";

    /** Static value HighPerformance for VirtualNetworkGatewaySkuTier. */
    public static final String HIGHPERFORMANCE = "HighPerformance";

    /** Static value Standard for VirtualNetworkGatewaySkuTier. */
    public static final String STANDARD = "Standard";

}
