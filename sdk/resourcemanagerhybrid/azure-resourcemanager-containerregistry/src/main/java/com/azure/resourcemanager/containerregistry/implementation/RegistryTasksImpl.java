// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.fluent.TasksClient;
import com.azure.resourcemanager.containerregistry.fluent.models.TaskInner;
import com.azure.resourcemanager.containerregistry.models.RegistryTask;
import com.azure.resourcemanager.containerregistry.models.RegistryTasks;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

public class RegistryTasksImpl implements RegistryTasks {

    private final ContainerRegistryManager registryManager;

    public RegistryTasksImpl(ContainerRegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    @Override
    public RegistryTask.DefinitionStages.Blank define(String name) {
        return new RegistryTaskImpl(this.registryManager, name);
    }

    @Override
    public PagedFlux<RegistryTask> listByRegistryAsync(String resourceGroupName, String registryName) {
        return PagedConverter.mapPage(this
            .registryManager
            .serviceClient()
            .getTasks()
            .listAsync(resourceGroupName, registryName),
            inner -> wrapModel(inner));
    }

    @Override
    public PagedIterable<RegistryTask> listByRegistry(String resourceGroupName, String registryName) {
        return new PagedIterable<>(this.listByRegistryAsync(resourceGroupName, registryName));
    }

    @Override
    public Mono<RegistryTask> getByRegistryAsync(
        String resourceGroupName, String registryName, String taskName, boolean includeSecrets) {
        if (includeSecrets) {
            return this
                .registryManager
                .serviceClient()
                .getTasks()
                .getDetailsAsync(resourceGroupName, registryName, taskName)
                .map(taskInner -> new RegistryTaskImpl(registryManager, taskInner));
        } else {
            return this
                .registryManager
                .serviceClient()
                .getTasks()
                .getAsync(resourceGroupName, registryName, taskName)
                .map(taskInner -> new RegistryTaskImpl(registryManager, taskInner));
        }
    }

    @Override
    public RegistryTask getByRegistry(
        String resourceGroupName, String registryName, String taskName, boolean includeSecrets) {
        return this.getByRegistryAsync(resourceGroupName, registryName, taskName, includeSecrets).block();
    }

    @Override
    public Mono<Void> deleteByRegistryAsync(String resourceGroupName, String registryName, String taskName) {
        return this.registryManager.serviceClient().getTasks().deleteAsync(resourceGroupName, registryName, taskName);
    }

    @Override
    public void deleteByRegistry(String resourceGroupName, String registryName, String taskName) {
        this.deleteByRegistryAsync(resourceGroupName, registryName, taskName).block();
    }

    private RegistryTaskImpl wrapModel(TaskInner innerModel) {
        return new RegistryTaskImpl(this.registryManager, innerModel);
    }

    public TasksClient inner() {
        return this.registryManager.serviceClient().getTasks();
    }
}
