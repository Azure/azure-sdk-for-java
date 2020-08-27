// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.BindingsClient;
import com.azure.resourcemanager.appplatform.fluent.inner.BindingResourceInner;
import com.azure.resourcemanager.appplatform.models.BindingResourceProperties;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppServiceBinding;
import com.azure.resourcemanager.appplatform.models.SpringAppServiceBindings;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import reactor.core.publisher.Mono;

public class SpringAppServiceBindingsImpl
    extends ExternalChildResourcesNonCachedImpl<
        SpringAppServiceBindingImpl, SpringAppServiceBinding, BindingResourceInner, SpringAppImpl, SpringApp>
    implements SpringAppServiceBindings {
    SpringAppServiceBindingsImpl(SpringAppImpl parent) {
        super(parent, parent.taskGroup(), "SpringAppServiceBinding");
    }

    @Override
    public SpringAppServiceBinding getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<SpringAppServiceBinding> getByIdAsync(String id) {
        return getByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public SpringAppServiceBinding getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<SpringAppServiceBinding> getByNameAsync(String name) {
        return inner().getAsync(parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name)
            .map(this::wrapModel);
    }

    SpringAppServiceBindingImpl wrapModel(BindingResourceInner inner) {
        return inner == null ? null : new SpringAppServiceBindingImpl(inner.name(), parent(), inner);
    }

    @Override
    public AppPlatformManager manager() {
        return parent().manager();
    }

    @Override
    public SpringAppImpl parent() {
        return getParent();
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
        return inner().deleteAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name);
    }

    @Override
    public PagedIterable<SpringAppServiceBinding> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<SpringAppServiceBinding> listAsync() {
        return inner().listAsync(parent().parent().resourceGroupName(), parent().parent().name(), parent().name())
            .mapPage(this::wrapModel);
    }

    @Override
    public BindingsClient inner() {
        return manager().inner().getBindings();
    }

    SpringAppServiceBinding prepareCreateOrUpdate(String name, BindingResourceProperties properties) {
        return prepareInlineDefine(
            new SpringAppServiceBindingImpl(name, parent(), new BindingResourceInner().withProperties(properties)));
    }

    void prepareDelete(String name) {
        prepareInlineRemove(new SpringAppServiceBindingImpl(name, parent(), new BindingResourceInner()));
    }
}
