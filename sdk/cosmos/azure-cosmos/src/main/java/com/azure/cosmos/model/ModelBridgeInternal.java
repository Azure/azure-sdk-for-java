// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.model;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ResourceResponse;

import java.util.List;

/**
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.cosmos.model package
 **/
public class ModelBridgeInternal {

    public static CosmosAsyncConflictResponse createCosmosAsyncConflictResponse(ResourceResponse<Conflict> response,
                                                                                CosmosAsyncContainer container) {
        return new CosmosAsyncConflictResponse(response, container);
    }

    public static CosmosAsyncContainerResponse createCosmosAsyncContainerResponse(ResourceResponse<DocumentCollection> response,
                                                                                  CosmosAsyncDatabase database) {
        return new CosmosAsyncContainerResponse(response, database);
    }

    public static List<CosmosConflictProperties> getCosmosConflictPropertiesFromV2Results(List<Conflict> results) {
        return CosmosConflictProperties.getFromV2Results(results);
    }

    public static DocumentCollection getV2Collection(CosmosContainerProperties containerProperties) {
        return containerProperties.getV2Collection();
    }

    public static List<CosmosContainerProperties> getCosmosContainerPropertiesFromV2Results(List<DocumentCollection> results) {
        return CosmosContainerProperties.getFromV2Results(results);
    }
}
