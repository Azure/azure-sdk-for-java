// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluent.models.ManagementLockObjectInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.models.LockLevel;
import com.azure.resourcemanager.resources.models.ManagementLock;
import com.azure.resourcemanager.resources.models.ManagementLockOwner;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 *  Implementation for ManagementLock and its create and update interfaces.
 */
final class ManagementLockImpl
    extends CreatableUpdatableImpl<ManagementLock, ManagementLockObjectInner, ManagementLockImpl>
    implements
        ManagementLock,
        ManagementLock.Definition,
        ManagementLock.Update {

    private final ResourceManager manager;
    private String lockedResourceId = null;
    private final ClientLogger logger = new ClientLogger(ManagementLockImpl.class);

    ManagementLockImpl(
            String name,
            ManagementLockObjectInner innerModel,
            final ResourceManager manager) {
        super(name, innerModel);
        this.manager = manager;
    }

    @Override
    protected Mono<ManagementLockObjectInner> getInnerAsync() {
        return this.manager().managementLockClient().getManagementLocks()
            .getByScopeAsync(this.lockedResourceId(), this.name());
    }

    @Override
    public ManagementLockImpl withNotes(String notes) {
        this.innerModel().withNotes(notes);
        return this;
    }

    @Override
    public ManagementLockImpl withLevel(LockLevel level) {
        this.innerModel().withLevel(level);
        return this;
    }

    @Override
    public ManagementLockImpl withLockedResource(String resourceId) {
        if (!CoreUtils.isNullOrEmpty(resourceId)) {
            this.lockedResourceId = resourceId;
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException("Missing resource ID."));
        }
        return this;
    }

    @Override
    public ManagementLockImpl withLockedResource(Resource resource) {
        if (resource != null) {
            this.lockedResourceId = resource.id();
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException("Missing resource ID."));
        }
        return this;
    }

    @Override
    public ManagementLockImpl withLockedResourceGroup(String resourceGroupName) {
        return withLockedResource(
            "/subscriptions/" + this.manager().subscriptionId() + "/resourceGroups/" + resourceGroupName);
    }

    @Override
    public ManagementLockImpl withLockedResourceGroup(ResourceGroup resourceGroup) {
        if (resourceGroup != null) {
            this.lockedResourceId = resourceGroup.id();
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException("Missing resource group ID."));
        }
        return this;
    }

    @Override
    public ResourceManager manager() {
        return this.manager;
    }

    @Override
    public Mono<ManagementLock> createResourceAsync() {
        return this.manager().managementLockClient()
            .getManagementLocks()
            .createOrUpdateByScopeAsync(this.lockedResourceId(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public LockLevel level() {
        return this.innerModel().level();
    }

    @Override
    public String lockedResourceId() {
        if (this.lockedResourceId == null) {
            this.lockedResourceId = ManagementLocksImpl.resourceIdFromLockId(this.innerModel().id());
        }
        return this.lockedResourceId;
    }

    @Override
    public String notes() {
        return this.innerModel().notes();
    }

    @Override
    public List<ManagementLockOwner> owners() {
        if (this.innerModel().owners() == null) {
            return null;
        }
        return Collections.unmodifiableList(this.innerModel().owners());
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }
}
