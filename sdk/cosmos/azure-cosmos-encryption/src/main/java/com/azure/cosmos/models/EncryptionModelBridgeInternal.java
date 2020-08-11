// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.Warning;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.Warning.INTERNAL_USE_ONLY_WARNING;

/**
 * This is an internal class in the encryption project.
 * This is meant to be used only internally as a bridge access to classes in implementation.
 */
@Warning(value = INTERNAL_USE_ONLY_WARNING)
public class EncryptionModelBridgeInternal {

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> CosmosItemResponse<T> createCosmosItemResponse(ResourceResponse<Document> response, byte[] contentAsByteArray, Class<T> classType, ItemDeserializer itemDeserializer) {
        return new CosmosItemResponse<>(response, contentAsByteArray, classType, itemDeserializer);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static byte[] getByteArrayContent(CosmosItemResponse response) {
        return response.responseBodyAsByteArray;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setByteArrayContent(CosmosItemResponse response, byte[] content) {
        response.responseBodyAsByteArray = content;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ResourceResponse<Document> getResourceResponse(CosmosItemResponse response) {
        return response.resourceResponse;
    }
}
