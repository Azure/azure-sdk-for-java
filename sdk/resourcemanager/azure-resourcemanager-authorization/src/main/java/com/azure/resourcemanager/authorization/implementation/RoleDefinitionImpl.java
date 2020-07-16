// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.Permission;
import com.azure.resourcemanager.authorization.models.RoleDefinition;
import com.azure.resourcemanager.authorization.fluent.inner.PermissionInner;
import com.azure.resourcemanager.authorization.fluent.inner.RoleDefinitionInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Implementation for ServicePrincipal and its parent interfaces. */
class RoleDefinitionImpl extends WrapperImpl<RoleDefinitionInner> implements RoleDefinition {
    private AuthorizationManager manager;
    // Active Directory identify info
    // private String objectId;
    // private String userName;
    // private String servicePrincipalName;
    // role info
    // private String roleDefinitionId;
    // private String roleName;

    RoleDefinitionImpl(RoleDefinitionInner innerObject, AuthorizationManager manager) {
        super(innerObject);
        this.manager = manager;
    }

    @Override
    public String roleName() {
        return inner().roleName();
    }

    @Override
    public String description() {
        return inner().description();
    }

    @Override
    public String type() {
        return inner().type();
    }

    @Override
    public Set<Permission> permissions() {
        HashSet<Permission> ret = new HashSet<>();
        for (PermissionInner inner : inner().permissions()) {
            ret.add(new PermissionImpl(inner));
        }
        return ret;
    }

    @Override
    public Set<String> assignableScopes() {
        return Collections.unmodifiableSet(new HashSet(inner().assignableScopes()));
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    @Override
    public String name() {
        return inner().name();
    }
}
