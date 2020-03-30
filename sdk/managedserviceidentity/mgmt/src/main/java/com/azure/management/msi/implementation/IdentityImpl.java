/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.msi.implementation;

import com.azure.management.graphrbac.BuiltInRole;
import com.azure.management.graphrbac.RoleAssignment;
import com.azure.management.graphrbac.implementation.RoleAssignmentHelper;
import com.azure.management.msi.Identity;
import com.azure.management.msi.models.IdentityInner;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The implementation for Identity and its create and update interfaces.
 */
final class IdentityImpl
        extends GroupableResourceImpl<Identity, IdentityInner, IdentityImpl, MSIManager>
        implements Identity, Identity.Definition, Identity.Update {

    private RoleAssignmentHelper roleAssignmentHelper;

    protected IdentityImpl(String name, IdentityInner innerObject, MSIManager manager) {
        super(name, innerObject, manager);
        this.roleAssignmentHelper = new RoleAssignmentHelper(manager.graphRbacManager(), this.taskGroup(), this.idProvider());
    }

    @Override
    public String tenantId() {
        if (this.inner().getTenantId() == null) {
            return null;
        } else {
            return this.inner().getTenantId().toString();
        }
    }

    @Override
    public String principalId() {
        if (this.inner().getPrincipalId() == null) {
            return null;
        } else {
            return this.inner().getPrincipalId().toString();
        }
    }

    @Override
    public String clientId() {
        if (this.inner().getClientId() == null) {
            return null;
        } else {
            return this.inner().getClientId().toString();
        }
    }

    @Override
    public String clientSecretUrl() {
        return this.inner().getClientSecretUrl();
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
        return this.manager().inner().userAssignedIdentities()
                .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<IdentityInner> getInnerAsync() {
        return this.myManager
                .inner()
                .userAssignedIdentities()
                .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private RoleAssignmentHelper.IdProvider idProvider() {
        return new RoleAssignmentHelper.IdProvider() {
            @Override
            public String principalId() {
                Objects.requireNonNull(inner());
                Objects.requireNonNull(inner().getPrincipalId());
                return inner().getPrincipalId().toString();
            }
            @Override
            public String resourceId() {
                Objects.requireNonNull(inner());
                Objects.requireNonNull(inner().getId());
                return inner().getId();
            }
        };
    }
}
