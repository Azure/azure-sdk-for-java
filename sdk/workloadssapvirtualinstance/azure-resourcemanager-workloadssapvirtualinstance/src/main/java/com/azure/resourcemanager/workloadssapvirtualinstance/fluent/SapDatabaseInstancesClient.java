// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.workloadssapvirtualinstance.fluent;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.management.polling.PollResult;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.workloadssapvirtualinstance.fluent.models.OperationStatusResultInner;
import com.azure.resourcemanager.workloadssapvirtualinstance.fluent.models.SapDatabaseInstanceInner;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StartRequest;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StopRequest;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.UpdateSapDatabaseInstanceRequest;

/**
 * An instance of this class provides access to all the operations defined in SapDatabaseInstancesClient.
 */
public interface SapDatabaseInstancesClient {
    /**
     * Gets the SAP Database Instance resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the SAP Database Instance resource along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<SapDatabaseInstanceInner> getWithResponse(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName, Context context);

    /**
     * Gets the SAP Database Instance resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the SAP Database Instance resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    SapDatabaseInstanceInner get(String resourceGroupName, String sapVirtualInstanceName, String databaseInstanceName);

    /**
     * Creates the Database resource corresponding to the Virtual Instance for SAP solutions resource.
     * &amp;lt;br&amp;gt;&amp;lt;br&amp;gt;This will be used by service only. PUT by end user will return a Bad Request
     * error.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param resource Request body of Database resource of a SAP system.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of define the Database resource.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<SapDatabaseInstanceInner>, SapDatabaseInstanceInner> beginCreate(String resourceGroupName,
        String sapVirtualInstanceName, String databaseInstanceName, SapDatabaseInstanceInner resource);

    /**
     * Creates the Database resource corresponding to the Virtual Instance for SAP solutions resource.
     * &amp;lt;br&amp;gt;&amp;lt;br&amp;gt;This will be used by service only. PUT by end user will return a Bad Request
     * error.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param resource Request body of Database resource of a SAP system.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of define the Database resource.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<SapDatabaseInstanceInner>, SapDatabaseInstanceInner> beginCreate(String resourceGroupName,
        String sapVirtualInstanceName, String databaseInstanceName, SapDatabaseInstanceInner resource, Context context);

    /**
     * Creates the Database resource corresponding to the Virtual Instance for SAP solutions resource.
     * &amp;lt;br&amp;gt;&amp;lt;br&amp;gt;This will be used by service only. PUT by end user will return a Bad Request
     * error.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param resource Request body of Database resource of a SAP system.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return define the Database resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    SapDatabaseInstanceInner create(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName, SapDatabaseInstanceInner resource);

    /**
     * Creates the Database resource corresponding to the Virtual Instance for SAP solutions resource.
     * &amp;lt;br&amp;gt;&amp;lt;br&amp;gt;This will be used by service only. PUT by end user will return a Bad Request
     * error.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param resource Request body of Database resource of a SAP system.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return define the Database resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    SapDatabaseInstanceInner create(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName, SapDatabaseInstanceInner resource, Context context);

    /**
     * Updates the Database resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param properties Database resource update request body.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return define the Database resource along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<SapDatabaseInstanceInner> updateWithResponse(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName, UpdateSapDatabaseInstanceRequest properties, Context context);

    /**
     * Updates the Database resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param properties Database resource update request body.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return define the Database resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    SapDatabaseInstanceInner update(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName, UpdateSapDatabaseInstanceRequest properties);

    /**
     * Deletes the Database resource corresponding to a Virtual Instance for SAP solutions resource.
     * &amp;lt;br&amp;gt;&amp;lt;br&amp;gt;This will be used by service only. Delete by end user will return a Bad
     * Request error.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<Void>, Void> beginDelete(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName);

    /**
     * Deletes the Database resource corresponding to a Virtual Instance for SAP solutions resource.
     * &amp;lt;br&amp;gt;&amp;lt;br&amp;gt;This will be used by service only. Delete by end user will return a Bad
     * Request error.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<Void>, Void> beginDelete(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName, Context context);

    /**
     * Deletes the Database resource corresponding to a Virtual Instance for SAP solutions resource.
     * &amp;lt;br&amp;gt;&amp;lt;br&amp;gt;This will be used by service only. Delete by end user will return a Bad
     * Request error.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void delete(String resourceGroupName, String sapVirtualInstanceName, String databaseInstanceName);

    /**
     * Deletes the Database resource corresponding to a Virtual Instance for SAP solutions resource.
     * &amp;lt;br&amp;gt;&amp;lt;br&amp;gt;This will be used by service only. Delete by end user will return a Bad
     * Request error.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void delete(String resourceGroupName, String sapVirtualInstanceName, String databaseInstanceName, Context context);

    /**
     * Lists the Database resources associated with a Virtual Instance for SAP solutions resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response of a SAPDatabaseInstance list operation as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<SapDatabaseInstanceInner> list(String resourceGroupName, String sapVirtualInstanceName);

    /**
     * Lists the Database resources associated with a Virtual Instance for SAP solutions resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response of a SAPDatabaseInstance list operation as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<SapDatabaseInstanceInner> list(String resourceGroupName, String sapVirtualInstanceName,
        Context context);

    /**
     * Starts the database instance of the SAP system.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner> beginStart(String resourceGroupName,
        String sapVirtualInstanceName, String databaseInstanceName);

    /**
     * Starts the database instance of the SAP system.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param body SAP Database server instance start request body.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner> beginStart(String resourceGroupName,
        String sapVirtualInstanceName, String databaseInstanceName, StartRequest body, Context context);

    /**
     * Starts the database instance of the SAP system.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner start(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName);

    /**
     * Starts the database instance of the SAP system.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param body SAP Database server instance start request body.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner start(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName, StartRequest body, Context context);

    /**
     * Stops the database instance of the SAP system.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner> beginStop(String resourceGroupName,
        String sapVirtualInstanceName, String databaseInstanceName);

    /**
     * Stops the database instance of the SAP system.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param body Stop request for the database instance of the SAP system.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<OperationStatusResultInner>, OperationStatusResultInner> beginStop(String resourceGroupName,
        String sapVirtualInstanceName, String databaseInstanceName, StopRequest body, Context context);

    /**
     * Stops the database instance of the SAP system.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner stop(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName);

    /**
     * Stops the database instance of the SAP system.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param sapVirtualInstanceName The name of the Virtual Instances for SAP solutions resource.
     * @param databaseInstanceName Database resource name string modeled as parameter for auto generation to work
     * correctly.
     * @param body Stop request for the database instance of the SAP system.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    OperationStatusResultInner stop(String resourceGroupName, String sapVirtualInstanceName,
        String databaseInstanceName, StopRequest body, Context context);
}
