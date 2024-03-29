// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.largeinstance.fluent;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.management.polling.PollResult;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.largeinstance.fluent.models.AzureLargeInstanceInner;
import com.azure.resourcemanager.largeinstance.fluent.models.OperationStatusResultInner;
import com.azure.resourcemanager.largeinstance.models.AzureLargeInstanceTagsUpdate;
import com.azure.resourcemanager.largeinstance.models.ForceState;

/**
 * An instance of this class provides access to all the operations defined in AzureLargeInstancesClient.
 */
public interface AzureLargeInstancesClient {
    /**
     * Gets a list of Azure Large Instances in the specified subscription. The
     * operations returns various properties of each Azure Large Instance.
     * 
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of Azure Large Instances in the specified subscription as paginated response with
     * {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<AzureLargeInstanceInner> list();

    /**
     * Gets a list of Azure Large Instances in the specified subscription. The
     * operations returns various properties of each Azure Large Instance.
     * 
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of Azure Large Instances in the specified subscription as paginated response with
     * {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<AzureLargeInstanceInner> list(Context context);

    /**
     * Gets a list of Azure Large Instances in the specified subscription and resource
     * group. The operations returns various properties of each Azure Large Instance.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of Azure Large Instances in the specified subscription and resource
     * group as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<AzureLargeInstanceInner> listByResourceGroup(String resourceGroupName);

    /**
     * Gets a list of Azure Large Instances in the specified subscription and resource
     * group. The operations returns various properties of each Azure Large Instance.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of Azure Large Instances in the specified subscription and resource
     * group as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<AzureLargeInstanceInner> listByResourceGroup(String resourceGroupName, Context context);

    /**
     * Gets an Azure Large Instance for the specified subscription, resource group,
     * and instance name.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an Azure Large Instance for the specified subscription, resource group,
     * and instance name along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<AzureLargeInstanceInner> getByResourceGroupWithResponse(String resourceGroupName,
        String azureLargeInstanceName, Context context);

    /**
     * Gets an Azure Large Instance for the specified subscription, resource group,
     * and instance name.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an Azure Large Instance for the specified subscription, resource group,
     * and instance name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    AzureLargeInstanceInner getByResourceGroup(String resourceGroupName, String azureLargeInstanceName);

    /**
     * Patches the Tags field of an Azure Large Instance for the specified
     * subscription, resource group, and instance name.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @param properties The resource properties to be updated.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return azure Large Instance info on Azure (ARM properties and AzureLargeInstance
     * properties) along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<AzureLargeInstanceInner> updateWithResponse(String resourceGroupName, String azureLargeInstanceName,
        AzureLargeInstanceTagsUpdate properties, Context context);

    /**
     * Patches the Tags field of an Azure Large Instance for the specified
     * subscription, resource group, and instance name.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @param properties The resource properties to be updated.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return azure Large Instance info on Azure (ARM properties and AzureLargeInstance
     * properties).
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    AzureLargeInstanceInner update(String resourceGroupName, String azureLargeInstanceName,
        AzureLargeInstanceTagsUpdate properties);

    /**
     * The operation to restart an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner>
        beginRestart(String resourceGroupName, String azureLargeInstanceName);

    /**
     * The operation to restart an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @param forceParameter When set to 'active', this parameter empowers the server with the ability to forcefully
     * terminate and halt any existing processes that may be running on the server.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner> beginRestart(
        String resourceGroupName, String azureLargeInstanceName, ForceState forceParameter, Context context);

    /**
     * The operation to restart an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner restart(String resourceGroupName, String azureLargeInstanceName);

    /**
     * The operation to restart an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @param forceParameter When set to 'active', this parameter empowers the server with the ability to forcefully
     * terminate and halt any existing processes that may be running on the server.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner restart(String resourceGroupName, String azureLargeInstanceName,
        ForceState forceParameter, Context context);

    /**
     * The operation to shutdown an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner>
        beginShutdown(String resourceGroupName, String azureLargeInstanceName);

    /**
     * The operation to shutdown an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner>
        beginShutdown(String resourceGroupName, String azureLargeInstanceName, Context context);

    /**
     * The operation to shutdown an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner shutdown(String resourceGroupName, String azureLargeInstanceName);

    /**
     * The operation to shutdown an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner shutdown(String resourceGroupName, String azureLargeInstanceName, Context context);

    /**
     * The operation to start an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner> beginStart(String resourceGroupName,
        String azureLargeInstanceName);

    /**
     * The operation to start an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner> beginStart(String resourceGroupName,
        String azureLargeInstanceName, Context context);

    /**
     * The operation to start an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner start(String resourceGroupName, String azureLargeInstanceName);

    /**
     * The operation to start an Azure Large Instance (only for compute instances).
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param azureLargeInstanceName Name of the AzureLargeInstance.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the current status of an async operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner start(String resourceGroupName, String azureLargeInstanceName, Context context);
}
