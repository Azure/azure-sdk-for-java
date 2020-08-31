//------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.AddressResolverExtension;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.net.URI;
import java.util.Objects;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdReporter.reportIssueUnless;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Models the address cache token used to clear Cosmos client's address cache when the {@link RntbdTransportClient}
 * triggers an event on connection reset by a replica.
 */
public final class RntbdAddressCacheToken {

    private static Logger logger = LoggerFactory.getLogger(RntbdAddressCacheToken.class);

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

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = request.getPartitionKeyRangeIdentity();

        if (partitionKeyRangeIdentity == null) {

            final PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;
            final String collectionRid = request.requestContext.resolvedCollectionRid;

            if (collectionRid != null) {

                partitionKeyRangeIdentity = partitionKeyRange != null
                    ? new PartitionKeyRangeIdentity(collectionRid, partitionKeyRange.getId())
                    : new PartitionKeyRangeIdentity(collectionRid);

            } else {

                // TODO (DANOBLE) This happens on certain operations (e.g., read Database)
                //  Is there an alternative mechanism for producing a partition key range identity or is this expected?

                reportIssueUnless(logger, partitionKeyRange == null, request.requestContext,
                    "expected null partitionKeyRange, not {} for '{} {}' operation",
                    partitionKeyRange,
                    request.getOperationType(),
                    request.getResourceType());
            }
        }

        this.partitionKeyRangeIdentity = partitionKeyRangeIdentity;
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
