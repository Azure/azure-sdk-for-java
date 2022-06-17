// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.accesshelpers.CosmosItemResponseHelper;
import com.azure.cosmos.models.CosmosItemResponse;

public class CosmosResponseFactory {
    public <T> CosmosItemResponse<T> createItemResponse(CosmosItemResponse<byte[]> responseMessage,
                                                        Class<T> classType) {
        return CosmosItemResponseHelper.createCosmosItemResponse(
            CosmosItemResponseHelper.getResourceResponse(responseMessage),
            CosmosItemResponseHelper.getByteArrayContent(responseMessage),
            classType, new ItemDeserializer.JsonDeserializer());
    }
}
