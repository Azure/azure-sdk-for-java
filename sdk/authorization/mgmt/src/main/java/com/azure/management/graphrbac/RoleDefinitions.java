/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.graphrbac.models.RoleDefinitionsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * Entry point to role definition management API.
 */
@Fluent
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
    Mono<RoleDefinition> getByScopeAsync(String scope, String name);

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
    Mono<RoleDefinition> getByScopeAndRoleNameAsync(String scope, String roleName);

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
    PagedFlux<RoleDefinition> listByScopeAsync(String scope);

    /**
     * List role definitions in a scope.
     *
     * @param scope the scope of the role definition
     * @return a list of role definitions
     */
    PagedIterable<RoleDefinition> listByScope(String scope);
}
