/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Observable;

import java.util.List;

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

    private final ResourceManagementClientImpl serviceClient;

    GenericResourcesImpl(ResourceManagementClientImpl serviceClient, ResourceManager resourceManager) {
        super(serviceClient.resources(), resourceManager);
        this.serviceClient = serviceClient;
    }

    @Override
    public PagedList<GenericResource> listByGroup(String groupName) {
        return wrapList(this.serviceClient.resourceGroups().listResources(groupName));
    }

    @Override
    public GenericResource.DefinitionStages.Blank define(String name) {
        return new GenericResourceImpl(
                name,
                new GenericResourceInner(),
                this.innerCollection,
                serviceClient,
                super.myManager);
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) {
        return this.innerCollection.checkExistence(
                resourceGroupName,
                resourceProviderNamespace,
                parentResourcePath,
                resourceType,
                resourceName,
                apiVersion);
    }

    @Override
    public GenericResource getById(String id) {
        return this.get(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.resourceProviderFromResourceId(id),
                ResourceUtils.resourceTypeFromResourceId(id),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public GenericResource get(
            String resourceGroupName,
            String providerNamespace,
            String resourceType,
            String name) {

        PagedList<GenericResource> genericResources = this.listByGroup(resourceGroupName);
        for (GenericResource resource : genericResources) {
            if (resource.name().equalsIgnoreCase(name)
                    && resource.resourceProviderNamespace().equalsIgnoreCase(providerNamespace)
                    && resource.resourceType().equalsIgnoreCase(resourceType)) {
                return resource;
            }
        }
        throw new CloudException("Generic resource not found.");
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

        GenericResourceInner inner = this.innerCollection.get(
                resourceGroupName,
                resourceProviderNamespace,
                parentResourcePath,
                resourceType,
                resourceName,
                apiVersion);
        GenericResourceImpl resource = new GenericResourceImpl(
                resourceName,
                inner,
                this.innerCollection,
                serviceClient,
                this.myManager);

        return resource.withExistingResourceGroup(resourceGroupName)
                .withProviderNamespace(resourceProviderNamespace)
                .withParentResource(parentResourcePath)
                .withResourceType(resourceType)
                .withApiVersion(apiVersion);
    }

    @Override
    public void moveResources(String sourceResourceGroupName, ResourceGroup targetResourceGroup, List<String> resources) {
        ResourcesMoveInfoInner moveInfo = new ResourcesMoveInfoInner();
        moveInfo.withTargetResourceGroup(targetResourceGroup.id());
        moveInfo.withResources(resources);
        this.innerCollection.moveResources(sourceResourceGroupName, moveInfo);
    }

    @Override
    public void delete(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) {
        this.innerCollection.delete(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, apiVersion);
    }

    @Override
    protected GenericResourceImpl wrapModel(String id) {
        return new GenericResourceImpl(
                id,
                new GenericResourceInner(),
                this.innerCollection,
                this.serviceClient,
                this.myManager)
                .withExistingResourceGroup(ResourceUtils.groupFromResourceId(id))
                .withProviderNamespace(ResourceUtils.resourceProviderFromResourceId(id))
                .withResourceType(ResourceUtils.resourceTypeFromResourceId(id))
                .withParentResource(ResourceUtils.parentResourcePathFromResourceId(id));
    }

    @Override
    protected GenericResourceImpl wrapModel(GenericResourceInner inner) {
        return new GenericResourceImpl(
                inner.id(),
                inner,
                this.innerCollection,
                this.serviceClient,
                this.myManager)
                .withExistingResourceGroup(ResourceUtils.groupFromResourceId(inner.id()))
                .withProviderNamespace(ResourceUtils.resourceProviderFromResourceId(inner.id()))
                .withResourceType(ResourceUtils.resourceTypeFromResourceId(inner.id()))
                .withParentResource(ResourceUtils.parentResourcePathFromResourceId(inner.id()));
    }

    @Override
    public GenericResource getByGroup(String groupName, String name) {
        // Not needed, can't be supported, provided only to satisfy GroupableResourceImpl's requirements
        throw new UnsupportedOperationException("Get just by resource group and name is not supported. Please use other overloads.");
    }

    @Override
    public Observable<Void> deleteAsync(String groupName, String name) {
        // Not needed, can't be supported, provided only to satisfy GroupableResourceImpl's requirements
        throw new UnsupportedOperationException("Delete just by resource group and name is not supported. Please use other overloads.");
    }
}
