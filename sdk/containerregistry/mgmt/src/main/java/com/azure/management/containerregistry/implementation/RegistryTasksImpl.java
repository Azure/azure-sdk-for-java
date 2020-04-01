/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.containerregistry.RegistryTask;
import com.azure.management.containerregistry.RegistryTasks;
import com.azure.management.containerregistry.models.TaskInner;
import com.azure.management.containerregistry.models.TasksInner;
import reactor.core.publisher.Mono;

class RegistryTasksImpl implements RegistryTasks {

    private final ContainerRegistryManager registryManager;

    RegistryTasksImpl(ContainerRegistryManager registryManager) {
        this.registryManager = registryManager;
    }
    @Override
    public RegistryTask.DefinitionStages.Blank define(String name) {
        return new RegistryTaskImpl(this.registryManager, name);
    }

    @Override
    public PagedFlux<RegistryTask> listByRegistryAsync(String resourceGroupName, String registryName) {
        return this.registryManager.inner().tasks().listAsync(resourceGroupName, registryName)
                .mapPage(inner -> wrapModel(inner));
    }

    @Override
    public PagedIterable<RegistryTask> listByRegistry(String resourceGroupName, String registryName) {
        return new PagedIterable<>(this.listByRegistryAsync(resourceGroupName, registryName));
    }

    @Override
    public Mono<RegistryTask> getByRegistryAsync(String resourceGroupName, String registryName, String taskName, boolean includeSecrets) {
        if (includeSecrets) {
            return this.registryManager.inner().tasks().getDetailsAsync(resourceGroupName, registryName, taskName)
                .map(taskInner -> new RegistryTaskImpl(registryManager, taskInner));
        } else {
            return this.registryManager.inner().tasks().getAsync(resourceGroupName, registryName, taskName)
                .map(taskInner -> new RegistryTaskImpl(registryManager, taskInner));
        }
    }

    @Override
    public RegistryTask getByRegistry(String resourceGroupName, String registryName, String taskName, boolean includeSecrets) {
        return this.getByRegistryAsync(resourceGroupName, registryName, taskName, includeSecrets).block();
    }

    @Override
    public Mono<Void> deleteByRegistryAsync(String resourceGroupName, String registryName, String taskName) {
        return this.registryManager.inner().tasks().deleteAsync(resourceGroupName, registryName, taskName);
    }

    @Override
    public void deleteByRegistry(String resourceGroupName, String registryName, String taskName) {
        this.deleteByRegistryAsync(resourceGroupName, registryName, taskName).block();
    }

    private RegistryTaskImpl wrapModel(TaskInner innerModel) {
        return new RegistryTaskImpl(this.registryManager, innerModel);
    }

    @Override
    public TasksInner inner() {
        return this.registryManager.inner().tasks();
    }
}
