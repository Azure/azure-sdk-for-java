// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.StorageResourceInner;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.SpringStorage;
import com.azure.resourcemanager.appplatform.models.SpringStorages;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

public class SpringStoragesImpl
    extends ExternalChildResourcesNonCachedImpl
    <SpringStorageImpl, SpringStorage, StorageResourceInner, SpringServiceImpl, SpringService>
    implements SpringStorages<SpringStorageImpl> {
    protected SpringStoragesImpl(SpringServiceImpl parent) {
        super(parent, parent.taskGroup(), "SpringStorage");
    }

    @Override
    public SpringStorage getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<SpringStorage> getByNameAsync(String name) {
        return manager().serviceClient().getStorages().getAsync(getParent().resourceGroupName(), getParent().name(), name)
            .map(this::wrapModel);
    }

    @Override
    public AppPlatformManager manager() {
        return getParent().manager();
    }

    @Override
    public SpringStorageImpl define(String name) {
        return prepareIndependentDefine(wrapModel(name));
    }

    @Override
    public PagedIterable<SpringStorage> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<SpringStorage> listAsync() {
        return PagedConverter.mapPage(manager().serviceClient().getStorages().listAsync(getParent().resourceGroupName(), getParent().name()), this::wrapModel);
    }

    private SpringStorageImpl wrapModel(StorageResourceInner inner) {
        return inner == null ? null : new SpringStorageImpl(inner.name(), getParent(), inner);
    }

    private SpringStorageImpl wrapModel(String name) {
        return new SpringStorageImpl(name, getParent(), new StorageResourceInner());
    }
}
