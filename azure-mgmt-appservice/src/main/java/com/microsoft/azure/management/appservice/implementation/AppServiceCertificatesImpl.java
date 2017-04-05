/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.appservice.AppServiceCertificate;
import com.microsoft.azure.management.appservice.AppServiceCertificates;
import rx.Completable;
import rx.Observable;

/**
 * The implementation for AppServiceCertificates.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class AppServiceCertificatesImpl
        extends GroupableResourcesImpl<
            AppServiceCertificate,
            AppServiceCertificateImpl,
            CertificateInner,
            CertificatesInner,
            AppServiceManager>
        implements AppServiceCertificates {

    AppServiceCertificatesImpl(AppServiceManager manager) {
        super(manager.inner().certificates(), manager);
    }

    @Override
    protected Observable<CertificateInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getAsync(resourceGroupName, name);
    }

    @Override
    protected Completable deleteInnerAsync(String resourceGroupName, String name) {
        return this.inner().deleteAsync(resourceGroupName, name).toCompletable();

    }

    @Override
    public PagedList<AppServiceCertificate> listByResourceGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
    }

    @Override
    public Observable<AppServiceCertificate> listByResourceGroupAsync(String resourceGroupName) {
        return null;
    }

    @Override
    protected AppServiceCertificateImpl wrapModel(String name) {
        return new AppServiceCertificateImpl(name, new CertificateInner(), this.manager());
    }

    @Override
    protected AppServiceCertificateImpl wrapModel(CertificateInner inner) {
        if (inner == null) {
            return null;
        }
        return new AppServiceCertificateImpl(inner.name(), inner, this.manager());
    }

    @Override
    public AppServiceCertificateImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedList<AppServiceCertificate> list() {
        return wrapList(inner().list());
    }

    @Override
    public Observable<AppServiceCertificate> listAsync() {
        return wrapPageAsync(inner().listAsync());
    }
}