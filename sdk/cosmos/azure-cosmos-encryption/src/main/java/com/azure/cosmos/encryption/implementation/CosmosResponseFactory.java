// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosItemResponseHelper.CosmosItemResponseBuilderAccessor;
import com.azure.cosmos.models.CosmosItemResponse;

public class CosmosResponseFactory {
    CosmosItemResponseBuilderAccessor cosmosItemResponseBuilderAccessor;

    public CosmosResponseFactory() {
        cosmosItemResponseBuilderAccessor =
            ImplementationBridgeHelpers.CosmosItemResponseHelper.getCosmosItemResponseBuilderAccessor();
    }

    public <T> CosmosItemResponse<T> createItemResponse(CosmosItemResponse<byte[]> responseMessage,
                                                        Class<T> classType,
                                                        CosmosItemSerializer effectiveItemSerializer) {
        return cosmosItemResponseBuilderAccessor.createCosmosItemResponse(
            responseMessage,
            classType,
            effectiveItemSerializer);
    }
}
