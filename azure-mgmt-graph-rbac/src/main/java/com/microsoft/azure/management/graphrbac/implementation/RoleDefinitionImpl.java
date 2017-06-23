/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.google.common.collect.Sets;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.RoleDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.util.Collections;
import java.util.Set;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class RoleDefinitionImpl
        extends WrapperImpl<RoleDefinitionInner>
        implements
            RoleDefinition {
    private GraphRbacManager manager;
    // Active Directory identify info
    private String objectId;
    private String userName;
    private String servicePrincipalName;
    // role info
    private String roleDefinitionId;
    private String roleName;

    RoleDefinitionImpl(RoleDefinitionInner innerObject, GraphRbacManager manager) {
        super(innerObject);
        this.manager = manager;
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public String roleName() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().roleName();
    }

    @Override
    public String description() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().description();
    }

    @Override
    public String type() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().type();
    }

    @Override
    public Set<PermissionInner> permissions() {
        if (inner().properties() == null) {
            return null;
        }
        return Collections.unmodifiableSet(Sets.newHashSet(inner().properties().permissions()));
    }

    @Override
    public Set<String> assignableScopes() {
        if (inner().properties() == null) {
            return null;
        }
        return Collections.unmodifiableSet(Sets.newHashSet(inner().properties().assignableScopes()));
    }
}
