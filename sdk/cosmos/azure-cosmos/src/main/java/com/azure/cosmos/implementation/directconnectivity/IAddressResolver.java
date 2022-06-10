// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface IAddressResolver {

    Mono<AddressInformation[]> resolveAsync(
            RxDocumentServiceRequest request,
            boolean forceRefreshPartitionAddresses);

    int updateAddresses(URI serverKey);

    /***
     * Warm up caches and open connections to all replicas of the container for the current read region.
     *
     * @param containerLink the container link.
     *
     * @return A flux of {@link OpenConnectionResponse}.
     */
    Flux<OpenConnectionResponse> openConnectionsAndInitCaches(String containerLink);

    /***
     * Set the open connection handler so SDK can proactively create connections based on need.
     *
     * @param openConnectionHandler the {@link IOpenConnectionsHandler}.
     */
    void setOpenConnectionsHandler(IOpenConnectionsHandler openConnectionHandler);
}
