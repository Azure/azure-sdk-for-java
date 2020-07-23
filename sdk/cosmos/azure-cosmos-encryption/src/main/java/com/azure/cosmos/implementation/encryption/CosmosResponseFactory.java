// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.models.CosmosItemResponse;

/**
 * This response factory converts response messages
 * to the corresponding type response using the
 * CosmosClient serializer
 */
public interface CosmosResponseFactory {
    <T> CosmosItemResponse<T> createItemResponse(CosmosItemResponse<byte[]> responseMessage, Class<T> classType);
}
