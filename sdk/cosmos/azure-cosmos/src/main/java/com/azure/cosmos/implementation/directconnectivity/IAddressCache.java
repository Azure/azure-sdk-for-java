// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface IAddressCache {

    /**
     * Update the physical address of the {@link PartitionKeyRangeIdentity partition key range identity} associated to the serverKey.
     *
     *
     */
    void updateAddresses(URI serverKey);

    /**
     * Resolves physical addresses by either PartitionKeyRangeIdentity.
     *
     *
     * @param request Request is needed only by GatewayAddressCache in the only case when request is name based and user has name based auth token.
     *                PartitionkeyRangeIdentity can be used to locate auth token in this case.
     * @param partitionKeyRangeIdentity target partition key range Id
     * @param forceRefreshPartitionAddresses Whether addresses need to be refreshed as previously resolved addresses were determined to be outdated.
     * @return Physical addresses.
     */
    Mono<Utils.ValueHolder<AddressInformation[]>> tryGetAddresses(
            RxDocumentServiceRequest request,
            PartitionKeyRangeIdentity partitionKeyRangeIdentity,
            boolean forceRefreshPartitionAddresses);
}
