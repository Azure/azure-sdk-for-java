/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

/**
 * Defines values for ExpressRouteCircuitPeeringAdvertisedPublicPrefixState.
 */
public final class ExpressRouteCircuitPeeringAdvertisedPublicPrefixState {
    /** Static value NotConfigured for ExpressRouteCircuitPeeringAdvertisedPublicPrefixState. */
    public static final String NOT_CONFIGURED = "NotConfigured";

    /** Static value Configuring for ExpressRouteCircuitPeeringAdvertisedPublicPrefixState. */
    public static final String CONFIGURING = "Configuring";

    /** Static value Configured for ExpressRouteCircuitPeeringAdvertisedPublicPrefixState. */
    public static final String CONFIGURED = "Configured";

    /** Static value ValidationNeeded for ExpressRouteCircuitPeeringAdvertisedPublicPrefixState. */
    public static final String VALIDATION_NEEDED = "ValidationNeeded";

    private ExpressRouteCircuitPeeringAdvertisedPublicPrefixState() {
    }
}
