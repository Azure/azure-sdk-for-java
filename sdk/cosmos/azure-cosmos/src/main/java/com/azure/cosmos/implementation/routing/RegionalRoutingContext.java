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

    // equals, hashCode and toString should only take dependency on gatewayRegionalEndpoint
    // because map lookups (including CaseInsensitiveMap which keys on toString()) are done
    // on RegionalRoutingContext with only the gateway regional endpoint.
    // thinclientRegionalEndpoint is set after construction via a mutable setter and must not
    // participate in identity or toString -- otherwise CaseInsensitiveMap keys change after insertion.
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
