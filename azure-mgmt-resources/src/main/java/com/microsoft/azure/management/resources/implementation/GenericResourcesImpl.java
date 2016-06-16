/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourcesInner;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.implementation.api.ResourcesMoveInfoInner;

import java.io.IOException;
import java.util.List;

/**
 * An instance of this class provides access to generic resources in Azure.
 */
final class GenericResourcesImpl
    implements GenericResources {

    private final ResourceManagementClientImpl serviceClient;
    private final ResourcesInner client;
    private final ResourceGroupsInner resourceGroupsInner;
    private final ResourceManager resourceManager;

    GenericResourcesImpl(ResourceManagementClientImpl serviceClient, ResourceManager resourceManager) {
        this.serviceClient = serviceClient;
        this.client = serviceClient.resources();
        this.resourceGroupsInner = serviceClient.resourceGroups();
        this.resourceManager = resourceManager;
    }

    @Override
    public PagedList<GenericResource> list() throws CloudException, IOException {
        return listIntern(null);
    }

    @Override
    public PagedList<GenericResource> listByGroup(String groupName) throws CloudException, IOException {
        return listIntern(groupName);
    }

    @Override
    public GenericResource.DefinitionBlank define(String name) {
        return new GenericResourceImpl(
                name,
                new GenericResourceInner(),
                client,
                serviceClient,
                this.resourceManager);
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) throws IOException, CloudException {
        return client.checkExistence(
                resourceGroupName,
                resourceProviderNamespace,
                parentResourcePath,
                resourceType,
                resourceName,
                apiVersion).getBody();
    }

    @Override
    public GenericResource get(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) throws CloudException, IOException {
        GenericResourceInner inner = client.get(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, apiVersion).getBody();
        GenericResourceImpl resource = new GenericResourceImpl(
                resourceName,
                inner,
                client,
                serviceClient,
                this.resourceManager);

        return resource.withExistingGroup(resourceGroupName)
                .withProviderNamespace(resourceProviderNamespace)
                .withParentResource(parentResourcePath)
                .withResourceType(resourceType)
                .withApiVersion(apiVersion);
    }

    @Override
    public void moveResources(String sourceResourceGroupName, ResourceGroup targetResourceGroup, List<String> resources) throws CloudException, IOException, InterruptedException {
        ResourcesMoveInfoInner moveInfo = new ResourcesMoveInfoInner();
        moveInfo.withTargetResourceGroup(targetResourceGroup.id());
        moveInfo.withResources(resources);
        client.moveResources(sourceResourceGroupName, moveInfo);
    }

    @Override
    public void delete(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) throws CloudException, IOException {
        client.delete(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, apiVersion);
    }

    private PagedList<GenericResource> listIntern(String groupName) throws IOException, CloudException {
        PagedListConverter<GenericResourceInner, GenericResource> converter = new PagedListConverter<GenericResourceInner, GenericResource>() {
            @Override
            public GenericResource typeConvert(GenericResourceInner genericResourceInner) {
                return new GenericResourceImpl(
                        genericResourceInner.id(),
                        genericResourceInner,
                        client,
                        serviceClient,
                        resourceManager)
                        .withExistingGroup(ResourceUtils.groupFromResourceId(genericResourceInner.id()))
                        .withProviderNamespace(ResourceUtils.resourceProviderFromResourceId(genericResourceInner.id()))
                        .withResourceType(ResourceUtils.resourceTypeFromResourceId(genericResourceInner.id()))
                        .withParentResource(ResourceUtils.parentResourcePathFromResourceId(genericResourceInner.id()));
            }
        };
        return converter.convert(resourceGroupsInner.listResources(groupName).getBody());
    }
}
