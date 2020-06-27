//------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.SocketAddress;

/**
 * Models the address cache token used to clear Cosmos client's address cache when the {@link RntbdTransportClient}
 * triggers an event on connection reset by a replica.
 */
public final class RntbdAddressCacheToken {

    private final PartitionKeyRangeIdentity partitionKeyRangeIdentity;
    private final RntbdEndpoint endpoint;

    public RntbdAddressCacheToken(PartitionKeyRangeIdentity partitionKeyRangeIdentity, RntbdEndpoint endpoint) {
        this.partitionKeyRangeIdentity = partitionKeyRangeIdentity;
        this.endpoint = endpoint;
    }

    @JsonProperty
    public PartitionKeyRangeIdentity getPartitionKeyRangeIdentity() {
        return partitionKeyRangeIdentity;
    }

    @JsonProperty
    public SocketAddress remoteAddress() {
        return this.endpoint.remoteAddress();
    }

    @Override
    public boolean equals(Object other) {
        return this.equals(other instanceof RntbdAddressCacheToken ? (RntbdAddressCacheToken) other : null);
    }

    public boolean equals(RntbdAddressCacheToken other) {
        return other != null && this.partitionKeyRangeIdentity.equals(other.partitionKeyRangeIdentity) &&
            this.endpoint.equals(other.endpoint);
    }

    @Override
    public int hashCode() {
        return this.partitionKeyRangeIdentity.hashCode() ^ this.remoteAddress().hashCode();
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }
}
