/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.GenericResource;
import com.azure.management.resources.GenericResources;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.ResourcesMoveInfo;
import com.azure.management.resources.Provider;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import com.azure.management.resources.models.GenericResourceInner;
import com.azure.management.resources.models.ResourcesInner;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * Implementation of the {@link GenericResources}.
 */
final class GenericResourcesImpl
        extends GroupableResourcesImpl<
        GenericResource,
        GenericResourceImpl,
        GenericResourceInner,
        ResourcesInner,
        ResourceManager>
        implements GenericResources {

    GenericResourcesImpl(ResourceManager resourceManager) {
        super(resourceManager.inner().resources(), resourceManager);
    }

    @Override
    public PagedIterable<GenericResource> list() {
        return wrapList(this.manager().inner().resources().list()
                .mapPage(res -> (GenericResourceInner) res));
    }

    @Override
    public PagedIterable<GenericResource> listByResourceGroup(String groupName) {
        return wrapList(this.manager().inner().resources().listByResourceGroup(groupName)
                .mapPage(res -> (GenericResourceInner) res));
    }

    @Override
    public PagedIterable<GenericResource> listByTag(String resourceGroupName, String tagName, String tagValue) {
        return wrapList(this.manager().inner().resources().listByResourceGroup(resourceGroupName,
                Utils.createOdataFilterForTags(tagName, tagValue), null, null)
                .mapPage(res -> (GenericResourceInner) res));
    }

    @Override
    public PagedFlux<GenericResource> listByTagAsync(String resourceGroupName, String tagName, String tagValue) {
        return wrapPageAsync(this.manager().inner().resources().listByResourceGroupAsync(resourceGroupName,
                Utils.createOdataFilterForTags(tagName, tagValue), null, null)
                .mapPage(res -> (GenericResourceInner) res));
    }

    @Override
    public GenericResource.DefinitionStages.Blank define(String name) {
        return new GenericResourceImpl(
                name,
                new GenericResourceInner(),
                this.manager());
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) {
        return this.inner().checkExistence(
                resourceGroupName,
                resourceProviderNamespace,
                parentResourcePath,
                resourceType,
                resourceName,
                apiVersion);
    }

    @Override
    public boolean checkExistenceById(String id) {
        String apiVersion = getApiVersionFromId(id).block();
        return this.inner().checkExistenceById(id, apiVersion);
    }

    @Override
    public GenericResource getById(String id) {
        Provider provider = this.manager().providers().getByName(ResourceUtils.resourceProviderFromResourceId(id));
        String apiVersion = ResourceUtils.defaultApiVersion(id, provider);
        return wrapModel(this.inner().getById(id, apiVersion)).withApiVersion(apiVersion);
    }

    @Override
    public GenericResource get(
            String resourceGroupName,
            String providerNamespace,
            String resourceType,
            String name) {

        PagedIterable<GenericResource> genericResources = this.listByResourceGroup(resourceGroupName);
        for (GenericResource resource : genericResources) {
            if (resource.name().equalsIgnoreCase(name)
                    && resource.resourceProviderNamespace().equalsIgnoreCase(providerNamespace)
                    && resource.resourceType().equalsIgnoreCase(resourceType)) {
                return resource;
            }
        }
        return null;
    }

    @Override
    public GenericResource get(
            String resourceGroupName,
            String resourceProviderNamespace,
            String parentResourcePath,
            String resourceType,
            String resourceName,
            String apiVersion) {

        // Correct for auto-gen'd API's treatment parent path as required even though it makes sense only for child resources
        if (parentResourcePath == null) {
            parentResourcePath = "";
        }

        GenericResourceInner inner = this.inner().get(
                resourceGroupName,
                resourceProviderNamespace,
                parentResourcePath,
                resourceType,
                resourceName,
                apiVersion);
        GenericResourceImpl resource = new GenericResourceImpl(
                resourceName,
                inner,
                this.manager());

        return resource.withExistingResourceGroup(resourceGroupName)
                .withProviderNamespace(resourceProviderNamespace)
                .withParentResourcePath(parentResourcePath)
                .withResourceType(resourceType)
                .withApiVersion(apiVersion);
    }

    @Override
    public void moveResources(String sourceResourceGroupName, ResourceGroup targetResourceGroup, List<String> resources) {
        this.moveResourcesAsync(sourceResourceGroupName, targetResourceGroup, resources).block();
    }

    @Override
    public Mono<Void> moveResourcesAsync(String sourceResourceGroupName, ResourceGroup targetResourceGroup, List<String> resources) {
        ResourcesMoveInfo moveInfo = new ResourcesMoveInfo();
        moveInfo.setTargetResourceGroup(targetResourceGroup.id());
        moveInfo.setResources(resources);
        return this.inner().moveResourcesAsync(sourceResourceGroupName, moveInfo);
    }

    public void delete(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) {
        deleteAsync(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, apiVersion).block();
    }

    @Override
    public Mono<Void> deleteAsync(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) {
        return this.inner().deleteAsync(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, apiVersion);
    }


    @Override
    protected GenericResourceImpl wrapModel(String id) {
        return new GenericResourceImpl(id, new GenericResourceInner(), this.manager())
                .withExistingResourceGroup(ResourceUtils.groupFromResourceId(id))
                .withProviderNamespace(ResourceUtils.resourceProviderFromResourceId(id))
                .withResourceType(ResourceUtils.resourceTypeFromResourceId(id))
                .withParentResourceId(ResourceUtils.parentResourceIdFromResourceId(id));
    }

    @Override
    protected GenericResourceImpl wrapModel(GenericResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new GenericResourceImpl(inner.getId(), inner, this.manager())
                .withExistingResourceGroup(ResourceUtils.groupFromResourceId(inner.getId()))
                .withProviderNamespace(ResourceUtils.resourceProviderFromResourceId(inner.getId()))
                .withResourceType(ResourceUtils.resourceTypeFromResourceId(inner.getId()))
                .withParentResourceId(ResourceUtils.parentResourceIdFromResourceId(inner.getId()));
    }

    @Override
    public Mono<GenericResourceInner> getInnerAsync(String groupName, String name) {
        // Not needed, can't be supported, provided only to satisfy GroupableResourceImpl's requirements
        throw new UnsupportedOperationException("Get just by resource group and name is not supported. Please use other overloads.");
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        // Not needed, can't be supported, provided only to satisfy GroupableResourceImpl's requirements
        throw new UnsupportedOperationException("Delete just by resource group and name is not supported. Please use other overloads.");
    }

    @Override
    public Mono<Void> deleteByIdAsync(final String id) {
        final ResourcesInner inner = this.inner();
        return getApiVersionFromId(id)
                .flatMap(apiVersion -> inner.deleteByIdAsync(id, apiVersion));
    }

    private Mono<String> getApiVersionFromId(final String id) {
        return this.manager().providers().getByNameAsync(ResourceUtils.resourceProviderFromResourceId(id))
                .map(provider -> ResourceUtils.defaultApiVersion(id, provider));
    }

    @Override
    public PagedFlux<GenericResource> listAsync() {
        return wrapPageAsync(this.inner().listAsync()
                .mapPage(res -> (GenericResourceInner) res));
    }

    @Override
    public PagedFlux<GenericResource> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.manager().inner().resources().listByResourceGroupAsync(resourceGroupName)
                .mapPage(res -> (GenericResourceInner) res));
    }
}
