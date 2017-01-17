/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

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
    public PagedList<GenericResource> list() {
        return wrapList(this.serviceClient.resources().list());
    }

    @Override
    public PagedList<GenericResource> listByGroup(String groupName) {
        return wrapList(this.serviceClient.resourceGroups().listResources(groupName));
    }

    @Override
    public PagedList<GenericResource> listByTag(String resourceGroupName, String tagName, String tagValue) {
        return wrapList(this.serviceClient.resourceGroups().listResources(
                resourceGroupName, Utils.createOdataFilterForTags(tagName, tagValue), null, null));
    }

    @Override
    public GenericResource.DefinitionStages.Blank define(String name) {
        return new GenericResourceImpl(
                name,
                new GenericResourceInner(),
                this.innerCollection,
                this.myManager.providers(),
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
    public boolean checkExistenceById(String id) {
        String apiVersion = getApiVersionFromId(id).toBlocking().single();
        return innerCollection.checkExistenceById(id, apiVersion);
    }

    @Override
    public GenericResource getById(String id) {
        Provider provider = myManager.providers().getByName(ResourceUtils.resourceProviderFromResourceId(id));
        String apiVersion = ResourceUtils.defaultApiVersion(id, provider);
        return wrapModel(this.innerCollection.getById(id, apiVersion)).withApiVersion(apiVersion);
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
                this.myManager.providers(),
                serviceClient,
                this.myManager);

        return resource.withExistingResourceGroup(resourceGroupName)
                .withProviderNamespace(resourceProviderNamespace)
                .withParentResourcePath(parentResourcePath)
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
                this.myManager.providers(),
                this.serviceClient,
                this.myManager)
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
        return new GenericResourceImpl(
                inner.id(),
                inner,
                this.innerCollection,
                this.myManager.providers(),
                this.serviceClient,
                this.myManager)
                .withExistingResourceGroup(ResourceUtils.groupFromResourceId(inner.id()))
                .withProviderNamespace(ResourceUtils.resourceProviderFromResourceId(inner.id()))
                .withResourceType(ResourceUtils.resourceTypeFromResourceId(inner.id()))
                .withParentResourceId(ResourceUtils.parentResourceIdFromResourceId(inner.id()));
    }

    @Override
    public GenericResource getByGroup(String groupName, String name) {
        // Not needed, can't be supported, provided only to satisfy GroupableResourceImpl's requirements
        throw new UnsupportedOperationException("Get just by resource group and name is not supported. Please use other overloads.");
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        // Not needed, can't be supported, provided only to satisfy GroupableResourceImpl's requirements
        throw new UnsupportedOperationException("Delete just by resource group and name is not supported. Please use other overloads.");
    }

    @Override
    public Completable deleteByIdAsync(final String id) {
        return getApiVersionFromId(id)
                .flatMap(new Func1<String, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(String apiVersion) {
                        return innerCollection.deleteByIdAsync(id, apiVersion);
                    }
                }).toCompletable();
    }

    private Observable<String> getApiVersionFromId(final String id) {
        return myManager.providers().getByNameAsync(ResourceUtils.resourceProviderFromResourceId(id))
                .map(new Func1<Provider, String>() {
                    @Override
                    public String call(Provider provider) {
                        return ResourceUtils.defaultApiVersion(id, provider);
                    }
                });
    }
}
