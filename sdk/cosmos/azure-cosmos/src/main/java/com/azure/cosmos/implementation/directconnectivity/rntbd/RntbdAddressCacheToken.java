//------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.AddressResolverExtension;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
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

    private final URI addressResolverURI;
    private final RntbdEndpoint endpoint;
    private final PartitionKeyRangeIdentity partitionKeyRangeIdentity;

    public RntbdAddressCacheToken(
        final AddressResolverExtension addressResolver,
        final RntbdEndpoint endpoint,
        final RxDocumentServiceRequest request) {

        checkNotNull(addressResolver, "expected non-null resolver");
        checkNotNull(endpoint, "expected non-null endpoint");
        checkNotNull(request, "expected non-null request");

        this.addressResolverURI = addressResolver.getAddressResolverURI(request);
        this.endpoint = endpoint;
        this.partitionKeyRangeIdentity = request.getPartitionKeyRangeIdentity();
    }

    public URI getAddressResolverURI(GlobalAddressResolver resolver) {
        return this.addressResolverURI;
    }

    @JsonProperty
    public URI getAddressResolverURI() {
        return this.addressResolverURI;
    }

    @JsonProperty
    public PartitionKeyRangeIdentity getPartitionKeyRangeIdentity() {
        return this.partitionKeyRangeIdentity;
    }

    @JsonProperty
    public SocketAddress getRemoteAddress() {
        return this.endpoint.remoteAddress();
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
            && Objects.equals(this.getPartitionKeyRangeIdentity(), that.getPartitionKeyRangeIdentity());
    }

    public boolean equals(final RntbdAddressCacheToken other) {
        return other != null
            && this.endpoint.equals(other.endpoint)
            && Objects.equals(this.getPartitionKeyRangeIdentity(), other.getPartitionKeyRangeIdentity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPartitionKeyRangeIdentity(), this.endpoint);
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }
}
