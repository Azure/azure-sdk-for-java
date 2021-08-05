// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.CertificateResourceInner;
import com.azure.resourcemanager.appplatform.models.CertificateProperties;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.SpringServiceCertificate;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

public class SpringServiceCertificateImpl
    extends ExternalChildResourceImpl<
        SpringServiceCertificate, CertificateResourceInner, SpringServiceImpl, SpringService>
    implements SpringServiceCertificate {
    SpringServiceCertificateImpl(String name, SpringServiceImpl parent, CertificateResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public Mono<SpringServiceCertificate> createResourceAsync() {
        return manager().serviceClient().getCertificates().createOrUpdateAsync(
            parent().resourceGroupName(), parent().name(), name(), innerModel().properties())
            .map(inner -> {
                setInner(inner);
                return this;
            });
    }

    @Override
    public Mono<SpringServiceCertificate> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return manager().serviceClient().getCertificates()
            .deleteAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    protected Mono<CertificateResourceInner> getInnerAsync() {
        return manager().serviceClient().getCertificates()
            .getAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    public CertificateProperties properties() {
        return innerModel().properties();
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    public AppPlatformManager manager() {
        return parent().manager();
    }
}
