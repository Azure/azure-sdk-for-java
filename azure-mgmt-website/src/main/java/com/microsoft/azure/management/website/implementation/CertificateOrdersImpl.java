/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.website.AppServicePlans;
import com.microsoft.azure.management.website.CertificateOrder;
import com.microsoft.azure.management.website.CertificateOrders;

import java.io.IOException;

/**
 * The implementation for {@link AppServicePlans}.
 */
class CertificateOrdersImpl
    extends GroupableResourcesImpl<
        CertificateOrder,
        CertificateOrderImpl,
            CertificateOrderInner,
            CertificateOrdersInner,
        AppServiceManager>
    implements CertificateOrders {

    CertificateOrdersImpl(CertificateOrdersInner innerCollection, AppServiceManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public CertificateOrder getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(innerCollection.getCertificateOrder(groupName, name).getBody());
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        innerCollection.deleteCertificateOrder(groupName, name);
    }

    @Override
    public PagedList<CertificateOrder> listByGroup(String resourceGroupName) throws CloudException, IOException {
        return wrapList(innerCollection.getCertificateOrders(resourceGroupName).getBody().value());
    }

    @Override
    protected CertificateOrderImpl wrapModel(String name) {
        return new CertificateOrderImpl(name, new CertificateOrderInner(), innerCollection, myManager);
    }

    @Override
    protected CertificateOrderImpl wrapModel(CertificateOrderInner inner) {
        return new CertificateOrderImpl(inner.name(), inner, innerCollection, myManager);
    }

    @Override
    public CertificateOrderImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public void delete(String id) throws Exception {
        innerCollection.deleteCertificateOrder(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }
}
