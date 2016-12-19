/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.appservice.AppServicePlans;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrder;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrders;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link AppServicePlans}.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class AppServiceCertificateOrdersImpl
        extends GroupableResourcesImpl<
        AppServiceCertificateOrder,
        AppServiceCertificateOrderImpl,
        AppServiceCertificateOrderInner,
        AppServiceCertificateOrdersInner,
        AppServiceManager>
        implements AppServiceCertificateOrders {

    AppServiceCertificateOrdersImpl(AppServiceCertificateOrdersInner innerCollection, AppServiceManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public AppServiceCertificateOrder getByGroup(String groupName, String name) {
        return wrapModel(innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return innerCollection.deleteCertificateOrderAsync(groupName, name)
                .map(new Func1<Object, Void>() {
                    @Override
                    public Void call(Object o) {
                        return null;
                    }
                });
    }

    @Override
    public PagedList<AppServiceCertificateOrder> listByGroup(String resourceGroupName) {
        return wrapList(innerCollection.listByResourceGroup(resourceGroupName));
    }

    @Override
    protected AppServiceCertificateOrderImpl wrapModel(String name) {
        return new AppServiceCertificateOrderImpl(name, new AppServiceCertificateOrderInner(), innerCollection, myManager);
    }

    @Override
    protected AppServiceCertificateOrderImpl wrapModel(AppServiceCertificateOrderInner inner) {
        if (inner == null) {
            return null;
        }
        return new AppServiceCertificateOrderImpl(inner.name(), inner, innerCollection, myManager);
    }

    @Override
    public AppServiceCertificateOrderImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<AppServiceCertificateOrder> getByGroupAsync(String resourceGroupName, String name) {
        return innerCollection.getAsync(resourceGroupName, name)
                .map(new Func1<AppServiceCertificateOrderInner, AppServiceCertificateOrder>() {
                    @Override
                    public AppServiceCertificateOrder call(AppServiceCertificateOrderInner appServiceCertificateOrderInner) {
                        return wrapModel(appServiceCertificateOrderInner);
                    }
                });
    }
}