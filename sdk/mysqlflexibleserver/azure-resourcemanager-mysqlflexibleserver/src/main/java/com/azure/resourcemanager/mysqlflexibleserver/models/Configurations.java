// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.mysqlflexibleserver.models;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.fluent.models.ConfigurationInner;

/**
 * Resource collection API of Configurations.
 */
public interface Configurations {
    /**
     * Updates a configuration of a server.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serverName The name of the server.
     * @param configurationName The name of the server configuration.
     * @param parameters The required parameters for updating a server configuration.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a Configuration.
     */
    Configuration update(String resourceGroupName, String serverName, String configurationName,
        ConfigurationInner parameters);

    /**
     * Updates a configuration of a server.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serverName The name of the server.
     * @param configurationName The name of the server configuration.
     * @param parameters The required parameters for updating a server configuration.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return represents a Configuration.
     */
    Configuration update(String resourceGroupName, String serverName, String configurationName,
        ConfigurationInner parameters, Context context);

    /**
     * Gets information about a configuration of server.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serverName The name of the server.
     * @param configurationName The name of the server configuration.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return information about a configuration of server along with {@link Response}.
     */
    Response<Configuration> getWithResponse(String resourceGroupName, String serverName, String configurationName,
        Context context);

    /**
     * Gets information about a configuration of server.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serverName The name of the server.
     * @param configurationName The name of the server configuration.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return information about a configuration of server.
     */
    Configuration get(String resourceGroupName, String serverName, String configurationName);

    /**
     * Update a list of configurations in a given server.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serverName The name of the server.
     * @param parameters The parameters for updating a list of server configuration.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of server configurations.
     */
    ConfigurationListResult batchUpdate(String resourceGroupName, String serverName,
        ConfigurationListForBatchUpdate parameters);

    /**
     * Update a list of configurations in a given server.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serverName The name of the server.
     * @param parameters The parameters for updating a list of server configuration.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of server configurations.
     */
    ConfigurationListResult batchUpdate(String resourceGroupName, String serverName,
        ConfigurationListForBatchUpdate parameters, Context context);

    /**
     * List all the configurations in a given server.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serverName The name of the server.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of server configurations as paginated response with {@link PagedIterable}.
     */
    PagedIterable<Configuration> listByServer(String resourceGroupName, String serverName);

    /**
     * List all the configurations in a given server.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serverName The name of the server.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of server configurations as paginated response with {@link PagedIterable}.
     */
    PagedIterable<Configuration> listByServer(String resourceGroupName, String serverName, Context context);
}
