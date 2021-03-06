/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 *
 */

package com.microsoft.azure.management.network.v2020_06_01.implementation;

import com.microsoft.azure.arm.model.implementation.WrapperImpl;
import com.microsoft.azure.management.network.v2020_06_01.ApplicationGatewayPrivateLinkResources;
import rx.Observable;
import rx.functions.Func1;
import com.microsoft.azure.Page;
import com.microsoft.azure.management.network.v2020_06_01.ApplicationGatewayPrivateLinkResource;

class ApplicationGatewayPrivateLinkResourcesImpl extends WrapperImpl<ApplicationGatewayPrivateLinkResourcesInner> implements ApplicationGatewayPrivateLinkResources {
    private final NetworkManager manager;

    ApplicationGatewayPrivateLinkResourcesImpl(NetworkManager manager) {
        super(manager.inner().applicationGatewayPrivateLinkResources());
        this.manager = manager;
    }

    public NetworkManager manager() {
        return this.manager;
    }

    private ApplicationGatewayPrivateLinkResourceImpl wrapModel(ApplicationGatewayPrivateLinkResourceInner inner) {
        return  new ApplicationGatewayPrivateLinkResourceImpl(inner, manager());
    }

    @Override
    public Observable<ApplicationGatewayPrivateLinkResource> listAsync(final String resourceGroupName, final String applicationGatewayName) {
        ApplicationGatewayPrivateLinkResourcesInner client = this.inner();
        return client.listAsync(resourceGroupName, applicationGatewayName)
        .flatMapIterable(new Func1<Page<ApplicationGatewayPrivateLinkResourceInner>, Iterable<ApplicationGatewayPrivateLinkResourceInner>>() {
            @Override
            public Iterable<ApplicationGatewayPrivateLinkResourceInner> call(Page<ApplicationGatewayPrivateLinkResourceInner> page) {
                return page.items();
            }
        })
        .map(new Func1<ApplicationGatewayPrivateLinkResourceInner, ApplicationGatewayPrivateLinkResource>() {
            @Override
            public ApplicationGatewayPrivateLinkResource call(ApplicationGatewayPrivateLinkResourceInner inner) {
                return wrapModel(inner);
            }
        });
    }

}
