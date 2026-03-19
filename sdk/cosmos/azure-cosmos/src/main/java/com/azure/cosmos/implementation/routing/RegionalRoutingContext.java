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
    private final String gatewayRegionalEndpointAsString;
    private URI thinclientRegionalEndpoint;
    private String thinclientRegionalEndpointAsString;

    public RegionalRoutingContext(URI gatewayRegionalEndpoint) {
        this.gatewayRegionalEndpoint = gatewayRegionalEndpoint;
        this.gatewayRegionalEndpointAsString = gatewayRegionalEndpoint.toString();
        this.thinclientRegionalEndpoint = null;
        this.thinclientRegionalEndpointAsString = null;
    }

    public URI getGatewayRegionalEndpoint() {
        return this.gatewayRegionalEndpoint;
    }

    public void setThinclientRegionalEndpoint(URI thinclientRegionalEndpoint) {
        this.thinclientRegionalEndpoint = thinclientRegionalEndpoint;
        this.thinclientRegionalEndpointAsString = thinclientRegionalEndpoint.toString();
    }

    public URI getThinclientRegionalEndpoint() {
        return this.thinclientRegionalEndpoint;
    }

    // equals, hashCode and toString only depend on gatewayRegionalEndpoint because
    // there are map lookups done on RegionalRoutingContext with only the gateway regional endpoint.
    // Lookup based on gateway regional endpoint is "safer" as gateway regional endpoint is expected
    // to always be returned in the DatabaseAccount payload from the Gateway service.
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

    @Override
    public String toString() {
        return gatewayRegionalEndpointAsString;
    }
}
