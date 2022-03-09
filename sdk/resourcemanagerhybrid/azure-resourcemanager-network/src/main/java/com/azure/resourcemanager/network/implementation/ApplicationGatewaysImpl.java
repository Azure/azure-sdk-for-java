// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ApplicationGatewaysClient;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayInner;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewaySkuName;
import com.azure.resourcemanager.network.models.ApplicationGateways;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.exception.AggregatedManagementException;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/** Implementation for ApplicationGateways. */
public class ApplicationGatewaysImpl
    extends TopLevelModifiableResourcesImpl<
        ApplicationGateway, ApplicationGatewayImpl, ApplicationGatewayInner, ApplicationGatewaysClient, NetworkManager>
    implements ApplicationGateways {

    public ApplicationGatewaysImpl(final NetworkManager networkManager) {
        super(networkManager.serviceClient().getApplicationGateways(), networkManager);
    }

    @Override
    public ApplicationGatewayImpl define(String name) {
        return wrapModel(name).withSize(ApplicationGatewaySkuName.STANDARD_SMALL).withInstanceCount(1);
    }

    // Fluent model create helpers

    @Override
    protected ApplicationGatewayImpl wrapModel(String name) {
        ApplicationGatewayInner inner = new ApplicationGatewayInner();
        return new ApplicationGatewayImpl(name, inner, this.manager());
    }

    @Override
    protected ApplicationGatewayImpl wrapModel(ApplicationGatewayInner inner) {
        return (inner == null) ? null : new ApplicationGatewayImpl(inner.name(), inner, this.manager());
    }

    @Override
    public void start(String... applicationGatewayResourceId) {
        if (applicationGatewayResourceId == null) {
            return;
        }
        this.startAsync(applicationGatewayResourceId).blockLast();
    }

    @Override
    public Flux<String> startAsync(String... applicationGatewayResourceId) {
        return this.startAsync(new ArrayList<>(Arrays.asList(applicationGatewayResourceId)));
    }

    @Override
    public void stop(String... applicationGatewayResourceIds) {
        if (applicationGatewayResourceIds == null) {
            return;
        }
        this.stopAsync(applicationGatewayResourceIds).blockLast();
    }

    @Override
    public void start(Collection<String> applicationGatewayResourceIds) {
        this.startAsync(applicationGatewayResourceIds).blockLast();
    }

    @Override
    public void stop(Collection<String> applicationGatewayResourceIds) {
        this.stopAsync(applicationGatewayResourceIds).blockLast();
    }

    @Override
    public Flux<String> stopAsync(String... applicationGatewayResourceIds) {
        return this.stopAsync(new ArrayList<>(Arrays.asList(applicationGatewayResourceIds)));
    }

    @Override
    public Flux<String> startAsync(Collection<String> applicationGatewayResourceIds) {
        if (applicationGatewayResourceIds == null) {
            return Flux.empty();
        } else {
            return Flux
                .fromIterable(applicationGatewayResourceIds)
                .flatMapDelayError(
                    id -> {
                        final String resourceGroupName = ResourceUtils.groupFromResourceId(id);
                        final String name = ResourceUtils.nameFromResourceId(id);
                        return this.inner().startAsync(resourceGroupName, name).then(Mono.just(id));
                    },
                    32,
                    32)
                .onErrorMap(AggregatedManagementException::convertToManagementException)
                .subscribeOn(ResourceManagerUtils.InternalRuntimeContext.getReactorScheduler());
        }
    }

    @Override
    public Flux<String> stopAsync(Collection<String> applicationGatewayResourceIds) {
        if (applicationGatewayResourceIds == null) {
            return Flux.empty();
        } else {
            return Flux
                .fromIterable(applicationGatewayResourceIds)
                .flatMapDelayError(
                    id -> {
                        final String resourceGroupName = ResourceUtils.groupFromResourceId(id);
                        final String name = ResourceUtils.nameFromResourceId(id);
                        return this.inner().stopAsync(resourceGroupName, name).then(Mono.just(id));
                    },
                    32,
                    32)
                .onErrorMap(AggregatedManagementException::convertToManagementException)
                .subscribeOn(ResourceManagerUtils.InternalRuntimeContext.getReactorScheduler());
        }
    }
}
