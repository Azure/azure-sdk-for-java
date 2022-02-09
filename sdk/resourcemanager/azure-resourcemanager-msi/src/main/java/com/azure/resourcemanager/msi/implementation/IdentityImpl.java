// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.msi.implementation;

import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;
import com.azure.resourcemanager.msi.MsiManager;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.msi.fluent.models.IdentityInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The implementation for Identity and its create and update interfaces.
 */
public final class IdentityImpl
        extends GroupableResourceImpl<Identity, IdentityInner, IdentityImpl, MsiManager>
        implements Identity, Identity.Definition, Identity.Update {

    private RoleAssignmentHelper roleAssignmentHelper;

    public IdentityImpl(String name, IdentityInner innerObject, MsiManager manager) {
        super(name, innerObject, manager);
        this.roleAssignmentHelper = new RoleAssignmentHelper(manager.authorizationManager(),
            this.taskGroup(),
            this.idProvider());
    }

    @Override
    public String tenantId() {
        if (this.innerModel().tenantId() == null) {
            return null;
        } else {
            return this.innerModel().tenantId().toString();
        }
    }

    @Override
    public String principalId() {
        if (this.innerModel().principalId() == null) {
            return null;
        } else {
            return this.innerModel().principalId().toString();
        }
    }

    @Override
    public String clientId() {
        if (this.innerModel().clientId() == null) {
            return null;
        } else {
            return this.innerModel().clientId().toString();
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
        return this.manager().serviceClient().getUserAssignedIdentities()
                .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<IdentityInner> getInnerAsync() {
        return this.myManager
                .serviceClient()
                .getUserAssignedIdentities()
                .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private RoleAssignmentHelper.IdProvider idProvider() {
        return new RoleAssignmentHelper.IdProvider() {
            @Override
            public String principalId() {
                Objects.requireNonNull(innerModel());
                Objects.requireNonNull(innerModel().principalId());
                return innerModel().principalId().toString();
            }
            @Override
            public String resourceId() {
                Objects.requireNonNull(innerModel());
                Objects.requireNonNull(innerModel().id());
                return innerModel().id();
            }
        };
    }
}
