// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.BindingResourceInner;
import com.azure.resourcemanager.appplatform.models.BindingResourceProperties;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppServiceBinding;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

public class SpringAppServiceBindingImpl
    extends ExternalChildResourceImpl<SpringAppServiceBinding, BindingResourceInner, SpringAppImpl, SpringApp>
    implements SpringAppServiceBinding {
    SpringAppServiceBindingImpl(String name, SpringAppImpl parent, BindingResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public Mono<SpringAppServiceBinding> createResourceAsync() {
        return manager().serviceClient().getBindings().createOrUpdateAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name(), properties()
        )
            .map(inner -> {
                setInner(inner);
                return this;
            });
    }

    @Override
    public Mono<SpringAppServiceBinding> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return manager().serviceClient().getBindings().deleteAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        );
    }

    @Override
    protected Mono<BindingResourceInner> getInnerAsync() {
        return manager().serviceClient().getBindings().getAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        );
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public BindingResourceProperties properties() {
        return innerModel().properties();
    }

    private AppPlatformManager manager() {
        return parent().manager();
    }
}
