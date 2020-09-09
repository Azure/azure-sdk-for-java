// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.fluent.TasksClient;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/** Interface to define the RegistryTasks collection. */
@Fluent()
public interface RegistryTasks extends HasInner<TasksClient>, SupportsCreating<RegistryTask.DefinitionStages.Blank> {
    /**
     * Lists the tasks in a registry asynchronously.
     *
     * @param resourceGroupName the resource group of the parent registry.
     * @param registryName the name of the parent registry.
     * @return the tasks with parent registry registry.
     */
    PagedFlux<RegistryTask> listByRegistryAsync(String resourceGroupName, String registryName);

    /**
     * Lists the tasks in a registry.
     *
     * @param resourceGroupName the resource group of the parent registry.
     * @param registryName the name of the parent registry.
     * @return the tasks with parent registry registry.
     */
    PagedIterable<RegistryTask> listByRegistry(String resourceGroupName, String registryName);

    /**
     * Gets a task in a registry asynchronously.
     *
     * @param resourceGroupName the resource group of the parent registry.
     * @param registryName the name of the parent registry.
     * @param taskName the name of the task.
     * @param includeSecrets whether to include secrets or not.
     * @return the task
     */
    Mono<RegistryTask> getByRegistryAsync(
        String resourceGroupName, String registryName, String taskName, boolean includeSecrets);

    /**
     * Gets a task in a registry.
     *
     * @param resourceGroupName the resource group of the parent registry.
     * @param registryName the name of the parent registry.
     * @param taskName the name of the task.
     * @param includeSecrets whether to include secrets or not.
     * @return the task
     */
    RegistryTask getByRegistry(String resourceGroupName, String registryName, String taskName, boolean includeSecrets);

    /**
     * Deletes a task in a registry asynchronously.
     *
     * @param resourceGroupName the resource group of the parent registry.
     * @param registryName the name of the parent registry.
     * @param taskName the name of the task.
     * @return the handle to the request.
     */
    Mono<Void> deleteByRegistryAsync(String resourceGroupName, String registryName, String taskName);

    /**
     * Deletes a task in a registry.
     *
     * @param resourceGroupName the resource group of the parent registry.
     * @param registryName the name of the parent registry.
     * @param taskName the name of the task.
     */
    void deleteByRegistry(String resourceGroupName, String registryName, String taskName);
}
