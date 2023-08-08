// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.ServiceRegistryResourceInner;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.SpringServiceRegistry;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class SpringServiceRegistryImpl
    extends ExternalChildResourceImpl<SpringServiceRegistry, ServiceRegistryResourceInner, SpringServiceImpl, SpringService>
    implements SpringServiceRegistry {

    protected SpringServiceRegistryImpl(String name, SpringServiceImpl parent, ServiceRegistryResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public Double cpu() {
        return Utils.fromCpuString(innerModel().properties().resourceRequests().cpu());
    }

    @Override
    public Double memory() {
        return Utils.fromMemoryString(innerModel().properties().resourceRequests().memory());
    }

    @Override
    public List<SpringApp> getAppBindings() {
        return parent().apps().list()
            .stream()
            .filter(SpringApp::hasServiceRegistryBinding)
            .collect(Collectors.toList());
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public Mono<SpringServiceRegistry> createResourceAsync() {
        return manager().serviceClient().getServiceRegistries()
            .createOrUpdateAsync(parent().resourceGroupName(), parent().name(), name())
            .map(inner -> {
                setInner(inner);
                return this;
            });
    }

    @Override
    public Mono<SpringServiceRegistry> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return manager().serviceClient().getServiceRegistries().deleteAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    protected Mono<ServiceRegistryResourceInner> getInnerAsync() {
        return manager().serviceClient().getServiceRegistries().getAsync(parent().resourceGroupName(), parent().name(), name());
    }

    private AppPlatformManager manager() {
        return parent().manager();
    }
}
