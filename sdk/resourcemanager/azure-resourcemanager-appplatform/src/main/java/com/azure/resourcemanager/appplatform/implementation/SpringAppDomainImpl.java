// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.CustomDomainResourceInner;
import com.azure.resourcemanager.appplatform.models.CustomDomainProperties;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDomain;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

public class SpringAppDomainImpl
    extends ExternalChildResourceImpl<SpringAppDomain, CustomDomainResourceInner, SpringAppImpl, SpringApp>
    implements SpringAppDomain {
    SpringAppDomainImpl(String name, SpringAppImpl parent, CustomDomainResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public Mono<SpringAppDomain> createResourceAsync() {
        return manager().serviceClient().getCustomDomains().createOrUpdateAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name(), properties()
        )
            .map(inner -> {
                setInner(inner);
                return this;
            });
    }

    @Override
    public Mono<SpringAppDomain> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return manager().serviceClient().getBindings().deleteAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        );
    }

    @Override
    protected Mono<CustomDomainResourceInner> getInnerAsync() {
        return manager().serviceClient().getCustomDomains().getAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        );
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public CustomDomainProperties properties() {
        return innerModel().properties();
    }

    private AppPlatformManager manager() {
        return parent().manager();
    }
}
