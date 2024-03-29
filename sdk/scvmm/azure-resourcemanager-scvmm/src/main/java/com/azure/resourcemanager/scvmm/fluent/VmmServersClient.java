// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.scvmm.fluent;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.management.polling.PollResult;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.scvmm.fluent.models.VmmServerInner;
import com.azure.resourcemanager.scvmm.models.ResourcePatch;

/** An instance of this class provides access to all the operations defined in VmmServersClient. */
public interface VmmServersClient {
    /**
     * Implements VMMServer GET method.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the VmmServers resource definition.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    VmmServerInner getByResourceGroup(String resourceGroupName, String vmmServerName);

    /**
     * Implements VMMServer GET method.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the VmmServers resource definition along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<VmmServerInner> getByResourceGroupWithResponse(
        String resourceGroupName, String vmmServerName, Context context);

    /**
     * Onboards the SCVMM fabric as an Azure VmmServer resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param body Request payload.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the VmmServers resource definition.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<VmmServerInner>, VmmServerInner> beginCreateOrUpdate(
        String resourceGroupName, String vmmServerName, VmmServerInner body);

    /**
     * Onboards the SCVMM fabric as an Azure VmmServer resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param body Request payload.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the VmmServers resource definition.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<VmmServerInner>, VmmServerInner> beginCreateOrUpdate(
        String resourceGroupName, String vmmServerName, VmmServerInner body, Context context);

    /**
     * Onboards the SCVMM fabric as an Azure VmmServer resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param body Request payload.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the VmmServers resource definition.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    VmmServerInner createOrUpdate(String resourceGroupName, String vmmServerName, VmmServerInner body);

    /**
     * Onboards the SCVMM fabric as an Azure VmmServer resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param body Request payload.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the VmmServers resource definition.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    VmmServerInner createOrUpdate(String resourceGroupName, String vmmServerName, VmmServerInner body, Context context);

    /**
     * Deboards the SCVMM fabric from Azure.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param force Forces the resource to be deleted from azure. The corresponding CR would be attempted to be deleted
     *     too.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<Void>, Void> beginDelete(String resourceGroupName, String vmmServerName, Boolean force);

    /**
     * Deboards the SCVMM fabric from Azure.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param force Forces the resource to be deleted from azure. The corresponding CR would be attempted to be deleted
     *     too.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<Void>, Void> beginDelete(
        String resourceGroupName, String vmmServerName, Boolean force, Context context);

    /**
     * Deboards the SCVMM fabric from Azure.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param force Forces the resource to be deleted from azure. The corresponding CR would be attempted to be deleted
     *     too.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void delete(String resourceGroupName, String vmmServerName, Boolean force);

    /**
     * Deboards the SCVMM fabric from Azure.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void delete(String resourceGroupName, String vmmServerName);

    /**
     * Deboards the SCVMM fabric from Azure.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param force Forces the resource to be deleted from azure. The corresponding CR would be attempted to be deleted
     *     too.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void delete(String resourceGroupName, String vmmServerName, Boolean force, Context context);

    /**
     * Updates the VmmServers resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param body VmmServers patch payload.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the VmmServers resource definition.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<VmmServerInner>, VmmServerInner> beginUpdate(
        String resourceGroupName, String vmmServerName, ResourcePatch body);

    /**
     * Updates the VmmServers resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param body VmmServers patch payload.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of the VmmServers resource definition.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<VmmServerInner>, VmmServerInner> beginUpdate(
        String resourceGroupName, String vmmServerName, ResourcePatch body, Context context);

    /**
     * Updates the VmmServers resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param body VmmServers patch payload.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the VmmServers resource definition.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    VmmServerInner update(String resourceGroupName, String vmmServerName, ResourcePatch body);

    /**
     * Updates the VmmServers resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmmServerName Name of the VMMServer.
     * @param body VmmServers patch payload.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the VmmServers resource definition.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    VmmServerInner update(String resourceGroupName, String vmmServerName, ResourcePatch body, Context context);

    /**
     * List of VmmServers in a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return list of VmmServers as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<VmmServerInner> listByResourceGroup(String resourceGroupName);

    /**
     * List of VmmServers in a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return list of VmmServers as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<VmmServerInner> listByResourceGroup(String resourceGroupName, Context context);

    /**
     * List of VmmServers in a subscription.
     *
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return list of VmmServers as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<VmmServerInner> list();

    /**
     * List of VmmServers in a subscription.
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return list of VmmServers as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<VmmServerInner> list(Context context);
}
