/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.appservice.AppServiceCertificate;
import com.azure.management.appservice.AppServiceCertificates;
import com.azure.management.appservice.models.CertificateInner;
import com.azure.management.appservice.models.CertificatesInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for AppServiceCertificates.
 */
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
    protected Mono<CertificateInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return this.inner().deleteAsync(resourceGroupName, name);

    }

    @Override
    public PagedIterable<AppServiceCertificate> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName)));
    }

    @Override
    public PagedFlux<AppServiceCertificate> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroupName));
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
        return new AppServiceCertificateImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public AppServiceCertificateImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedIterable<AppServiceCertificate> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<AppServiceCertificate> listAsync() {
        return wrapPageAsync(inner().listAsync());
    }
}