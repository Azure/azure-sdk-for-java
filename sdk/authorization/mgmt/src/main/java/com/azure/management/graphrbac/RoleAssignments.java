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
import com.azure.management.graphrbac.models.RoleAssignmentsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * Entry point to role assignment management API.
 */
@Fluent
public interface RoleAssignments extends
        SupportsGettingById<RoleAssignment>,
        SupportsCreating<RoleAssignment.DefinitionStages.Blank>,
        SupportsBatchCreation<RoleAssignment>,
        SupportsDeletingById,
        HasManager<GraphRbacManager>,
        HasInner<RoleAssignmentsInner> {
    /**
     * Gets the information about a role assignment based on scope and name.
     *
     * @param scope the scope of the role assignment
     * @param name the name of the role assignment
     * @return an immutable representation of the role assignment
     */
    Mono<RoleAssignment> getByScopeAsync(String scope, String name);

    /**
     * Gets the information about a role assignment based on scope and name.
     *
     * @param scope the scope of the role assignment
     * @param name the name of the role assignment
     * @return an immutable representation of the role assignment
     */
    RoleAssignment getByScope(String scope, String name);

    /**
     * List role assignments in a scope.
     *
     * @param scope the scope of the role assignments
     * @return an observable of role assignments
     */
    PagedFlux<RoleAssignment> listByScopeAsync(String scope);

    /**
     * List role assignments in a scope.
     *
     * @param scope the scope of the role assignments
     * @return a list of role assignments
     */
    PagedIterable<RoleAssignment> listByScope(String scope);
}
