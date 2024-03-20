// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public interface IGlobalPartitionEndpointManager {
    boolean tryMarkRegionAsUnavailableForPartitionKeyRange(RxDocumentServiceRequest request);
    boolean tryBookmarkRegionSuccessForPartitionKeyRange(RxDocumentServiceRequest request);
    boolean isRegionAvailableForPartitionKeyRange(RxDocumentServiceRequest request);
}
