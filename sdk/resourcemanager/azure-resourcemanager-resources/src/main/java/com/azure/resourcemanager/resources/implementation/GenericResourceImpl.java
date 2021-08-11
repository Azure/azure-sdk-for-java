// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.Identity;
import com.azure.resourcemanager.resources.models.Plan;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluent.models.GenericResourceInner;
import com.azure.resourcemanager.resources.fluent.ResourcesClient;
import com.azure.resourcemanager.resources.models.ResourceIdentityType;
import com.azure.resourcemanager.resources.models.Sku;
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

    private final ClientLogger logger = new ClientLogger(GenericResourceImpl.class);

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
        if (apiVersion == null) {
            apiVersion = ResourceUtils.defaultApiVersion(
                id(), manager().providers().getByName(ResourceUtils.resourceProviderFromResourceId(id())));
        }
        return apiVersion;
    }

    @Override
    public Plan plan() {
        return innerModel().plan();
    }

    @Override
    public Object properties() {
        return innerModel().properties();
    }

    @Override
    public String kind() {
        return innerModel().kind();
    }

    @Override
    public Sku sku() {
        return innerModel().sku();
    }

    @Override
    public Identity identity() {
        return innerModel().identity();
    }

    @Override
    public String managedBy() {
        return innerModel().managedBy();
    }

    @Override
    protected Mono<GenericResourceInner> getInnerAsync() {
        return this.manager().serviceClient().getResources().getAsync(
                resourceGroupName(),
                resourceProviderNamespace(),
                parentResourcePath(),
                resourceType(),
                this.name(),
                this.apiVersion());
    }

    public GenericResourceImpl withProperties(Object properties) {
        innerModel().withProperties(properties);
        return this;
    }

    @Override
    public GenericResourceImpl withKind(String kind) {
        innerModel().withKind(kind);
        return this;
    }

    @Override
    public GenericResourceImpl withSku(Sku sku) {
        innerModel().withSku(sku);
        return this;
    }

    @Override
    public GenericResourceImpl withIdentity(Identity identity) {
        innerModel().withIdentity(identity);
        return this;
    }

    @Override
    public GenericResourceImpl withoutIdentity() {
        innerModel().withIdentity(new Identity().withType(ResourceIdentityType.NONE));
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
        innerModel().withPlan(
            new Plan()
                .withName(name)
                .withPublisher(publisher)
                .withProduct(product)
                .withPromotionCode(promotionCode));
        return this;
    }

    public GenericResourceImpl withPlan(Plan plan) {
        innerModel().withPlan(plan);
        return this;
    }

    @Override
    public GenericResourceImpl withoutPlan() {
        innerModel().withPlan(null);
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
    public Accepted<GenericResource> beginCreate() {
        String apiVersion = this.getApiVersionAsync().block();
        String name = isInCreateMode() ? this.name() : ResourceUtils.nameFromResourceId(innerModel().id());

        return AcceptedImpl.newAccepted(logger,
            this.manager().serviceClient().getHttpPipeline(),
            this.manager().serviceClient().getDefaultPollInterval(),
            () -> this.manager().serviceClient().getResources()
                .createOrUpdateWithResponseAsync(
                    resourceGroupName(),
                    resourceProviderNamespace,
                    parentResourcePath(),
                    resourceType,
                    name,
                    apiVersion,
                    innerModel()).block(),
            inner -> new GenericResourceImpl(inner.id(), inner, this.manager()),
            GenericResourceInner.class,
            null,
            this::setInner,
            Context.NONE);
    }

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Mono<GenericResource> createResourceAsync() {
        Mono<String> observable = this.getApiVersionAsync();
        final ResourcesClient resourceClient = this.manager().serviceClient().getResources();
        return observable
                .flatMap(api -> {
                    String name = this.name();
                    if (!isInCreateMode()) {
                        name = ResourceUtils.nameFromResourceId(innerModel().id());
                    }
                    return resourceClient.createOrUpdateAsync(
                            resourceGroupName(),
                            resourceProviderNamespace,
                            parentResourcePath(),
                            resourceType,
                            name,
                            api,
                            innerModel())
                            .subscribeOn(ResourceManagerUtils.InternalRuntimeContext.getReactorScheduler())
                            .map(innerToFluentMap(this));
                });
    }

    private Mono<String> getApiVersionAsync() {
        Mono<String> apiVersion;
        if (this.apiVersion != null) {
            apiVersion = Mono.just(this.apiVersion);
        } else {
            apiVersion = this.manager().providers().getByNameAsync(resourceProviderNamespace)
                .flatMap(provider -> {
                    String id;
                    if (!isInCreateMode()) {
                        id = innerModel().id();
                    } else {
                        id = ResourceUtils.constructResourceId(
                            this.manager().subscriptionId(),
                            resourceGroupName(),
                            resourceProviderNamespace(),
                            resourceType(),
                            this.name(),
                            parentResourcePath());
                    }
                    this.apiVersion = ResourceUtils.defaultApiVersion(id, provider);
                    return Mono.just(this.apiVersion);
                });
        }
        return apiVersion;
    }
}
