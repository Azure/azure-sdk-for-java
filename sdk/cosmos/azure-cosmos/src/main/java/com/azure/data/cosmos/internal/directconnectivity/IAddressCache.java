// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import reactor.core.publisher.Mono;

public interface IAddressCache {

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
    Mono<AddressInformation[]> tryGetAddresses(
            RxDocumentServiceRequest request,
            PartitionKeyRangeIdentity partitionKeyRangeIdentity,
            boolean forceRefreshPartitionAddresses);
}
