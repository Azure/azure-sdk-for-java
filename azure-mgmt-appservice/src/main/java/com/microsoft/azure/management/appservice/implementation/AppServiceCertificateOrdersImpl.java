/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrder;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrders;
import com.microsoft.azure.management.appservice.AppServicePlans;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * The implementation for {@link AppServicePlans}.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
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
