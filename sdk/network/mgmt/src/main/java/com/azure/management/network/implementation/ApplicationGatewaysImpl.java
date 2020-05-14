// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network.implementation;

import com.azure.management.network.ApplicationGateway;
import com.azure.management.network.ApplicationGatewaySkuName;
import com.azure.management.network.ApplicationGateways;
import com.azure.management.network.models.ApplicationGatewayInner;
import com.azure.management.network.models.ApplicationGatewaysInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.management.resources.fluentcore.utils.ReactorMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementation for ApplicationGateways. */
class ApplicationGatewaysImpl
    extends TopLevelModifiableResourcesImpl<
        ApplicationGateway, ApplicationGatewayImpl, ApplicationGatewayInner, ApplicationGatewaysInner, NetworkManager>
    implements ApplicationGateways {

    ApplicationGatewaysImpl(final NetworkManager networkManager) {
        super(networkManager.inner().applicationGateways(), networkManager);
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
        return this.startAsync(new ArrayList<String>(Arrays.asList(applicationGatewayResourceId)));
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
        return this.stopAsync(new ArrayList<String>(Arrays.asList(applicationGatewayResourceIds)));
    }

    @Override
    public Flux<String> startAsync(Collection<String> applicationGatewayResourceIds) {
        if (applicationGatewayResourceIds == null) {
            return Flux.empty();
        }

        Collection<Mono<String>> observables = new ArrayList<>();
        for (String id : applicationGatewayResourceIds) {
            final String resourceGroupName = ResourceUtils.groupFromResourceId(id);
            final String name = ResourceUtils.nameFromResourceId(id);
            Mono<String> o = ReactorMapper.map(this.inner().startAsync(resourceGroupName, name), id);
            observables.add(o);
        }
        return Flux.mergeDelayError(32, observables.toArray(new Mono[observables.size()]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Flux<String> stopAsync(Collection<String> applicationGatewayResourceIds) {
        if (applicationGatewayResourceIds == null) {
            return Flux.empty();
        }
        Collection<Mono<String>> observables = new ArrayList<>();
        for (String id : applicationGatewayResourceIds) {
            final String resourceGroupName = ResourceUtils.groupFromResourceId(id);
            final String name = ResourceUtils.nameFromResourceId(id);
            Mono<String> o = ReactorMapper.map(this.inner().stopAsync(resourceGroupName, name), id);
            observables.add(o);
        }
        return Flux.mergeDelayError(32, observables.toArray(new Mono[0]));
    }
}
