// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.models.CosmosItemOperation;

import java.util.concurrent.atomic.AtomicReference;

public abstract class CosmosItemOperationBase implements CosmosItemOperation {

    private final AtomicReference<Integer> serializedLengthReference;
    private final AtomicReference<JsonSerializable> serializedOperation;

    public CosmosItemOperationBase() {
        this.serializedLengthReference = new AtomicReference<>(null);
        this.serializedOperation = new AtomicReference<>(null);
    }

    abstract JsonSerializable getSerializedOperationInternal();

    public JsonSerializable getSerializedOperation() {
        if (this.serializedOperation.get() == null) {
            this.serializedOperation.compareAndSet(null, this.getSerializedOperationInternal());
        }
        return this.serializedOperation.get();
    }

    public int getSerializedLength() {
        if (this.serializedLengthReference.get() == null) {
            this.serializedLengthReference.compareAndSet(null, this.getSerializedLengthInternal());
        }
        return this.serializedLengthReference.get();
    }

    private int getSerializedLengthInternal() {
        JsonSerializable operationSerializable = this.getSerializedOperation();
        String serializedValue = operationSerializable.toString();
        return serializedValue.codePointCount(0, serializedValue.length());
    }
}
