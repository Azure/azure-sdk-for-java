/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.management.resources.GenericResource;
import com.azure.management.resources.Plan;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.resources.models.GenericResourceInner;
import com.azure.management.resources.models.ResourceManagementClientImpl;
import com.azure.management.resources.models.ResourcesInner;
import reactor.core.publisher.Mono;

/**
 * The implementation for GenericResource and its nested interfaces.
 */
final class GenericResourceImpl
        extends GroupableResourceImpl<
        GenericResource,
        GenericResourceInner,
        GenericResourceImpl,
        ResourceManager>
        implements
        GenericResource,
        GenericResource.Definition,
        GenericResource.UpdateStages.WithApiVersion,
        GenericResource.Update {
    private String resourceProviderNamespace;
    private String parentResourcePath;
    private String resourceType;
    private String apiVersion;

    GenericResourceImpl(String key,
                        GenericResourceInner innerModel,
                        final ResourceManager resourceManager) {
        super(key, innerModel, resourceManager);
        resourceProviderNamespace = ResourceUtils.resourceProviderFromResourceId(innerModel.getId());
        resourceType = ResourceUtils.resourceTypeFromResourceId(innerModel.getId());
        parentResourcePath = ResourceUtils.parentRelativePathFromResourceId(innerModel.getId());
    }

    @Override
    public String resourceProviderNamespace() {
        return resourceProviderNamespace;
    }

    @Override
    public String parentResourcePath() {
        if (parentResourcePath == null) {
            return "";
        }
        return parentResourcePath;
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
        return inner().getPlan();
    }

    @Override
    public Object properties() {
        return inner().getProperties();
    }

    @Override
    protected Mono<GenericResourceInner> getInnerAsync() {
        return this.manager().inner().resources().getAsync(
                resourceGroupName(),
                resourceProviderNamespace(),
                parentResourcePath(),
                resourceType(),
                this.name(),
                this.apiVersion());
    }

    public GenericResourceImpl withProperties(Object properties) {
        inner().setProperties(properties);
        return this;
    }

    @Override
    public GenericResourceImpl withParentResourceId(String parentResourceId) {
        return withParentResourcePath(ResourceUtils.relativePathFromResourceId(parentResourceId));
    }

    @Override
    public GenericResourceImpl withParentResourcePath(String parentResourcePath) {
        this.parentResourcePath = parentResourcePath;
        return this;
    }

    public GenericResourceImpl withPlan(String name, String publisher, String product, String promotionCode) {
        inner().setPlan(new Plan().setName(name).setPublisher(publisher).setProduct(product).setPromotionCode(promotionCode));
        return this;
    }

    @Override
    public GenericResourceImpl withoutPlan() {
        inner().setPlan(null);
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

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Mono<GenericResource> createResourceAsync() {
        final GenericResourceImpl self = this;
        Mono<String> observable = null;
        if (apiVersion != null) {
            observable = Mono.just(apiVersion);
        } else {
            final ResourceManagementClientImpl serviceClient = this.manager().inner();
            observable = this.manager().providers().getByNameAsync(resourceProviderNamespace)
                    .flatMap(provider -> {
                        String id;
                        if (!isInCreateMode()) {
                            id = inner().getId();
                        } else {
                            id = ResourceUtils.constructResourceId(
                                    serviceClient.getSubscriptionId(),
                                    resourceGroupName(),
                                    resourceProviderNamespace(),
                                    resourceType(),
                                    this.name(),
                                    parentResourcePath());
                        }
                        self.apiVersion = ResourceUtils.defaultApiVersion(id, provider);
                        return Mono.just(self.apiVersion);
                    });
        }
        final ResourcesInner resourceClient = this.manager().inner().resources();
        return observable
                .flatMap(api -> {
                    String name = this.name();
                    if (!isInCreateMode()) {
                        name = ResourceUtils.nameFromResourceId(inner().getId());
                    }
                    return resourceClient.createOrUpdateAsync(
                            resourceGroupName(),
                            resourceProviderNamespace,
                            parentResourcePath(),
                            resourceType,
                            name,
                            api,
                            inner())
                            .subscribeOn(SdkContext.getReactorScheduler())
                            .map(innerToFluentMap(self));
                });
    }
}
