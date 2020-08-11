// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.EncryptionModelBridgeInternal;

public class CosmosResponseFactoryCore extends CosmosResponseFactoryInternal {

    @Override
    public <T> CosmosItemResponse<T> createItemResponse(CosmosItemResponse<byte[]> responseMessage, Class<T> classType) {
        return EncryptionModelBridgeInternal.createCosmosItemResponse(
            EncryptionModelBridgeInternal.getResourceResponse(responseMessage),
            EncryptionModelBridgeInternal.getByteArrayContent(responseMessage),
            classType,
            new ItemDeserializer.JsonDeserializer());
    }
}
