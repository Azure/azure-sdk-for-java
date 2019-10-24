// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.internal.directconnectivity;

import com.azure.cosmos.internal.RxDocumentServiceRequest;
import reactor.core.publisher.Mono;

public interface IAddressResolver {
    Mono<AddressInformation[]> resolveAsync(
            RxDocumentServiceRequest request,
            boolean forceRefreshPartitionAddresses);
}
