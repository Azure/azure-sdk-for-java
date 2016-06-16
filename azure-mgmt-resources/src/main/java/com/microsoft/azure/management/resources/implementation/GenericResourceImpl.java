/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.implementation.api.Plan;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourcesInner;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;

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
        GenericResource.UpdateWithApiVersion,
        GenericResource.Update {
    private final ResourcesInner client;
    private String resourceProviderNamespace;
    private String parentResourceId;
    private String resourceType;
    private String apiVersion;

    GenericResourceImpl(String key,
                        GenericResourceInner innerModel,
                        ResourcesInner client,
                        final ResourceManagementClientImpl serviceClient,
                        final ResourceManager resourceManager) {
        super(key, innerModel, resourceManager);
        this.client = client;
    }

    @Override
    public String resourceProviderNamespace() {
        return resourceProviderNamespace;
    }

    @Override
    public String parentResourceId() {
        return parentResourceId;
    }

    @Override
    public String resourceType() {
        return resourceType;
    }

    @Override
    public String apiVersion() {
        return apiVersion;
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

    public GenericResourceImpl withProperties(Object properties) {
            inner().withProperties(properties);
        return this;
    }

    @Override
    public GenericResourceImpl withParentResource(String parentResourceId) {
        this.parentResourceId = parentResourceId;
        return this;
    }

    public GenericResourceImpl withPlan(String name, String publisher, String product, String promotionCode) {
            inner().withPlan(new Plan().withName(name).withPublisher(publisher).withProduct(product).withPromotionCode(promotionCode));
        return this;
    }

    @Override
    public GenericResourceImpl withoutPlan() {
            inner().withPlan(null);
        return this;
    }

    @Override
    public GenericResourceImpl withProviderNamespace(String resourceProviderNamespace) {
        this.resourceProviderNamespace = resourceProviderNamespace;
        return this;
    }

    @Override
    public GenericResourceImpl withResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    @Override
    public GenericResourceImpl withApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Override
    public GenericResourceImpl create() throws Exception {
        createResource();
        return this;
    }

    @Override
    public ServiceCall createAsync(final ServiceCallback<GenericResource> callback) {
        return createResourceAsync(Utils.toVoidCallback(this, callback));
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
    }

    @Override
    protected ServiceCall createResourceAsync(final ServiceCallback<Void> callback) {
        return client.createOrUpdateAsync(
                resourceGroupName(),
                resourceProviderNamespace,
                parentResourceId,
                resourceType,
                key(),
                apiVersion,
                inner(),
                Utils.fromVoidCallback(this, callback));
    }

    @Override
    public GenericResourceImpl apply() throws Exception {
        return create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<GenericResource> callback) {
        return createAsync(callback);
    }
}
