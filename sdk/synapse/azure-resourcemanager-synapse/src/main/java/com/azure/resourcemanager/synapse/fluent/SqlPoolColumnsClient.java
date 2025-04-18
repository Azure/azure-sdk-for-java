// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.synapse.fluent;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.resourcemanager.synapse.fluent.models.SqlPoolColumnInner;

/**
 * An instance of this class provides access to all the operations defined in SqlPoolColumnsClient.
 */
public interface SqlPoolColumnsClient {
    /**
     * Get Sql pool column.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The name of the workspace.
     * @param sqlPoolName SQL pool name.
     * @param schemaName The name of the schema.
     * @param tableName The name of the table.
     * @param columnName The name of the column.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return sql pool column along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<SqlPoolColumnInner> getWithResponse(String resourceGroupName, String workspaceName, String sqlPoolName,
        String schemaName, String tableName, String columnName, Context context);

    /**
     * Get Sql pool column.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The name of the workspace.
     * @param sqlPoolName SQL pool name.
     * @param schemaName The name of the schema.
     * @param tableName The name of the table.
     * @param columnName The name of the column.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return sql pool column.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    SqlPoolColumnInner get(String resourceGroupName, String workspaceName, String sqlPoolName, String schemaName,
        String tableName, String columnName);
}
