// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import reactor.core.publisher.Mono;

/** Entry point to role assignment management API. */
@Fluent
public interface RoleAssignments
    extends SupportsGettingById<RoleAssignment>,
        SupportsCreating<RoleAssignment.DefinitionStages.Blank>,
        SupportsBatchCreation<RoleAssignment>,
        SupportsDeletingById,
        HasManager<AuthorizationManager> {
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
     * @return a list of role assignments
     */
    PagedFlux<RoleAssignment> listByScopeAsync(String scope);

    /**
     * List role assignments in a scope.
     *
     * @param scope the scope of the role assignments
     * @return a list of role assignments
     */
    PagedIterable<RoleAssignment> listByScope(String scope);

    /**
     * List role assignments for a service principal.
     *
     * @param servicePrincipal the service principal
     * @return a list of role assignments
     */
    PagedFlux<RoleAssignment> listByServicePrincipalAsync(ServicePrincipal servicePrincipal);

    /**
     * List role assignments for a service principal.
     *
     * @param servicePrincipal the service principal
     * @return a list of role assignments
     */
    PagedIterable<RoleAssignment> listByServicePrincipal(ServicePrincipal servicePrincipal);


    /**
     * List role assignments for a service principal.
     *
     * @param principalId the ID of the service principal
     * @return a list of role assignments
     */
    PagedFlux<RoleAssignment> listByServicePrincipalAsync(String principalId);

    /**
     * List role assignments for a service principal.
     *
     * @param principalId the ID of the service principal
     * @return a list of role assignments
     */
    PagedIterable<RoleAssignment> listByServicePrincipal(String principalId);
}
