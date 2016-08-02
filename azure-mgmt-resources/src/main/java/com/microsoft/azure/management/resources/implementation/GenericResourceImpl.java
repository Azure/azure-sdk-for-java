/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.Plan;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

/**
 * The implementation for {@link GenericResource} and its nested interfaces.
 */
class GenericResourceImpl
    extends GroupableResourceImpl<
        GenericResource,
        GenericResourceInner,
        GenericResourceImpl,
        ResourceManager>
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
        return createResourceAsync(new ServiceCallback<Resource>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
            }

            @Override
            public void success(ServiceResponse<Resource> result) {
                callback.success(new ServiceResponse<>((GenericResource) result.getBody(), result.getResponse()));
            }
        });
    }

    @Override
    public GenericResourceImpl apply() throws Exception {
        return create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<GenericResource> callback) {
        return createAsync(callback);
    }

    // CreatorTaskGroup.ResourceCreator implementation

    @Override
    public Resource createResource() throws Exception {
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
        return this;
    }

    @Override
    public ServiceCall createResourceAsync(final ServiceCallback<Resource> callback) {
        final GenericResourceImpl self = this;
        return client.createOrUpdateAsync(
                resourceGroupName(),
                resourceProviderNamespace,
                parentResourceId,
                resourceType,
                key(),
                apiVersion,
                inner(),
                new ServiceCallback<GenericResourceInner>() {
                    @Override
                    public void failure(Throwable t) {
                        callback.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<GenericResourceInner> response) {
                        self.setInner(response.getBody());
                        callback.success(new ServiceResponse<Resource>(self, response.getResponse()));
                    }
                });
    }
}
