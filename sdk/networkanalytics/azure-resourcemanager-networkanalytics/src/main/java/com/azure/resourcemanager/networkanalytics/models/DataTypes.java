// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.networkanalytics.models;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Resource collection API of DataTypes.
 */
public interface DataTypes {
    /**
     * List data type by parent resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response of a DataType list operation as paginated response with {@link PagedIterable}.
     */
    PagedIterable<DataType> listByDataProduct(String resourceGroupName, String dataProductName);

    /**
     * List data type by parent resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response of a DataType list operation as paginated response with {@link PagedIterable}.
     */
    PagedIterable<DataType> listByDataProduct(String resourceGroupName, String dataProductName, Context context);

    /**
     * Retrieve data type resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @param dataTypeName The data type name.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the data type resource along with {@link Response}.
     */
    Response<DataType> getWithResponse(String resourceGroupName, String dataProductName, String dataTypeName,
        Context context);

    /**
     * Retrieve data type resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @param dataTypeName The data type name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the data type resource.
     */
    DataType get(String resourceGroupName, String dataProductName, String dataTypeName);

    /**
     * Delete data type resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @param dataTypeName The data type name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void delete(String resourceGroupName, String dataProductName, String dataTypeName);

    /**
     * Delete data type resource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @param dataTypeName The data type name.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void delete(String resourceGroupName, String dataProductName, String dataTypeName, Context context);

    /**
     * Delete data for data type.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @param dataTypeName The data type name.
     * @param body The content of the action request.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void deleteData(String resourceGroupName, String dataProductName, String dataTypeName, Object body);

    /**
     * Delete data for data type.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @param dataTypeName The data type name.
     * @param body The content of the action request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void deleteData(String resourceGroupName, String dataProductName, String dataTypeName, Object body,
        Context context);

    /**
     * Generate sas token for storage container.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @param dataTypeName The data type name.
     * @param body The content of the action request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return details of storage container account sas token along with {@link Response}.
     */
    Response<ContainerSasToken> generateStorageContainerSasTokenWithResponse(String resourceGroupName,
        String dataProductName, String dataTypeName, ContainerSaS body, Context context);

    /**
     * Generate sas token for storage container.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param dataProductName The data product resource name.
     * @param dataTypeName The data type name.
     * @param body The content of the action request.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return details of storage container account sas token.
     */
    ContainerSasToken generateStorageContainerSasToken(String resourceGroupName, String dataProductName,
        String dataTypeName, ContainerSaS body);

    /**
     * Retrieve data type resource.
     * 
     * @param id the resource ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the data type resource along with {@link Response}.
     */
    DataType getById(String id);

    /**
     * Retrieve data type resource.
     * 
     * @param id the resource ID.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the data type resource along with {@link Response}.
     */
    Response<DataType> getByIdWithResponse(String id, Context context);

    /**
     * Delete data type resource.
     * 
     * @param id the resource ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void deleteById(String id);

    /**
     * Delete data type resource.
     * 
     * @param id the resource ID.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void deleteByIdWithResponse(String id, Context context);

    /**
     * Begins definition for a new DataType resource.
     * 
     * @param name resource name.
     * @return the first stage of the new DataType definition.
     */
    DataType.DefinitionStages.Blank define(String name);
}
