// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.RoleAssignments;
import com.azure.resourcemanager.authorization.fluent.inner.RoleAssignmentInner;
import com.azure.resourcemanager.authorization.fluent.RoleAssignmentsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/** The implementation of RoleAssignments and its parent interfaces. */
public class RoleAssignmentsImpl extends CreatableResourcesImpl<RoleAssignment, RoleAssignmentImpl, RoleAssignmentInner>
    implements RoleAssignments, HasInner<RoleAssignmentsClient> {
    private final AuthorizationManager manager;

    public RoleAssignmentsImpl(final AuthorizationManager manager) {
        this.manager = manager;
    }

    @Override
    protected RoleAssignmentImpl wrapModel(RoleAssignmentInner roleAssignmentInner) {
        if (roleAssignmentInner == null) {
            return null;
        }
        return new RoleAssignmentImpl(roleAssignmentInner.name(), roleAssignmentInner, manager());
    }

    @Override
    public RoleAssignmentImpl getById(String objectId) {
        return (RoleAssignmentImpl) getByIdAsync(objectId).block();
    }

    @Override
    public Mono<RoleAssignment> getByIdAsync(String id) {
        return inner()
            .getByIdAsync(id)
            .map(
                roleAssignmentInner ->
                    new RoleAssignmentImpl(roleAssignmentInner.name(), roleAssignmentInner, manager()));
    }

    @Override
    public RoleAssignmentImpl getByScope(String scope, String name) {
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
    public Mono<RoleAssignment> getByScopeAsync(String scope, String name) {
        return inner()
            .getAsync(scope, name)
            .map(
                roleAssignmentInner ->
                    new RoleAssignmentImpl(roleAssignmentInner.name(), roleAssignmentInner, manager()));
    }

    @Override
    protected RoleAssignmentImpl wrapModel(String name) {
        return new RoleAssignmentImpl(name, new RoleAssignmentInner(), manager());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return inner().deleteByIdAsync(id).then();
    }

    @Override
    public RoleAssignmentImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    @Override
    public RoleAssignmentsClient inner() {
        return manager().roleInner().getRoleAssignments();
    }
}
