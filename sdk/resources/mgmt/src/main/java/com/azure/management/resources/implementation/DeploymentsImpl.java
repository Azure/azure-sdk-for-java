/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.Deployment;
import com.azure.management.resources.Deployments;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.collection.implementation.SupportsGettingByResourceGroupImpl;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import com.azure.management.resources.models.DeploymentExtendedInner;
import com.azure.management.resources.models.DeploymentsInner;
import reactor.core.publisher.Mono;

/**
 * The implementation for {@link Deployments}.
 */
final class DeploymentsImpl
        extends SupportsGettingByResourceGroupImpl<Deployment>
        implements Deployments,
        HasManager<ResourceManager> {

    private final ResourceManager resourceManager;

    DeploymentsImpl(final ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public PagedIterable<Deployment> list() {
        return this.manager().inner().deployments().list()
                .mapPage(inner -> new DeploymentImpl(inner, inner.getName(), resourceManager));
    }

    @Override
    public PagedIterable<Deployment> listByResourceGroup(String groupName) {
        return this.manager().inner().deployments().listByResourceGroup(groupName).mapPage(inner -> createFluentModel(inner));
    }

    @Override
    public Deployment getByName(String name) {
        for (ResourceGroup group : this.resourceManager.resourceGroups().list()) {
            DeploymentExtendedInner inner = this.manager().inner().deployments().getAtManagementGroupScope(group.name(), name);
            if (inner != null) {
                return createFluentModel(inner);
            }
        }
        return null;
    }

    @Override
    public Mono<Deployment> getByNameAsync(String name) {
        // TODO: Add this
        return null;
    }

    @Override
    public Mono<Deployment> getByResourceGroupAsync(String groupName, String name) {
        return this.manager().inner().deployments().getByResourceGroupAsync(groupName, name).map(deploymentExtendedInner -> {
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
        return this.manager().inner().deployments().deleteAsync(groupName, name);
    }

    @Override
    public DeploymentImpl define(String name) {
        return createFluentModel(name);
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String deploymentName) {
        // return this.getManager().getInner().deployments().checkExistence(resourceGroupName, deploymentName);
        return true;
    }

    protected DeploymentImpl createFluentModel(String name) {
        return new DeploymentImpl(new DeploymentExtendedInner(), name, this.resourceManager);
    }

    protected DeploymentImpl createFluentModel(DeploymentExtendedInner deploymentExtendedInner) {
        return new DeploymentImpl(deploymentExtendedInner, deploymentExtendedInner.getName(), this.resourceManager);
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
        final DeploymentsInner client = this.manager().inner().deployments();
        return client.listByResourceGroupAsync(resourceGroupName).mapPage(deploymentExtendedInner -> createFluentModel(deploymentExtendedInner));
    }
}
