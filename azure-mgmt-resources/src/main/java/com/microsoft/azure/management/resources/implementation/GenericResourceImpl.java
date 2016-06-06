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
        GenericResource.DefinitionWithApiVersion,
        GenericResource.DefinitionCreatable,
        GenericResource.Update {
    private final ResourcesInner client;
    private final ResourceManagementClientImpl serviceClient;
    private String resourceProviderNamespace;
    private String parentResourceId;
    private String resourceType;
    private String apiVersion;

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
        inner().withProperties(properties);
        return this;
    }

    @Override
    public DefinitionWithPlan withParentResource(String parentResourceId) {
        this.parentResourceId = parentResourceId;
        return this;
    }

    @Override
    public DefinitionWithApiVersion withPlan(String name, String publisher, String product, String promotionCode) {
        inner().withPlan(new Plan().withName(name).withPublisher(publisher).withProduct(product).withPromotionCode(promotionCode));
        return this;
    }

    @Override
    public DefinitionWithApiVersion withoutPlan() {
        inner().withPlan(null);
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
    public DefinitionCreatable withApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
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
                apiVersion,
                inner()
        ).getBody();
        this.setInner(inner);
        this.resourceType = null;
        this.resourceProviderNamespace = null;
        this.parentResourceId = null;
    }

    @Override
    public Update update() throws Exception {
        return this;
    }

    @Override
    public GenericResource apply() throws Exception {
        return create();
    }
}
