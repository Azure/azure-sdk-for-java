// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdAddressCacheToken;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAddressResolver {
    Mono<AddressInformation[]> resolveAsync(
        RxDocumentServiceRequest request,
        boolean forceRefreshPartitionAddresses);
}
