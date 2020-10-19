// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.patch.PatchOperationCore;
import com.azure.cosmos.implementation.patch.PatchOperationType;

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

    public static <T> PatchOperation add(String path, T value) {
        return new PatchOperationCore<>(
            PatchOperationType.ADD,
            path,
            value);
    }

    public static PatchOperation remove(String path) {
        return new PatchOperationCore<>(
            PatchOperationType.REMOVE,
            path,
            null);
    }

    public static <T> PatchOperation replace(String path, T value) {
        return new PatchOperationCore<>(
            PatchOperationType.REPLACE,
            path,
            value);
    }

    public static <T> PatchOperation set(String path, T value) {
        return new PatchOperationCore<>(
            PatchOperationType.SET,
            path,
            value);
    }

    public static PatchOperation increment(String path, long value) {
        return new PatchOperationCore<>(
            PatchOperationType.INCREMENT,
            path,
            value);
    }

    public static PatchOperation increment(String path, double value) {
        return new PatchOperationCore<>(
            PatchOperationType.INCREMENT,
            path,
            value);
    }

    public PatchOperationType getOperationType() {
        return operationType;
    }
}
