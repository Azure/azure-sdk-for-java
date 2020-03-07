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
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.implementation.StoredProcedureResponse;
import com.azure.cosmos.implementation.Trigger;
import com.azure.cosmos.implementation.UserDefinedFunction;

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

    public static CosmosAsyncStoredProcedureResponse createCosmosAsyncStoredProcedureResponse(ResourceResponse<StoredProcedure> response,
                                                                                              CosmosAsyncContainer cosmosContainer) {
        return new CosmosAsyncStoredProcedureResponse(response, cosmosContainer);
    }

    public static CosmosAsyncStoredProcedureResponse createCosmosAsyncStoredProcedureResponse(StoredProcedureResponse response,
                                                                                              CosmosAsyncContainer cosmosContainer,
                                                                                              String storedProcedureId) {
        return new CosmosAsyncStoredProcedureResponse(response, cosmosContainer, storedProcedureId);
    }

    public static CosmosStoredProcedureProperties createCosmosStoredProcedureProperties(String jsonString) {
        return new CosmosStoredProcedureProperties(jsonString);
    }

    public static CosmosAsyncTriggerResponse createCosmosAsyncTriggerResponse(ResourceResponse<Trigger> response,
                                                                              CosmosAsyncContainer container) {
        return new CosmosAsyncTriggerResponse(response, container);
    }

    public static CosmosAsyncUserDefinedFunctionResponse createCosmosAsyncUserDefinedFunctionResponse(ResourceResponse<UserDefinedFunction> response,
                                                                                                      CosmosAsyncContainer container) {
        return new CosmosAsyncUserDefinedFunctionResponse(response, container);
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

    public static <T> CosmosItemProperties getCosmosItemProperties(CosmosAsyncItemResponse<T> cosmosItemResponse) {
        return cosmosItemResponse.getProperties();
    }

    public static Permission getV2Permissions(CosmosPermissionProperties permissionSettings) {
        return permissionSettings.getV2Permissions();
    }

    public static List<CosmosPermissionProperties> getCosmosPermissionPropertiesFromV2Results(List<Permission> results) {
        return CosmosPermissionProperties.getFromV2Results(results);
    }

    public static List<CosmosStoredProcedureProperties> getCosmosStoredProcedurePropertiesFromV2Results(List<StoredProcedure> results) {
        return CosmosStoredProcedureProperties.getFromV2Results(results);
    }

    public static List<CosmosTriggerProperties> getCosmosTriggerPropertiesFromV2Results(List<Trigger> results) {
        return CosmosTriggerProperties.getFromV2Results(results);
    }

    public static List<CosmosUserDefinedFunctionProperties> getCosmosUserDefinedFunctionPropertiesFromV2Results(List<UserDefinedFunction> results) {
        return CosmosUserDefinedFunctionProperties.getFromV2Results(results);
    }
}
