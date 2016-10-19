/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.Plan;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.ProviderResourceType;
import com.microsoft.azure.management.resources.Providers;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * The implementation for {@link GenericResource} and its nested interfaces.
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
    private final ResourcesInner resourceClient;
    private final Providers providersClient;
    private String resourceProviderNamespace;
    private String parentResourceId;
    private String resourceType;
    private String apiVersion;

    GenericResourceImpl(String key,
                        GenericResourceInner innerModel,
                        ResourcesInner innerCollection,
                        Providers providerClient,
                        final ResourceManagementClientImpl serviceClient,
                        final ResourceManager resourceManager) {
        super(key, innerModel, resourceManager);
        resourceProviderNamespace = ResourceUtils.resourceProviderFromResourceId(innerModel.id());
        parentResourceId = ResourceUtils.parentResourcePathFromResourceId(innerModel.id());
        resourceType = ResourceUtils.resourceTypeFromResourceId(innerModel.id());
        this.resourceClient = innerCollection;
        this.providersClient = providerClient;
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
    public GenericResource refresh() {
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

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Observable<GenericResource> createResourceAsync() {
        final GenericResourceImpl self = this;
        Observable<String> observable = Observable.just(apiVersion);
        if (apiVersion == null) {
            observable = providersClient.getByNameAsync(resourceProviderNamespace)
                    .map(new Func1<Provider, String>() {
                        @Override
                        public String call(Provider provider) {
                            for (ProviderResourceType type : provider.resourceTypes()) {
                                if (resourceType().equalsIgnoreCase(type.resourceType())) {
                                    return type.apiVersions().get(0);
                                }
                            }
                            // Use the first available one as default
                            return provider.resourceTypes().get(0).apiVersions().get(0);
                        }
                    });
        }
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
                                ResourceUtils.relativePathFromResourceId(parentResourceId),
                                resourceType,
                                name,
                                api,
                                inner())
                                .subscribeOn(Schedulers.io())
                                .map(innerToFluentMap(self));
                    }
                });
    }
}
