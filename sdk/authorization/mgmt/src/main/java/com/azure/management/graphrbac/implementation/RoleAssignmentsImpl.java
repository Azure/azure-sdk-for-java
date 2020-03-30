/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.CloudException;
import com.azure.management.graphrbac.RoleAssignment;
import com.azure.management.graphrbac.RoleAssignments;
import com.azure.management.graphrbac.models.RoleAssignmentInner;
import com.azure.management.graphrbac.models.RoleAssignmentsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * The implementation of RoleAssignments and its parent interfaces.
 */
class RoleAssignmentsImpl
        extends CreatableResourcesImpl<
                RoleAssignment,
                RoleAssignmentImpl,
                RoleAssignmentInner>
        implements
        RoleAssignments,
        HasInner<RoleAssignmentsInner> {
    private final GraphRbacManager manager;

    RoleAssignmentsImpl(
            final GraphRbacManager manager) {
        this.manager = manager;
    }

    @Override
    protected RoleAssignmentImpl wrapModel(RoleAssignmentInner roleAssignmentInner) {
        if (roleAssignmentInner == null) {
            return null;
        }
        return new RoleAssignmentImpl(roleAssignmentInner.getName(), roleAssignmentInner, manager());
    }

    @Override
    public RoleAssignmentImpl getById(String objectId) {
        return (RoleAssignmentImpl) getByIdAsync(objectId).block();
    }

    @Override
    public Mono<RoleAssignment> getByIdAsync(String id) {
        return inner().getByIdAsync(id)
                .onErrorResume(CloudException.class, e -> Mono.empty())
                .map(roleAssignmentInner -> new RoleAssignmentImpl(roleAssignmentInner.getName(), roleAssignmentInner, manager()));
    }

    @Override
    public RoleAssignmentImpl getByScope(String scope,  String name) {
        return (RoleAssignmentImpl) getByScopeAsync(scope, name).block();
    }

    @Override
    public PagedFlux<RoleAssignment> listByScopeAsync(String scope) {
        return inner().listForScopeAsync(scope, null).mapPage(roleAssignmentInner -> wrapModel(roleAssignmentInner));
    }

    @Override
    public PagedIterable<RoleAssignment> listByScope(String scope) {
        return wrapList(inner().listForScope(scope, null));
    }

    @Override
    public Mono<RoleAssignment> getByScopeAsync(String scope,  String name) {
        return inner().getAsync(scope, name)
                .onErrorResume(CloudException.class, e-> Mono.empty())
                .map(roleAssignmentInner -> new RoleAssignmentImpl(roleAssignmentInner.getName(), roleAssignmentInner, manager()));
    }

    @Override
    protected RoleAssignmentImpl wrapModel(String name) {
        return new RoleAssignmentImpl(name, new RoleAssignmentInner(), manager());
    }

    @Override
    public Mono<RoleAssignment> deleteByIdAsync(String id) {
        return inner().deleteByIdAsync(id)
                .onErrorResume(CloudException.class, e -> Mono.empty())
                .map(roleAssignmentInner -> new RoleAssignmentImpl(roleAssignmentInner.getName(), roleAssignmentInner, manager()));
    }

    @Override
    public RoleAssignmentImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }

    @Override
    public RoleAssignmentsInner inner() {
        return manager().roleInner().roleAssignments();
    }
}
