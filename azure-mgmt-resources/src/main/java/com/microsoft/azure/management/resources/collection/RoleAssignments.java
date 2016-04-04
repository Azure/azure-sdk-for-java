/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.collection;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.resources.models.dto.PageImpl;
import com.microsoft.azure.management.resources.models.dto.toplevel.RoleAssignment;
import com.microsoft.azure.management.resources.models.dto.RoleAssignmentCreateParameters;
import com.microsoft.azure.management.resources.models.dto.RoleAssignmentFilter;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in RoleAssignments.
 */
public interface RoleAssignments {
    /**
     * Gets role assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleAssignment>> listForResource(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceAsync(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets role assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleAssignment>> listForResource(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final RoleAssignmentFilter filter) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceAsync(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final RoleAssignmentFilter filter, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets role assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleAssignment>> listForResourceGroup(final String resourceGroupName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceGroupAsync(final String resourceGroupName, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets role assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleAssignment>> listForResourceGroup(final String resourceGroupName, final RoleAssignmentFilter filter) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceGroupAsync(final String resourceGroupName, final RoleAssignmentFilter filter, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Delete role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleAssignment> delete(String scope, String roleAssignmentName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Delete role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String scope, String roleAssignmentName, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Create role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @param parameters Role assignment.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleAssignment> create(String scope, String roleAssignmentName, RoleAssignmentCreateParameters parameters) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Create role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @param parameters Role assignment.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall createAsync(String scope, String roleAssignmentName, RoleAssignmentCreateParameters parameters, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Get single role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleAssignment> get(String scope, String roleAssignmentName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Get single role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String scope, String roleAssignmentName, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Delete role assignment.
     *
     * @param roleAssignmentId Role assignment Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleAssignment> deleteById(String roleAssignmentId) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Delete role assignment.
     *
     * @param roleAssignmentId Role assignment Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteByIdAsync(String roleAssignmentId, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Create role assignment by Id.
     *
     * @param roleAssignmentId Role assignment Id
     * @param parameters Role assignment.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleAssignment> createById(String roleAssignmentId, RoleAssignmentCreateParameters parameters) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Create role assignment by Id.
     *
     * @param roleAssignmentId Role assignment Id
     * @param parameters Role assignment.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall createByIdAsync(String roleAssignmentId, RoleAssignmentCreateParameters parameters, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Get single role assignment.
     *
     * @param roleAssignmentId Role assignment Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleAssignment> getById(String roleAssignmentId) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Get single role assignment.
     *
     * @param roleAssignmentId Role assignment Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getByIdAsync(String roleAssignmentId, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets role assignments of the subscription.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleAssignment>> list() throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the subscription.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets role assignments of the subscription.
     *
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleAssignment>> list(final RoleAssignmentFilter filter) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the subscription.
     *
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final RoleAssignmentFilter filter, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets role assignments of the scope.
     *
     * @param scope Scope.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleAssignment>> listForScope(final String scope) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the scope.
     *
     * @param scope Scope.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForScopeAsync(final String scope, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets role assignments of the scope.
     *
     * @param scope Scope.
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleAssignment>> listForScope(final String scope, final RoleAssignmentFilter filter) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the scope.
     *
     * @param scope Scope.
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForScopeAsync(final String scope, final RoleAssignmentFilter filter, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets role assignments of the resource.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<RoleAssignment>> listForResourceNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the resource.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets role assignments of the resource group.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<RoleAssignment>> listForResourceGroupNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the resource group.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForResourceGroupNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets role assignments of the subscription.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<RoleAssignment>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the subscription.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets role assignments of the scope.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<RoleAssignment>> listForScopeNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets role assignments of the scope.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listForScopeNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException;

}
