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

    public abstract CosmosItemSerializer getEffectiveItemSerializerForResult();

    abstract JsonSerializable getSerializedOperationInternal(CosmosItemSerializer effectiveItemSerializer);

    public JsonSerializable getSerializedOperation(CosmosItemSerializer effectiveItemSerializer) {
        if (this.serializedOperation.get() == null) {
            this.serializedOperation.compareAndSet(null, this.getSerializedOperationInternal(effectiveItemSerializer));
        }
        return this.serializedOperation.get();
    }

    public int getSerializedLength(CosmosItemSerializer effectiveItemSerializer) {
        if (this.serializedLengthReference.get() == null) {
            this.serializedLengthReference.compareAndSet(null, this.getSerializedLengthInternal(effectiveItemSerializer));
        }
        return this.serializedLengthReference.get();
    }

    private int getSerializedLengthInternal(CosmosItemSerializer effectiveItemSerializer) {
        JsonSerializable operationSerializable = this.getSerializedOperation(effectiveItemSerializer);
        String serializedValue = operationSerializable.toString();
        return serializedValue.codePointCount(0, serializedValue.length());
    }
}
