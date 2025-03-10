// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.models;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Resource collection API of SoftwareUpdateConfigurations.
 */
public interface SoftwareUpdateConfigurations {
    /**
     * Get a single software update configuration by name.
     * 
     * @param resourceGroupName Name of an Azure Resource group.
     * @param automationAccountName The name of the automation account.
     * @param softwareUpdateConfigurationName The name of the software update configuration to be created.
     * @param clientRequestId Identifies this specific client request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a single software update configuration by name along with {@link Response}.
     */
    Response<SoftwareUpdateConfiguration> getByNameWithResponse(String resourceGroupName, String automationAccountName,
        String softwareUpdateConfigurationName, String clientRequestId, Context context);

    /**
     * Get a single software update configuration by name.
     * 
     * @param resourceGroupName Name of an Azure Resource group.
     * @param automationAccountName The name of the automation account.
     * @param softwareUpdateConfigurationName The name of the software update configuration to be created.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a single software update configuration by name.
     */
    SoftwareUpdateConfiguration getByName(String resourceGroupName, String automationAccountName,
        String softwareUpdateConfigurationName);

    /**
     * delete a specific software update configuration.
     * 
     * @param resourceGroupName Name of an Azure Resource group.
     * @param automationAccountName The name of the automation account.
     * @param softwareUpdateConfigurationName The name of the software update configuration to be created.
     * @param clientRequestId Identifies this specific client request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link Response}.
     */
    Response<Void> deleteWithResponse(String resourceGroupName, String automationAccountName,
        String softwareUpdateConfigurationName, String clientRequestId, Context context);

    /**
     * delete a specific software update configuration.
     * 
     * @param resourceGroupName Name of an Azure Resource group.
     * @param automationAccountName The name of the automation account.
     * @param softwareUpdateConfigurationName The name of the software update configuration to be created.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void delete(String resourceGroupName, String automationAccountName, String softwareUpdateConfigurationName);

    /**
     * Get all software update configurations for the account.
     * 
     * @param resourceGroupName Name of an Azure Resource group.
     * @param automationAccountName The name of the automation account.
     * @param clientRequestId Identifies this specific client request.
     * @param filter The filter to apply on the operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return all software update configurations for the account along with {@link Response}.
     */
    Response<SoftwareUpdateConfigurationListResult> listWithResponse(String resourceGroupName,
        String automationAccountName, String clientRequestId, String filter, Context context);

    /**
     * Get all software update configurations for the account.
     * 
     * @param resourceGroupName Name of an Azure Resource group.
     * @param automationAccountName The name of the automation account.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return all software update configurations for the account.
     */
    SoftwareUpdateConfigurationListResult list(String resourceGroupName, String automationAccountName);

    /**
     * Get a single software update configuration by name.
     * 
     * @param id the resource ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a single software update configuration by name along with {@link Response}.
     */
    SoftwareUpdateConfiguration getById(String id);

    /**
     * Get a single software update configuration by name.
     * 
     * @param id the resource ID.
     * @param clientRequestId Identifies this specific client request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a single software update configuration by name along with {@link Response}.
     */
    Response<SoftwareUpdateConfiguration> getByIdWithResponse(String id, String clientRequestId, Context context);

    /**
     * delete a specific software update configuration.
     * 
     * @param id the resource ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void deleteById(String id);

    /**
     * delete a specific software update configuration.
     * 
     * @param id the resource ID.
     * @param clientRequestId Identifies this specific client request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link Response}.
     */
    Response<Void> deleteByIdWithResponse(String id, String clientRequestId, Context context);

    /**
     * Begins definition for a new SoftwareUpdateConfiguration resource.
     * 
     * @param name resource name.
     * @return the first stage of the new SoftwareUpdateConfiguration definition.
     */
    SoftwareUpdateConfiguration.DefinitionStages.Blank define(String name);
}
