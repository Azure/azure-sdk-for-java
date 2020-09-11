// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.msi.implementation;

import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.implementation.RoleAssignmentHelper;
import com.azure.resourcemanager.msi.MSIManager;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.msi.fluent.inner.IdentityInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The implementation for Identity and its create and update interfaces.
 */
public final class IdentityImpl
        extends GroupableResourceImpl<Identity, IdentityInner, IdentityImpl, MSIManager>
        implements Identity, Identity.Definition, Identity.Update {

    private RoleAssignmentHelper roleAssignmentHelper;

    public IdentityImpl(String name, IdentityInner innerObject, MSIManager manager) {
        super(name, innerObject, manager);
        this.roleAssignmentHelper = new RoleAssignmentHelper(manager.graphRbacManager(),
            this.taskGroup(),
            this.idProvider());
    }

    @Override
    public String tenantId() {
        if (this.inner().tenantId() == null) {
            return null;
        } else {
            return this.inner().tenantId().toString();
        }
    }

    @Override
    public String principalId() {
        if (this.inner().principalId() == null) {
            return null;
        } else {
            return this.inner().principalId().toString();
        }
    }

    @Override
    public String clientId() {
        if (this.inner().clientId() == null) {
            return null;
        } else {
            return this.inner().clientId().toString();
        }
    }

    @Override
    public IdentityImpl withAccessTo(Resource resource, BuiltInRole role) {
        this.roleAssignmentHelper.withAccessTo(resource.id(), role);
        return this;
    }

    @Override
    public IdentityImpl withAccessTo(String resourceId, BuiltInRole role) {
        this.roleAssignmentHelper.withAccessTo(resourceId, role);
        return this;
    }

    @Override
    public IdentityImpl withAccessToCurrentResourceGroup(BuiltInRole role) {
        this.roleAssignmentHelper.withAccessToCurrentResourceGroup(role);
        return this;
    }

    @Override
    public IdentityImpl withAccessTo(Resource resource, String roleDefinitionId) {
        this.roleAssignmentHelper.withAccessTo(resource.id(), roleDefinitionId);
        return this;
    }

    @Override
    public IdentityImpl withAccessTo(String resourceId, String roleDefinitionId) {
        this.roleAssignmentHelper.withAccessTo(resourceId, roleDefinitionId);
        return this;
    }

    @Override
    public IdentityImpl withAccessToCurrentResourceGroup(String roleDefinitionId) {
        this.roleAssignmentHelper.withAccessToCurrentResourceGroup(roleDefinitionId);
        return this;
    }

    @Override
    public IdentityImpl withoutAccess(RoleAssignment access) {
        this.roleAssignmentHelper.withoutAccessTo(access);
        return this;
    }

    @Override
    public IdentityImpl withoutAccessTo(String resourceId, BuiltInRole role) {
        this.roleAssignmentHelper.withoutAccessTo(resourceId, role);
        return this;
    }

    @Override
    public Mono<Identity> createResourceAsync() {
        return this.manager().inner().getUserAssignedIdentities()
                .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<IdentityInner> getInnerAsync() {
        return this.myManager
                .inner()
                .getUserAssignedIdentities()
                .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private RoleAssignmentHelper.IdProvider idProvider() {
        return new RoleAssignmentHelper.IdProvider() {
            @Override
            public String principalId() {
                Objects.requireNonNull(inner());
                Objects.requireNonNull(inner().principalId());
                return inner().principalId().toString();
            }
            @Override
            public String resourceId() {
                Objects.requireNonNull(inner());
                Objects.requireNonNull(inner().id());
                return inner().id();
            }
        };
    }
}
