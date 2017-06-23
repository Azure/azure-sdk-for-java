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
import com.microsoft.azure.management.graphrbac.implementation.RoleAssignmentsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;

/**
 * Entry point to role assignment management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
@Beta(SinceVersion.V1_1_0)
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
    Observable<RoleAssignment> getByScopeAsync(String scope, String name);

    /**
     * Gets the information about a role assignment based on scope and name.
     *
     * @param scope the scope of the role assignment
     * @param name the name of the role assignment
     * @param callback the callback when the operation finishes
     * @return an immutable representation of the role assignment
     */
    ServiceFuture<RoleAssignment> getByScopeAsync(String scope, String name, ServiceCallback<RoleAssignment> callback);

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
    Observable<RoleAssignment> listByScopeAsync(String scope);

    /**
     * List role assignments in a scope.
     *
     * @param scope the scope of the role assignments
     * @return a list of role assignments
     */
    PagedList<RoleAssignment> listByScope(String scope);
}
