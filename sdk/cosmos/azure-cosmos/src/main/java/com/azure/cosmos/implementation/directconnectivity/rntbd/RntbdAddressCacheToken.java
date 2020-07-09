//------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.SocketAddress;
import java.net.URI;
import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Models the address cache token used to clear Cosmos client's address cache when the {@link RntbdTransportClient}
 * triggers an event on connection reset by a replica.
 */
public final class RntbdAddressCacheToken {

    private final RntbdEndpoint endpoint;
    private final PartitionKeyRangeIdentity partitionKeyRangeIdentity;

    public RntbdAddressCacheToken(
        final PartitionKeyRangeIdentity partitionKeyRangeIdentity,
        final RntbdEndpoint endpoint) {

        this.endpoint = checkNotNull(endpoint, "expected non-null endpoint");
        this.partitionKeyRangeIdentity = partitionKeyRangeIdentity;
    }

    @JsonProperty
    public PartitionKeyRangeIdentity getPartitionKeyRangeIdentity() {
        return this.partitionKeyRangeIdentity;
    }

    @JsonProperty
    public SocketAddress getRemoteAddress() {
        return this.endpoint.remoteAddress();
    }

    public URI getRemoteURI() {
        return this.endpoint.remoteURI();
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) {
            return true;
        }

        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        final RntbdAddressCacheToken that = (RntbdAddressCacheToken) other;

        return this.endpoint.equals(that.endpoint)
            && Objects.equals(this.partitionKeyRangeIdentity, that.partitionKeyRangeIdentity);
    }

    public boolean equals(final RntbdAddressCacheToken other) {
        return other != null
            && this.endpoint.equals(other.endpoint)
            && Objects.equals(this.partitionKeyRangeIdentity, other.partitionKeyRangeIdentity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.partitionKeyRangeIdentity, this.endpoint);
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }
}
