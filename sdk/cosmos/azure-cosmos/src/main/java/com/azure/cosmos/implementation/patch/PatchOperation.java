// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.util.Beta;

@Beta(Beta.SinceVersion.V4_7_0)
public abstract class PatchOperation {

    private PatchOperationType operationType;

    /**
     * Initializes a new instance of the {@link PatchOperation} class.
     *
     * @param operationType Specifies the type of Patch operation
     */
    protected PatchOperation(PatchOperationType operationType) {
        this.operationType = operationType;
    }

    PatchOperationType getOperationType() {
        return operationType;
    }
}
