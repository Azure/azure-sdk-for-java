// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.ServicesClient;
import com.azure.resourcemanager.appplatform.fluent.models.ServiceResourceInner;
import com.azure.resourcemanager.appplatform.models.NameAvailability;
import com.azure.resourcemanager.appplatform.models.NameAvailabilityParameters;
import com.azure.resourcemanager.appplatform.models.ResourceSku;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.SpringServices;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

public class SpringServicesImpl
    extends GroupableResourcesImpl<
        SpringService, SpringServiceImpl, ServiceResourceInner, ServicesClient, AppPlatformManager>
    implements SpringServices {
    private static final String SPRING_TYPE = "Microsoft.AppPlatform/Spring";

    public SpringServicesImpl(AppPlatformManager manager) {
        super(manager.serviceClient().getServices(), manager);
    }

    @Override
    protected Mono<ServiceResourceInner> getInnerAsync(String resourceGroupName, String name) {
        return inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    protected SpringServiceImpl wrapModel(String name) {
        return new SpringServiceImpl(name, new ServiceResourceInner(), manager());
    }

    @Override
    protected SpringServiceImpl wrapModel(ServiceResourceInner inner) {
        return inner == null ? null : new SpringServiceImpl(inner.name(), inner, manager());
    }

    @Override
    public NameAvailability checkNameAvailability(String name, Region region) {
        return checkNameAvailabilityAsync(name, region).block();
    }

    @Override
    public Mono<NameAvailability> checkNameAvailabilityAsync(String name, Region region) {
        return inner().checkNameAvailabilityAsync(
            region.toString(), new NameAvailabilityParameters().withName(name).withType(SPRING_TYPE));
    }

    @Override
    public PagedIterable<ResourceSku> listSkus() {
        return new PagedIterable<>(listSkusAsync());
    }

    @Override
    public PagedFlux<ResourceSku> listSkusAsync() {
        return manager().serviceClient().getSkus().listAsync();
    }

    @Override
    public PagedIterable<SpringService> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedFlux<SpringService> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        return PagedConverter.mapPage(inner().listByResourceGroupAsync(resourceGroupName), this::wrapModel);
    }

    @Override
    public SpringServiceImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedIterable<SpringService> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<SpringService> listAsync() {
        return PagedConverter.mapPage(inner().listAsync(), this::wrapModel);
    }
}
