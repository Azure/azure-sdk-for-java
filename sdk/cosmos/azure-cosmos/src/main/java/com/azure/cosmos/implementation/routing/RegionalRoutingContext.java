// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import java.net.URI;
import java.util.Objects;

public class RegionalRoutingContext {

    // IMPORTANT:
    // Please reevaluate overridden equals() implementation
    // when adding additional properties to this class
    private final URI gatewayRegionalEndpoint;

    public RegionalRoutingContext(URI gatewayRegionalEndpoint) {
        this.gatewayRegionalEndpoint = gatewayRegionalEndpoint;
    }

    public URI getGatewayRegionalEndpoint() {
        return this.gatewayRegionalEndpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionalRoutingContext that = (RegionalRoutingContext) o;
        return this.gatewayRegionalEndpoint.equals(that.gatewayRegionalEndpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.gatewayRegionalEndpoint);
    }
}
