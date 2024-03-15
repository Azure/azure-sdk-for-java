// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public interface IGlobalPartitionEndpointManager {
    boolean tryMarkPartitionKeyRangeAsUnavailable(RxDocumentServiceRequest request);
    boolean tryMarkPartitionKeyRangeAsAvailable(RxDocumentServiceRequest request);
    boolean tryAddPartitionKeyRangeLevelOverride(RxDocumentServiceRequest request);
}
