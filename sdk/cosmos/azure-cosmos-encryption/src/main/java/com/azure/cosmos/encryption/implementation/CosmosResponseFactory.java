// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.encryption.models.EncryptionModelBridgeInternal;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;

public class CosmosResponseFactory {
    public <T> CosmosItemResponse<T> createItemResponse(CosmosItemResponse<byte[]> responseMessage, Class<T> classType) {
        return ModelBridgeInternal.createCosmosItemResponse(
            ModelBridgeInternal.getResourceResponse(responseMessage),
            ModelBridgeInternal.getByteArrayContent(responseMessage),
            classType,
            new ItemDeserializer.JsonDeserializer());
    }
}
