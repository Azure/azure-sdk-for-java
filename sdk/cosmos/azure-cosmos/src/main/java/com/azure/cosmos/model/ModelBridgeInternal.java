// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.model;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.Permission;
import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
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

    public static CosmosAsyncDatabaseResponse createCosmosAsyncDatabaseResponse(ResourceResponse<Database> response,
                                                                                 CosmosAsyncClient client) {
        return new CosmosAsyncDatabaseResponse(response, client);
    }

    public static <T> CosmosAsyncItemResponse<T> createCosmosAsyncItemResponse(ResourceResponse<Document> response, Class<T> classType) {
        return new CosmosAsyncItemResponse<>(response, classType);
    }

    public static CosmosAsyncItemResponse createCosmosAsyncItemResponseWithObjectType(ResourceResponse<Document> response, Class classType) {
        return new CosmosAsyncItemResponse(response, classType);
    }

    public static CosmosAsyncPermissionResponse createCosmosAsyncPermissionResponse(ResourceResponse<Permission> response,
                                                                                    CosmosAsyncUser cosmosUser) {
        return new CosmosAsyncPermissionResponse(response, cosmosUser);
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

    public static List<CosmosDatabaseProperties> getCosmosDatabasePropertiesFromV2Results(List<Database> results) {
        return CosmosDatabaseProperties.getFromV2Results(results);
    }

    public static <T> CosmosItemProperties getProperties(CosmosAsyncItemResponse<T> cosmosItemResponse) {
        return cosmosItemResponse.getProperties();
    }

    public static Permission getV2Permissions(CosmosPermissionProperties permissionSettings) {
        return permissionSettings.getV2Permissions();
    }

    public static List<CosmosPermissionProperties> getCosmosPermissionPropertiesFromV2Results(List<Permission> results) {
        return CosmosPermissionProperties.getFromV2Results(results);
    }
}
