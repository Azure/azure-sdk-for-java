// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.AppServiceCertificateOrders;
import com.azure.resourcemanager.appservice.AppServicePlans;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrderInner;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrdersInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** The implementation for {@link AppServicePlans}. */
class AppServiceCertificateOrdersImpl
    extends TopLevelModifiableResourcesImpl<
        AppServiceCertificateOrder,
        AppServiceCertificateOrderImpl,
        AppServiceCertificateOrderInner,
        AppServiceCertificateOrdersInner,
        AppServiceManager>
    implements AppServiceCertificateOrders {

    AppServiceCertificateOrdersImpl(AppServiceManager manager) {
        super(manager.inner().appServiceCertificateOrders(), manager);
    }

    @Override
    protected AppServiceCertificateOrderImpl wrapModel(String name) {
        return new AppServiceCertificateOrderImpl(name, new AppServiceCertificateOrderInner(), this.manager());
    }

    @Override
    protected AppServiceCertificateOrderImpl wrapModel(AppServiceCertificateOrderInner inner) {
        if (inner == null) {
            return null;
        }
        return new AppServiceCertificateOrderImpl(inner.name(), inner, this.manager());
    }

    @Override
    public AppServiceCertificateOrderImpl define(String name) {
        return wrapModel(name);
    }
}
