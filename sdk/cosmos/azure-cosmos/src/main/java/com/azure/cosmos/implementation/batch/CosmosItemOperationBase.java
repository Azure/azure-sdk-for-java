// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemSerializer;
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

    public abstract CosmosItemSerializer getEffectiveItemSerializer();

    abstract JsonSerializable getSerializedOperationInternal(CosmosItemSerializer clientItemSerializer);

    public JsonSerializable getSerializedOperation(CosmosItemSerializer clientItemSerializer) {
        if (this.serializedOperation.get() == null) {
            this.serializedOperation.compareAndSet(null, this.getSerializedOperationInternal(clientItemSerializer));
        }
        return this.serializedOperation.get();
    }

    public int getSerializedLength(CosmosItemSerializer clientItemSerializer) {
        if (this.serializedLengthReference.get() == null) {
            this.serializedLengthReference.compareAndSet(null, this.getSerializedLengthInternal(clientItemSerializer));
        }
        return this.serializedLengthReference.get();
    }

    private int getSerializedLengthInternal(CosmosItemSerializer clientItemSerializer) {
        JsonSerializable operationSerializable = this.getSerializedOperation(clientItemSerializer);
        String serializedValue = operationSerializable.toString();
        return serializedValue.codePointCount(0, serializedValue.length());
    }
}
