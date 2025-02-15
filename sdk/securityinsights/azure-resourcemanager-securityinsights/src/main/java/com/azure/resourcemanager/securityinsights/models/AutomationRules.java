// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.securityinsights.models;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Resource collection API of AutomationRules.
 */
public interface AutomationRules {
    /**
     * Gets the automation rule.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The name of the workspace.
     * @param automationRuleId Automation rule ID.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the automation rule along with {@link Response}.
     */
    Response<AutomationRule> getWithResponse(String resourceGroupName, String workspaceName, String automationRuleId,
        Context context);

    /**
     * Gets the automation rule.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The name of the workspace.
     * @param automationRuleId Automation rule ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the automation rule.
     */
    AutomationRule get(String resourceGroupName, String workspaceName, String automationRuleId);

    /**
     * Delete the automation rule.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The name of the workspace.
     * @param automationRuleId Automation rule ID.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object along with {@link Response}.
     */
    Response<Object> deleteWithResponse(String resourceGroupName, String workspaceName, String automationRuleId,
        Context context);

    /**
     * Delete the automation rule.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The name of the workspace.
     * @param automationRuleId Automation rule ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    Object delete(String resourceGroupName, String workspaceName, String automationRuleId);

    /**
     * Gets all automation rules.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The name of the workspace.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return all automation rules as paginated response with {@link PagedIterable}.
     */
    PagedIterable<AutomationRule> list(String resourceGroupName, String workspaceName);

    /**
     * Gets all automation rules.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param workspaceName The name of the workspace.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return all automation rules as paginated response with {@link PagedIterable}.
     */
    PagedIterable<AutomationRule> list(String resourceGroupName, String workspaceName, Context context);

    /**
     * Gets the automation rule.
     * 
     * @param id the resource ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the automation rule along with {@link Response}.
     */
    AutomationRule getById(String id);

    /**
     * Gets the automation rule.
     * 
     * @param id the resource ID.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the automation rule along with {@link Response}.
     */
    Response<AutomationRule> getByIdWithResponse(String id, Context context);

    /**
     * Delete the automation rule.
     * 
     * @param id the resource ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object along with {@link Response}.
     */
    Object deleteById(String id);

    /**
     * Delete the automation rule.
     * 
     * @param id the resource ID.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object along with {@link Response}.
     */
    Response<Object> deleteByIdWithResponse(String id, Context context);

    /**
     * Begins definition for a new AutomationRule resource.
     * 
     * @param name resource name.
     * @return the first stage of the new AutomationRule definition.
     */
    AutomationRule.DefinitionStages.Blank define(String name);
}
