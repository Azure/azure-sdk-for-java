/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.graphrbac.implementation.RoleDefinitionsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;

/**
 * Entry point to role definition management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
@Beta(SinceVersion.V1_1_0)
public interface RoleDefinitions extends
        SupportsGettingById<RoleDefinition>,
        HasManager<GraphRbacManager>,
        HasInner<RoleDefinitionsInner> {
    /**
     * Gets the information about a role definition based on scope and name.
     *
     * @param scope the scope of the role definition
     * @param name the name of the role definition
     * @return an immutable representation of the role definition
     */
    Observable<RoleDefinition> getByScopeAsync(String scope, String name);

    /**
     * Gets the information about a role definition based on scope and name.
     *
     * @param scope the scope of the role definition
     * @param name the name of the role definition
     * @param callback the callback when the operation finishes
     * @return an immutable representation of the role definition
     */
    ServiceFuture<RoleDefinition> getByScopeAsync(String scope, String name, ServiceCallback<RoleDefinition> callback);

    /**
     * Gets the information about a role definition based on scope and name.
     *
     * @param scope the scope of the role definition
     * @param name the name of the role definition
     * @return an immutable representation of the role definition
     */
    RoleDefinition getByScope(String scope, String name);

    /**
     * Gets the information about a role definition based on scope and name.
     *
     * @param scope the scope of the role definition
     * @param roleName the name of the role
     * @return an immutable representation of the role definition
     */
    Observable<RoleDefinition> getByScopeAndRoleNameAsync(String scope, String roleName);

    /**
     * Gets the information about a role definition based on scope and name.
     *
     * @param scope the scope of the role definition
     * @param roleName the name of the role
     * @param callback the callback when the operation finishes
     * @return an immutable representation of the role definition
     */
    ServiceFuture<RoleDefinition> getByScopeAndRoleNameAsync(String scope, String roleName, ServiceCallback<RoleDefinition> callback);

    /**
     * Gets the information about a role definition based on scope and name.
     *
     * @param scope the scope of the role definition
     * @param roleName the name of the role
     * @return an immutable representation of the role definition
     */
    RoleDefinition getByScopeAndRoleName(String scope, String roleName);

    /**
     * List role definitions in a scope.
     *
     * @param scope the scope of the role definition
     * @return an observable of role definitions
     */
    Observable<RoleDefinition> listByScopeAsync(String scope);

    /**
     * List role definitions in a scope.
     *
     * @param scope the scope of the role definition
     * @return a list of role definitions
     */
    PagedList<RoleDefinition> listByScope(String scope);
}
