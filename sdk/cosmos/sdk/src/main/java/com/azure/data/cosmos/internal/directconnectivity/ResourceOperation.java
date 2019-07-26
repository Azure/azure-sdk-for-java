// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.ResourceType;

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
