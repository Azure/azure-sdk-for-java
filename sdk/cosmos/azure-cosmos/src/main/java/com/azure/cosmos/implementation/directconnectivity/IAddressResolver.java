// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface IAddressResolver {

    Mono<AddressInformation[]> resolveAsync(
            RxDocumentServiceRequest request,
            boolean forceRefreshPartitionAddresses);

    void updateAddresses(RxDocumentServiceRequest request, URI serverKey);
}
