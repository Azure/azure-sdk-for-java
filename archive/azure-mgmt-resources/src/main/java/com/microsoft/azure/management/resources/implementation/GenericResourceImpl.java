/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.Plan;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import rx.Observable;
import rx.functions.Func1;

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
        resourceProviderNamespace = ResourceUtils.resourceProviderFromResourceId(innerModel.id());
        resourceType = ResourceUtils.resourceTypeFromResourceId(innerModel.id());
        parentResourcePath = ResourceUtils.parentRelativePathFromResourceId(innerModel.id());
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
        return inner().plan();
    }

    @Override
    public Object properties() {
        return inner().properties();
    }

    @Override
    protected Observable<GenericResourceInner> getInnerAsync() {
        return this.manager().inner().resources().getAsync(
                resourceGroupName(),
                resourceProviderNamespace(),
                parentResourcePath(),
                resourceType(),
                name(),
                apiVersion());
    }

    public GenericResourceImpl withProperties(Object properties) {
            inner().withProperties(properties);
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

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Observable<GenericResource> createResourceAsync() {
        final GenericResourceImpl self = this;
        Observable<String> observable = Observable.just(apiVersion);
        if (apiVersion == null) {
            final ResourceManagementClientImpl serviceClient = this.manager().inner();
            observable = this.manager().providers().getByNameAsync(resourceProviderNamespace)
                    .map(new Func1<Provider, String>() {
                        @Override
                        public String call(Provider provider) {
                            String id;
                            if (!isInCreateMode()) {
                                id = inner().id();
                            } else {
                                id = ResourceUtils.constructResourceId(
                                        serviceClient.subscriptionId(),
                                        resourceGroupName(),
                                        resourceProviderNamespace(),
                                        resourceType(),
                                        name(),
                                        parentResourcePath());
                            }
                            self.apiVersion = ResourceUtils.defaultApiVersion(id, provider);
                            return self.apiVersion;
                        }
                    });
        }
        final ResourcesInner resourceClient = this.manager().inner().resources();
        return observable
                .flatMap(new Func1<String, Observable<GenericResource>>() {
                    @Override
                    public Observable<GenericResource> call(String api) {
                        String name = name();
                        if (!isInCreateMode()) {
                            name = ResourceUtils.nameFromResourceId(inner().id());
                        }
                        return resourceClient.createOrUpdateAsync(
                                resourceGroupName(),
                                resourceProviderNamespace,
                                parentResourcePath(),
                                resourceType,
                                name,
                                api,
                                inner())
                                .subscribeOn(SdkContext.getRxScheduler())
                                .map(innerToFluentMap(self));
                    }
                });
    }
}
