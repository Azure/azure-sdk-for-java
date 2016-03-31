/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.resources.models.PageImpl;
import com.microsoft.azure.management.resources.models.RoleDefinition;
import com.microsoft.azure.management.resources.models.RoleDefinitionFilter;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in RoleDefinitions.
 */
public interface RoleDefinitions {
    /**
     * Deletes the role definition.
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition id.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleDefinition object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleDefinition> delete(String scope, String roleDefinitionId) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Deletes the role definition.
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition id.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String scope, String roleDefinitionId, final ServiceCallback<RoleDefinition> serviceCallback) throws IllegalArgumentException;

    /**
     * Get role definition by name (GUID).
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleDefinition object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleDefinition> get(String scope, String roleDefinitionId) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Get role definition by name (GUID).
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String scope, String roleDefinitionId, final ServiceCallback<RoleDefinition> serviceCallback) throws IllegalArgumentException;

    /**
     * Creates or updates a role definition.
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition id.
     * @param roleDefinition Role definition.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleDefinition object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleDefinition> createOrUpdate(String scope, String roleDefinitionId, RoleDefinition roleDefinition) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Creates or updates a role definition.
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition id.
     * @param roleDefinition Role definition.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall createOrUpdateAsync(String scope, String roleDefinitionId, RoleDefinition roleDefinition, final ServiceCallback<RoleDefinition> serviceCallback) throws IllegalArgumentException;

    /**
     * Get role definition by name (GUID).
     *
     * @param roleDefinitionId Fully qualified role definition Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleDefinition object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<RoleDefinition> getById(String roleDefinitionId) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Get role definition by name (GUID).
     *
     * @param roleDefinitionId Fully qualified role definition Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getByIdAsync(String roleDefinitionId, final ServiceCallback<RoleDefinition> serviceCallback) throws IllegalArgumentException;

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param scope Scope
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleDefinition&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleDefinition>> list(final String scope) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param scope Scope
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final String scope, final ListOperationCallback<RoleDefinition> serviceCallback) throws IllegalArgumentException;
    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param scope Scope
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleDefinition&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<RoleDefinition>> list(final String scope, final RoleDefinitionFilter filter) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param scope Scope
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final String scope, final RoleDefinitionFilter filter, final ListOperationCallback<RoleDefinition> serviceCallback) throws IllegalArgumentException;

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleDefinition&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<RoleDefinition>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleDefinition> serviceCallback) throws IllegalArgumentException;

}
