// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.AppsClient;
import com.azure.resourcemanager.appplatform.fluent.inner.AppResourceInner;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringApps;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import reactor.core.publisher.Mono;

public class SpringAppsImpl
    extends ExternalChildResourcesNonCachedImpl<
        SpringAppImpl, SpringApp, AppResourceInner, SpringServiceImpl, SpringService>
    implements SpringApps {

    SpringAppsImpl(SpringServiceImpl parent) {
        super(parent, parent.taskGroup(), "SpringApp");
    }

    @Override
    public SpringApp getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<SpringApp> getByIdAsync(String id) {
        return getByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public SpringApp getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<SpringApp> getByNameAsync(String name) {
        return inner().getAsync(parent().resourceGroupName(), parent().name(), name)
            .map(this::wrapModel);
    }

    @Override
    public AppPlatformManager manager() {
        return parent().manager();
    }

    @Override
    public SpringServiceImpl parent() {
        return super.getParent();
    }

    @Override
    public SpringAppImpl define(String name) {
        return super.prepareIndependentDefine(wrapModel(name));
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return deleteByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return inner().deleteAsync(parent().resourceGroupName(), parent().name(), name);
    }

    @Override
    public PagedIterable<SpringApp> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<SpringApp> listAsync() {
        return inner().listAsync(parent().resourceGroupName(), parent().name())
            .mapPage(this::wrapModel);
    }

    private SpringAppImpl wrapModel(AppResourceInner inner) {
        return inner == null ? null : new SpringAppImpl(inner.name(), parent(), inner);
    }

    private SpringAppImpl wrapModel(String name) {
        return new SpringAppImpl(name, parent(), new AppResourceInner());
    }

    @Override
    public AppsClient inner() {
        return manager().inner().getApps();
    }
}
