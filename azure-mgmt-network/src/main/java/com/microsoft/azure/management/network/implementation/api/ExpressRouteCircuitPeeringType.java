/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

/**
 * Defines values for ExpressRouteCircuitPeeringType.
 */
public final class ExpressRouteCircuitPeeringType {
    /** Static value AzurePublicPeering for ExpressRouteCircuitPeeringType. */
    public static final String AZUREPUBLICPEERING = "AzurePublicPeering";

    /** Static value AzurePrivatePeering for ExpressRouteCircuitPeeringType. */
    public static final String AZUREPRIVATEPEERING = "AzurePrivatePeering";

    /** Static value MicrosoftPeering for ExpressRouteCircuitPeeringType. */
    public static final String MICROSOFTPEERING = "MicrosoftPeering";

    private ExpressRouteCircuitPeeringType() {
    }
}
