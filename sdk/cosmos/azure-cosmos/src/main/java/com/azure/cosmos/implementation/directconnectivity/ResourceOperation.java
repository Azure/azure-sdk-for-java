// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;

public class ResourceOperation {
    public final OperationType operationType;
    public final ResourceType resourceType;

    public ResourceOperation(
        OperationType operationType,
        ResourceType resourceType) {
        this.operationType = operationType;
        this.resourceType = resourceType;
    }
}
