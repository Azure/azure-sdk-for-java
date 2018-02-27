/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewaySkuName;
import com.microsoft.azure.management.network.ApplicationGateways;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.RXMapper;

import rx.Observable;

/**
 *  Implementation for ApplicationGateways.
 */
@LangDefinition
class ApplicationGatewaysImpl
        extends TopLevelModifiableResourcesImpl<
            ApplicationGateway,
            ApplicationGatewayImpl,
            ApplicationGatewayInner,
            ApplicationGatewaysInner,
            NetworkManager>
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
        this.startAsync(applicationGatewayResourceId).toBlocking().last();
    }

    @Override
    public Observable<String> startAsync(String...applicationGatewayResourceId) {
        return this.startAsync(new ArrayList<String>(Arrays.asList(applicationGatewayResourceId)));
    }

    @Override
    public void stop(String... applicationGatewayResourceIds) {
        if (applicationGatewayResourceIds == null) {
            return;
        }
        this.stopAsync(applicationGatewayResourceIds).toBlocking().last();
    }

    @Override
    public void start(Collection<String> applicationGatewayResourceIds) {
        this.startAsync(applicationGatewayResourceIds).toBlocking().last();
    }

    @Override
    public void stop(Collection<String> applicationGatewayResourceIds) {
        this.stopAsync(applicationGatewayResourceIds).toBlocking().last();
    }

    @Override
    public Observable<String> stopAsync(String...applicationGatewayResourceIds) {
        return this.stopAsync(new ArrayList<String>(Arrays.asList(applicationGatewayResourceIds)));
    }

    @Override
    public Observable<String> startAsync(Collection<String> applicationGatewayResourceIds) {
        if (applicationGatewayResourceIds == null) {
            return Observable.empty();
        }

        Collection<Observable<String>> observables = new ArrayList<>();
        for (String id : applicationGatewayResourceIds) {
            final String resourceGroupName = ResourceUtils.groupFromResourceId(id);
            final String name = ResourceUtils.nameFromResourceId(id);
            Observable<String> o = RXMapper.map(this.inner().startAsync(resourceGroupName, name), id);
            observables.add(o);
        }
        return Observable.mergeDelayError(observables);
    }

    @Override
    public Observable<String> stopAsync(Collection<String> applicationGatewayResourceIds) {
        if (applicationGatewayResourceIds == null) {
            return Observable.empty();
        }
        Collection<Observable<String>> observables = new ArrayList<>();
        for (String id : applicationGatewayResourceIds) {
            final String resourceGroupName = ResourceUtils.groupFromResourceId(id);
            final String name = ResourceUtils.nameFromResourceId(id);
            Observable<String> o = RXMapper.map(this.inner().stopAsync(resourceGroupName, name), id);
            observables.add(o);
        }
        return Observable.mergeDelayError(observables);
    }
}
