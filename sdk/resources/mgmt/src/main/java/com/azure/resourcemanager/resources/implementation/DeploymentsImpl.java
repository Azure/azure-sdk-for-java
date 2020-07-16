// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.Deployments;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.SupportsGettingByResourceGroupImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluent.inner.DeploymentExtendedInner;
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
        return this.manager().inner().getDeployments()
            .listByResourceGroup(groupName).mapPage(inner -> createFluentModel(inner));
    }

    @Override
    public Deployment getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<Deployment> getByNameAsync(String name) {
        return this.manager().inner().getDeployments().getAtTenantScopeAsync(name)
            .map(inner -> new DeploymentImpl(inner, inner.name(), this.resourceManager));
    }

    @Override
    public Mono<Deployment> getByResourceGroupAsync(String groupName, String name) {
        return this.manager().inner().getDeployments()
            .getByResourceGroupAsync(groupName, name).map(deploymentExtendedInner -> {
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
    public Mono<Void> deleteByResourceGroupAsync(String groupName, String name) {
        return this.manager().inner().getDeployments().deleteAsync(groupName, name);
    }

    @Override
    public DeploymentImpl define(String name) {
        return createFluentModel(name);
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String deploymentName) {
        return this.manager().inner().getDeployments().checkExistence(resourceGroupName, deploymentName);
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
        return PagedConverter.flatMapPage(this.manager().resourceGroups().listAsync(),
            resourceGroup -> listByResourceGroupAsync(resourceGroup.name()));
    }


    @Override
    public PagedFlux<Deployment> listByResourceGroupAsync(String resourceGroupName) {
        final DeploymentsClient client = this.manager().inner().getDeployments();
        return client.listByResourceGroupAsync(resourceGroupName)
            .mapPage(deploymentExtendedInner -> createFluentModel(deploymentExtendedInner));
    }
}
