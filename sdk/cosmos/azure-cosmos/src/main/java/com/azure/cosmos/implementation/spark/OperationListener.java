// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.spark;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.models.FeedResponse;

public interface OperationListener {
    void requestListener(OperationContext context, RxDocumentServiceRequest request);
    void responseListener(OperationContext context, RxDocumentServiceResponse response);
    void feedResponseReceivedListener(OperationContext context, FeedResponse<?> response);
    void feedResponseProcessedListener(OperationContext context, FeedResponse<?> response);
    void exceptionListener(OperationContext context, Throwable exception);
}
