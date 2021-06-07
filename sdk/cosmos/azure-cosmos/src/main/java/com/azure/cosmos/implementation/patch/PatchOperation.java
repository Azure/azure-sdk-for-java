// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

public abstract class PatchOperation {

    private final PatchOperationType operationType;

    /**
     * Initializes a new instance of the {@link PatchOperation} class.
     *
     * @param operationType Specifies the type of Patch operation
     */
    PatchOperation(PatchOperationType operationType) {
        this.operationType = operationType;
    }

    PatchOperationType getOperationType() {
        return operationType;
    }
}
