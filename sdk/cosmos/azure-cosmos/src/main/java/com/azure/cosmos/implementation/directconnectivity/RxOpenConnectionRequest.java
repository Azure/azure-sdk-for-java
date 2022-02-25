// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryContext;

public class RxOpenConnectionRequest {

    private final OperationType operationType;
    private final ResourceType resourceType;
    private final RetryContext retryContext;

    public static RxOpenConnectionRequest INSTANCE = new RxOpenConnectionRequest();

    private RxOpenConnectionRequest() {
        this.operationType = OperationType.OpenConnection;
        this.resourceType = ResourceType.Connection;
        this.retryContext = new RetryContext();
    }

    public ResourceType getResourceType() {
        return this.getResourceType();
    }

    public OperationType getOperationType() {
        return this.getOperationType();
    }

    public RetryContext getRetryContext() {
        return retryContext;
    }
}
