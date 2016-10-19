/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.website.AppServicePlans;
import com.microsoft.azure.management.website.Certificate;
import com.microsoft.azure.management.website.Certificates;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link AppServicePlans}.
 */
class CertificatesImpl
        extends GroupableResourcesImpl<
        Certificate,
        CertificateImpl,
        CertificateInner,
        CertificatesInner,
        AppServiceManager>
        implements Certificates {

    CertificatesImpl(CertificatesInner innerCollection, AppServiceManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public Certificate getByGroup(String groupName, String name) {
        return wrapModel(innerCollection.get(groupName, name));
    }

    @Override
    public PagedList<Certificate> listByGroup(String resourceGroupName) {
        return wrapList(innerCollection.listByResourceGroup(resourceGroupName));
    }

    @Override
    protected CertificateImpl wrapModel(String name) {
        return new CertificateImpl(name, new CertificateInner(), innerCollection, myManager);
    }

    @Override
    protected CertificateImpl wrapModel(CertificateInner inner) {
        return new CertificateImpl(inner.name(), inner, innerCollection, myManager);
    }

    @Override
    public CertificateImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteAsync(String groupName, String name) {
        return innerCollection.deleteAsync(groupName, name)
                .map(new Func1<Object, Void>() {
                    @Override
                    public Void call(Object o) {
                        return null;
                    }
                });
    }
}