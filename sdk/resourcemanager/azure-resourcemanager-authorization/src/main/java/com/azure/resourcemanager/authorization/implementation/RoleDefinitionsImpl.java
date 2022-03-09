// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.RoleDefinition;
import com.azure.resourcemanager.authorization.models.RoleDefinitions;
import com.azure.resourcemanager.authorization.fluent.models.RoleDefinitionInner;
import com.azure.resourcemanager.authorization.fluent.RoleDefinitionsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation of RoleDefinitions and its parent interfaces. */
public class RoleDefinitionsImpl extends ReadableWrappersImpl<RoleDefinition, RoleDefinitionImpl, RoleDefinitionInner>
    implements RoleDefinitions {
    private final AuthorizationManager manager;

    public RoleDefinitionsImpl(final AuthorizationManager manager) {
        this.manager = manager;
    }

    @Override
    protected RoleDefinitionImpl wrapModel(RoleDefinitionInner roleDefinitionInner) {
        if (roleDefinitionInner == null) {
            return null;
        }
        return new RoleDefinitionImpl(roleDefinitionInner, manager());
    }

    @Override
    public RoleDefinition getById(String objectId) {
        return getByIdAsync(objectId).block();
    }

    @Override
    public Mono<RoleDefinition> getByIdAsync(String id) {
        return inner()
            .getByIdAsync(id)
            .map(roleDefinitionInner -> new RoleDefinitionImpl(roleDefinitionInner, manager()));
    }

    @Override
    public RoleDefinition getByScope(String scope, String name) {
        return getByScopeAsync(scope, name).block();
    }

    @Override
    public Mono<RoleDefinition> getByScopeAsync(String scope, String name) {
        return inner()
            .getAsync(scope, name)
            .map(roleDefinitionInner -> new RoleDefinitionImpl(roleDefinitionInner, manager()));
    }

    @Override
    public RoleDefinition getByScopeAndRoleName(String scope, String roleName) {
        return getByScopeAndRoleNameAsync(scope, roleName).block();
    }

    @Override
    public PagedFlux<RoleDefinition> listByScopeAsync(String scope) {
        return PagedConverter.mapPage(inner()
            .listAsync(scope, null),
            roleDefinitionInner -> new RoleDefinitionImpl(roleDefinitionInner, manager()));
    }

    @Override
    public PagedIterable<RoleDefinition> listByScope(String scope) {
        return wrapList(inner().list(scope));
    }

    @Override
    public Mono<RoleDefinition> getByScopeAndRoleNameAsync(String scope, String roleName) {
        return inner()
            .listAsync(scope, String.format("roleName eq '%s'", roleName))
            .singleOrEmpty()
            .map(roleDefinitionInner -> new RoleDefinitionImpl(roleDefinitionInner, manager()));
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    public RoleDefinitionsClient inner() {
        return manager().roleServiceClient().getRoleDefinitions();
    }
}
