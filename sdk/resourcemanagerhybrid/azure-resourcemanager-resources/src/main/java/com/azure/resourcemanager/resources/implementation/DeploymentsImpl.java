// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.Deployments;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.SupportsGettingByResourceGroupImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluent.models.DeploymentExtendedInner;
import com.azure.resourcemanager.resources.fluent.DeploymentsClient;
import reactor.core.publisher.Mono;

/**
 * The implementation for {@link Deployments}.
 */
public final class DeploymentsImpl
        extends SupportsGettingByResourceGroupImpl<Deployment>
        implements Deployments,
        HasManager<ResourceManager> {

    private final ResourceManager resourceManager;

    public DeploymentsImpl(final ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public PagedIterable<Deployment> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedIterable<Deployment> listByResourceGroup(String groupName) {
        return PagedConverter.mapPage(this.manager().serviceClient().getDeployments()
            .listByResourceGroup(groupName), inner -> createFluentModel(inner));
    }

    @Override
    public Deployment getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<Deployment> getByNameAsync(String name) {
        return this.manager().serviceClient().getDeployments().getAtTenantScopeAsync(name)
            .map(inner -> new DeploymentImpl(inner, inner.name(), this.resourceManager));
    }

    @Override
    public Mono<Deployment> getByResourceGroupAsync(String resourceGroupName, String name) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(name)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'name' is required and cannot be null."));
        }
        return this.manager().serviceClient().getDeployments()
            .getByResourceGroupAsync(resourceGroupName, name).map(deploymentExtendedInner -> {
                if (deploymentExtendedInner != null) {
                    return createFluentModel(deploymentExtendedInner);
                } else {
                    return null;
                }
            });
    }

    @Override
    public void deleteByResourceGroup(String groupName, String name) {
        deleteByResourceGroupAsync(groupName, name).block();
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
        return this.manager().serviceClient().getDeployments().deleteAsync(resourceGroupName, name);
    }

    @Override
    public DeploymentImpl define(String name) {
        return createFluentModel(name);
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String deploymentName) {
        return this.manager().serviceClient().getDeployments().checkExistence(resourceGroupName, deploymentName);
    }

    protected DeploymentImpl createFluentModel(String name) {
        return new DeploymentImpl(new DeploymentExtendedInner(), name, this.resourceManager);
    }

    protected DeploymentImpl createFluentModel(DeploymentExtendedInner deploymentExtendedInner) {
        return new DeploymentImpl(deploymentExtendedInner, deploymentExtendedInner.name(), this.resourceManager);
    }

    @Override
    public Deployment getById(String id) {
        return this.getByResourceGroup(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return deleteByResourceGroupAsync(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public ResourceManager manager() {
        return this.resourceManager;
    }

    @Override
    public PagedFlux<Deployment> listAsync() {
        return PagedConverter.mergePagedFlux(this.manager().resourceGroups().listAsync(),
            resourceGroup -> listByResourceGroupAsync(resourceGroup.name()));
    }


    @Override
    public PagedFlux<Deployment> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        final DeploymentsClient client = this.manager().serviceClient().getDeployments();
        return PagedConverter.mapPage(client.listByResourceGroupAsync(resourceGroupName),
            deploymentExtendedInner -> createFluentModel(deploymentExtendedInner));
    }
}
