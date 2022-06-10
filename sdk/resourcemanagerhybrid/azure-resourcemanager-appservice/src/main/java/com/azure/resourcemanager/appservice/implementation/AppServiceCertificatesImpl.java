// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServiceCertificate;
import com.azure.resourcemanager.appservice.models.AppServiceCertificates;
import com.azure.resourcemanager.appservice.fluent.models.CertificateInner;
import com.azure.resourcemanager.appservice.fluent.CertificatesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

/** The implementation for AppServiceCertificates. */
public class AppServiceCertificatesImpl
    extends GroupableResourcesImpl<
        AppServiceCertificate, AppServiceCertificateImpl, CertificateInner, CertificatesClient, AppServiceManager>
    implements AppServiceCertificates {

    public AppServiceCertificatesImpl(AppServiceManager manager) {
        super(manager.serviceClient().getCertificates(), manager);
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
        return new PagedIterable<>(this.listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedFlux<AppServiceCertificate> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
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
        return new AppServiceCertificateImpl(inner.name(), inner, this.manager());
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
