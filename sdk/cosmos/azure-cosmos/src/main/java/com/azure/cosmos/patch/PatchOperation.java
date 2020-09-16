// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.patch;

import com.azure.cosmos.patch.implementation.PatchOperationType;

public abstract class PatchOperation<TResource> {

    private PatchOperationType operationType;
    private TResource resource;

    /**
     * Initializes a new instance of the {@link PatchOperation} class.
     *
     * @param operationType Specifies the type of Patch operation
     * @param value Specifies the value to be used
     */
    PatchOperation(PatchOperationType operationType, TResource value) {
        this.operationType = operationType;
        this.resource = value;
    }

    public static <T> PatchOperation<T> createAddOperation(String path, T value) {
        return new PatchOperationCore<T>(
            PatchOperationType.ADD,
            path,
            value);
    }

    public static PatchOperation<?> createRemoveOperation(String path) {
        return new PatchOperationCore<>(
            PatchOperationType.REMOVE,
            path,
            null);
    }

    public static <T> PatchOperation<T> createReplaceOperation(String path, T value) {
        return new PatchOperationCore<T>(
            PatchOperationType.REPLACE,
            path,
            value);
    }

    public static <T> PatchOperation<T> createSetOperation(String path, T value) {
        return new PatchOperationCore<T>(
            PatchOperationType.SET,
            path,
            value);
    }

    public PatchOperationType getOperationType() {
        return operationType;
    }

    public TResource getResource() {
        return resource;
    }
}
