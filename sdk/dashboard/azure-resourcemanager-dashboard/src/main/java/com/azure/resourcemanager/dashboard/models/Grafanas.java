// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.dashboard.models;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Resource collection API of Grafanas.
 */
public interface Grafanas {
    /**
     * Get the properties of a specific workspace for Grafana resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The workspace name of Azure Managed Grafana.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a specific workspace for Grafana resource along with {@link Response}.
     */
    Response<ManagedGrafana> getByResourceGroupWithResponse(String resourceGroupName, String workspaceName,
        Context context);

    /**
     * Get the properties of a specific workspace for Grafana resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The workspace name of Azure Managed Grafana.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a specific workspace for Grafana resource.
     */
    ManagedGrafana getByResourceGroup(String resourceGroupName, String workspaceName);

    /**
     * Delete a workspace for Grafana resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The workspace name of Azure Managed Grafana.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void deleteByResourceGroup(String resourceGroupName, String workspaceName);

    /**
     * Delete a workspace for Grafana resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The workspace name of Azure Managed Grafana.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void delete(String resourceGroupName, String workspaceName, Context context);

    /**
     * List all resources of workspaces for Grafana under the specified resource group.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged collection of ManagedGrafana items as paginated response with {@link PagedIterable}.
     */
    PagedIterable<ManagedGrafana> listByResourceGroup(String resourceGroupName);

    /**
     * List all resources of workspaces for Grafana under the specified resource group.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged collection of ManagedGrafana items as paginated response with {@link PagedIterable}.
     */
    PagedIterable<ManagedGrafana> listByResourceGroup(String resourceGroupName, Context context);

    /**
     * List all resources of workspaces for Grafana under the specified subscription.
     * 
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged collection of ManagedGrafana items as paginated response with {@link PagedIterable}.
     */
    PagedIterable<ManagedGrafana> list();

    /**
     * List all resources of workspaces for Grafana under the specified subscription.
     * 
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged collection of ManagedGrafana items as paginated response with {@link PagedIterable}.
     */
    PagedIterable<ManagedGrafana> list(Context context);

    /**
     * Retrieve enterprise add-on details information.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The workspace name of Azure Managed Grafana.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return enterprise details of a Grafana instance along with {@link Response}.
     */
    Response<EnterpriseDetails> checkEnterpriseDetailsWithResponse(String resourceGroupName, String workspaceName,
        Context context);

    /**
     * Retrieve enterprise add-on details information.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The workspace name of Azure Managed Grafana.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return enterprise details of a Grafana instance.
     */
    EnterpriseDetails checkEnterpriseDetails(String resourceGroupName, String workspaceName);

    /**
     * A synchronous resource action.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The workspace name of Azure Managed Grafana.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body along with {@link Response}.
     */
    Response<GrafanaAvailablePluginListResponse> fetchAvailablePluginsWithResponse(String resourceGroupName,
        String workspaceName, Context context);

    /**
     * A synchronous resource action.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The workspace name of Azure Managed Grafana.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    GrafanaAvailablePluginListResponse fetchAvailablePlugins(String resourceGroupName, String workspaceName);

    /**
     * Get the properties of a specific workspace for Grafana resource.
     * 
     * @param id the resource ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a specific workspace for Grafana resource along with {@link Response}.
     */
    ManagedGrafana getById(String id);

    /**
     * Get the properties of a specific workspace for Grafana resource.
     * 
     * @param id the resource ID.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a specific workspace for Grafana resource along with {@link Response}.
     */
    Response<ManagedGrafana> getByIdWithResponse(String id, Context context);

    /**
     * Delete a workspace for Grafana resource.
     * 
     * @param id the resource ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void deleteById(String id);

    /**
     * Delete a workspace for Grafana resource.
     * 
     * @param id the resource ID.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void deleteByIdWithResponse(String id, Context context);

    /**
     * Begins definition for a new ManagedGrafana resource.
     * 
     * @param name resource name.
     * @return the first stage of the new ManagedGrafana definition.
     */
    ManagedGrafana.DefinitionStages.Blank define(String name);
}
