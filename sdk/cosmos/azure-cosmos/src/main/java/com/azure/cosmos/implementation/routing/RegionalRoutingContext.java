// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import java.net.URI;
import java.util.Objects;

public class RegionalRoutingContext {

    // IMPORTANT:
    // Please reevaluate overridden equals() implementation
    // when adding additional properties to this class
    private final String region;
    private final URI gatewayRegionalEndpoint;
    private final String gatewayRegionalEndpointAsString;
    private URI thinclientRegionalEndpoint;
    private String thinclientRegionalEndpointAsString;

    public RegionalRoutingContext(URI gatewayRegionalEndpoint, String region) {
        this.gatewayRegionalEndpoint = gatewayRegionalEndpoint;
        this.gatewayRegionalEndpointAsString = gatewayRegionalEndpoint.toString();
        this.region = region;
        this.thinclientRegionalEndpoint = null;
        this.thinclientRegionalEndpointAsString = null;
    }

    public RegionalRoutingContext(URI gatewayRegionalEndpoint) {
        this(gatewayRegionalEndpoint, null);
    }

    public String getRegion() {
        return this.region;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionalRoutingContext that = (RegionalRoutingContext) o;
        if (this.thinclientRegionalEndpoint != null) {
            return this.gatewayRegionalEndpoint.equals(that.gatewayRegionalEndpoint) &&
                this.thinclientRegionalEndpoint.equals(that.thinclientRegionalEndpoint);
        } else {
            return this.gatewayRegionalEndpoint.equals(that.gatewayRegionalEndpoint);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.gatewayRegionalEndpoint, this.thinclientRegionalEndpoint);
    }

    @Override
    public String toString() {
        return "RegionalRoutingContext{" +
            "gatewayRegionalEndpoint=" + gatewayRegionalEndpointAsString +
            ", thinclientRegionalEndpoint=" + thinclientRegionalEndpointAsString +
            '}';
    }
}
