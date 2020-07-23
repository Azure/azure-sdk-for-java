// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.models.CosmosItemResponse;

public class CosmosResponseFactoryCore extends CosmosResponseFactoryInternal {

    @Override
    public <T> CosmosItemResponse<T> createItemResponse(CosmosItemResponse<byte[]> responseMessage, Class<T> classType) {
        return new CosmosItemResponse(
            responseMessage.resourceResponse,
            responseMessage.responseBodyAsByteArray,
            classType,
            new ItemDeserializer.JsonDeserializer());
    }
}
