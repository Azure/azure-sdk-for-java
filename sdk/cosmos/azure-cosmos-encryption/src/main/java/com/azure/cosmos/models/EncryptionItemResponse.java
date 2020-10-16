// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

class EncryptionItemResponse<T> extends CosmosItemResponse<T> {
    private final T resource;

    public EncryptionItemResponse(CosmosItemResponse<?> response, T item) {
        super(EncryptionModelBridgeInternal.getResourceResponse(response), null, (Class<T>) item.getClass(), null);
        this.resource = item;
    }

    @Override
    public T getItem() {
        return this.resource;
    }
}