/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.implementation.api.Plan;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourcesInner;

/**
 * The implementation for GenericResource and its nested interfaces.
 */
final class GenericResourceImpl
    extends GroupableResourceImpl<GenericResource, GenericResourceInner, GenericResourceImpl>
    implements
        GenericResource,
        GenericResource.DefinitionBlank,
        GenericResource.DefinitionWithGroup,
        GenericResource.DefinitionWithResourceType,
        GenericResource.DefinitionWithProviderNamespace,
        GenericResource.DefinitionWithOrWithoutParentResource,
        GenericResource.DefinitionWithPlan,
        GenericResource.DefinitionCreatable {
    private final ResourcesInner client;
    private final ResourceManagementClientImpl serviceClient;
    private String resourceProviderNamespace;
    private String parentResourceId;
    private String resourceType;

    GenericResourceImpl(String key,
                        GenericResourceInner innerModel,
                        ResourcesInner client,
                        final ResourceManagementClientImpl serviceClient) {
        super(key, innerModel, new ResourceGroupsImpl(serviceClient));
        this.client = client;
        this.serviceClient = serviceClient;
    }

    @Override
    public Plan plan() {
        return inner().plan();
    }

    @Override
    public Object properties() {
        return inner().properties();
    }

    @Override
    public GenericResource refresh() throws Exception {
        return null;
    }

    public DefinitionCreatable withProperties(Object properties) {
        inner().setProperties(properties);
        return this;
    }

    @Override
    public DefinitionWithPlan withParentResource(String parentResourceId) {
        this.parentResourceId = parentResourceId;
        return this;
    }

    @Override
    public DefinitionCreatable withPlan(String name, String publisher, String product, String promotionCode) {
        inner().setPlan(new Plan().setName(name).setPublisher(publisher).setProduct(product).setPromotionCode(promotionCode));
        return this;
    }

    @Override
    public DefinitionWithOrWithoutParentResource withProviderNamespace(String resourceProviderNamespace) {
        this.resourceProviderNamespace = resourceProviderNamespace;
        return this;
    }

    @Override
    public DefinitionWithProviderNamespace withResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    @Override
    public GenericResource create() throws Exception {
        createResource();
        return this;
    }

    @Override
    protected void createResource() throws Exception {
        GenericResourceInner inner = client.createOrUpdate(
                resourceGroupName(),
                resourceProviderNamespace,
                parentResourceId,
                resourceType,
                key(),
                serviceClient.apiVersion(),
                inner()
        ).getBody();
        this.setInner(inner);
        this.resourceType = null;
        this.resourceProviderNamespace = null;
        this.parentResourceId = null;
    }
}
