// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluent.models.ManagementLockObjectInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.models.ManagementLock;
import com.azure.resourcemanager.resources.models.ManagementLocks;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *  Implementation for ManagementLocks.
 */
public final class ManagementLocksImpl
    extends CreatableResourcesImpl<ManagementLock, ManagementLockImpl, ManagementLockObjectInner>
    implements ManagementLocks {

    private final ResourceManager manager;

    public ManagementLocksImpl(final ResourceManager manager) {
        this.manager = manager;
    }

    /**
     * Returns the part of the specified management lock resource ID
     * representing the resource the lock is associated with.
     * @param lockId a lock resource ID
     * @return a resource ID
     */
    static String resourceIdFromLockId(String lockId) {
        String[] lockIdParts = lockIdParts(lockId);
        if (CoreUtils.isNullOrEmpty(lockIdParts)) {
            return null;
        }

        StringBuilder resourceId = new StringBuilder();
        for (int i = 0; i < lockIdParts.length - 4; i++) {
            if (!lockIdParts[i].isEmpty()) {
                resourceId.append("/").append(lockIdParts[i]);
            }
        }

        return resourceId.toString();
    }

    private static String[] lockIdParts(String lockId) {
        if (CoreUtils.isNullOrEmpty(lockId)) {
            return new String[0];
        }

        String[] parts = lockId.split("/");
        if (parts.length < 4) {
            // ID too short to be possibly a lock ID
            return new String[0];
        }

        if (!parts[parts.length - 2].equalsIgnoreCase("locks")
                || !parts[parts.length - 3].equalsIgnoreCase("Microsoft.Authorization")
                || !parts[parts.length - 4].equalsIgnoreCase("providers")) {
            // Not a lock ID
            return new String[0];
        }

        return parts;
    }

    @Override
    public ManagementLockImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected ManagementLockImpl wrapModel(String name) {
        ManagementLockObjectInner inner = new ManagementLockObjectInner();

        return new ManagementLockImpl(name, inner, this.manager());
    }

    @Override
    protected ManagementLockImpl wrapModel(ManagementLockObjectInner inner) {
        if (inner == null) {
            return null;
        }
        return new ManagementLockImpl(inner.name(), inner, this.manager());
    }

    @Override
    public PagedIterable<ManagementLock> list() {
        return wrapList(this.manager().managementLockClient().getManagementLocks().list());
    }

    @Override
    public PagedFlux<ManagementLock> listAsync() {
        return wrapPageAsync(this.manager().managementLockClient().getManagementLocks().listAsync());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        String scope = resourceIdFromLockId(id);
        String lockName = ResourceUtils.nameFromResourceId(id);
        if (scope != null && lockName != null) {
            return this.manager().managementLockClient().getManagementLocks().deleteByScopeAsync(scope, lockName);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public PagedIterable<ManagementLock> listByResourceGroup(String resourceGroupName) {
        return wrapList(this.manager().managementLockClient().getManagementLocks()
            .listByResourceGroup(resourceGroupName));
    }

    @Override
    public PagedFlux<ManagementLock> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        return wrapPageAsync(this.manager().managementLockClient().getManagementLocks()
            .listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public ManagementLock getByResourceGroup(String resourceGroupName, String name) {
        return this.getByResourceGroupAsync(resourceGroupName, name).block();
    }

    @Override
    public Mono<ManagementLock> getByResourceGroupAsync(String resourceGroupName, String name) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(name)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'name' is required and cannot be null."));
        }
        return this.manager().managementLockClient().getManagementLocks()
            .getByResourceGroupAsync(resourceGroupName, name)
            .map(this::wrapModel);
    }

    @Override
    public ManagementLock getById(String id) {
        return this.getByIdAsync(id).block();
    }

    @Override
    public Mono<ManagementLock> getByIdAsync(String id) {
        String resourceId = resourceIdFromLockId(id);
        String lockName = ResourceUtils.nameFromResourceId(id);
        return this.manager().managementLockClient().getManagementLocks().getByScopeAsync(resourceId, lockName)
            .map(this::wrapModel);
    }

    @Override
    public void deleteByResourceGroup(String resourceGroupName, String name) {
        this.deleteByResourceGroupAsync(resourceGroupName, name).block();
    }

    @Override
    public Mono<Void> deleteByResourceGroupAsync(String resourceGroupName, String name) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(name)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'name' is required and cannot be null."));
        }
        return this.manager().managementLockClient().getManagementLocks()
            .deleteAsync(resourceGroupName, name);
    }

    @Override
    public Flux<String> deleteByIdsAsync(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        return Flux.fromIterable(ids)
            .flatMapDelayError(id -> {
                String lockName = ResourceUtils.nameFromResourceId(id);
                String scopeName = ManagementLocksImpl.resourceIdFromLockId(id);
                return this.manager().managementLockClient().getManagementLocks()
                    .deleteByScopeAsync(scopeName, lockName)
                    .then(Mono.just(id));
            }, 32, 32);
    }

    @Override
    public Flux<String> deleteByIdsAsync(String... ids) {
        return this.deleteByIdsAsync(new ArrayList<>(Arrays.asList(ids)));
    }

    @Override
    public void deleteByIds(Collection<String> ids) {
        this.deleteByIdsAsync(ids).blockLast();
    }

    @Override
    public void deleteByIds(String... ids) {
        this.deleteByIdsAsync(ids).blockLast();
    }

    @Override
    public ResourceManager manager() {
        return this.manager;
    }

    @Override
    public PagedIterable<ManagementLock> listForResource(String resourceId) {
        return wrapList(this.manager().managementLockClient().getManagementLocks().listByScope(resourceId));
    }

    @Override
    public PagedFlux<ManagementLock> listForResourceAsync(String resourceId) {
        return wrapPageAsync(this.manager().managementLockClient().getManagementLocks().listByScopeAsync(resourceId));
    }
}
