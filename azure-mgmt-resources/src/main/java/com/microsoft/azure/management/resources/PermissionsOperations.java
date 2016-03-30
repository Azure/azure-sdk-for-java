/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.resources.models.PageImpl;
import com.microsoft.azure.management.resources.models.Permission;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in PermissionsOperations.
 */
public interface PermissionsOperations {
    /**
     * Gets a resource group permissions.
     *
     * @param resourceGroupName Name of the resource group to get the permissions for.The name is case insensitive.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;Permission&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<Permission>> listForResourceGroup(final String resourceGroupName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets a resource group permissions.
     *
     * @param resourceGroupName Name of the resource group to get the permissions for.The name is case insensitive.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceGroupAsync(final String resourceGroupName, final ListOperationCallback<Permission> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets a resource permissions.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param resourceProviderNamespace Resource
     * @param parentResourcePath Resource
     * @param resourceType Resource
     * @param resourceName Resource
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;Permission&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<Permission>> listForResource(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets a resource permissions.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param resourceProviderNamespace Resource
     * @param parentResourcePath Resource
     * @param resourceType Resource
     * @param resourceName Resource
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceAsync(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final ListOperationCallback<Permission> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets a resource group permissions.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;Permission&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<Permission>> listForResourceGroupNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets a resource group permissions.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceGroupNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<Permission> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets a resource permissions.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;Permission&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<Permission>> listForResourceNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets a resource permissions.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<Permission> serviceCallback) throws IllegalArgumentException;

}
