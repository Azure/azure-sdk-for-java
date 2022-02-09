// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.CertificatesClient;
import com.azure.resourcemanager.appplatform.fluent.models.CertificateResourceInner;
import com.azure.resourcemanager.appplatform.models.CertificateProperties;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.SpringServiceCertificate;
import com.azure.resourcemanager.appplatform.models.SpringServiceCertificates;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

public class SpringServiceCertificatesImpl
    extends ExternalChildResourcesNonCachedImpl<
    SpringServiceCertificateImpl, SpringServiceCertificate, CertificateResourceInner, SpringServiceImpl, SpringService>
    implements SpringServiceCertificates {
    SpringServiceCertificatesImpl(SpringServiceImpl parent) {
        super(parent, parent.taskGroup(), "SpringServiceCertificate");
    }

    @Override
    public SpringServiceCertificate getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<SpringServiceCertificate> getByIdAsync(String id) {
        return getByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public SpringServiceCertificate getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<SpringServiceCertificate> getByNameAsync(String name) {
        return inner().getAsync(parent().resourceGroupName(), parent().name(), name)
            .map(this::wrapModel);
    }

    SpringServiceCertificateImpl wrapModel(CertificateResourceInner inner) {
        return inner == null ? null : new SpringServiceCertificateImpl(inner.name(), parent(), inner);
    }

    @Override
    public AppPlatformManager manager() {
        return parent().manager();
    }

    @Override
    public SpringServiceImpl parent() {
        return getParent();
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return deleteByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return inner().deleteAsync(parent().resourceGroupName(), parent().name(), name);
    }

    @Override
    public PagedIterable<SpringServiceCertificate> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<SpringServiceCertificate> listAsync() {
        return PagedConverter.mapPage(inner().listAsync(parent().resourceGroupName(), parent().name()), this::wrapModel);
    }

    public CertificatesClient inner() {
        return manager().serviceClient().getCertificates();
    }

    SpringServiceCertificate prepareCreateOrUpdate(String name, CertificateProperties properties) {
        return prepareInlineDefine(
            new SpringServiceCertificateImpl(
                name, parent(), new CertificateResourceInner().withProperties(properties)));
    }

    void prepareDelete(String name) {
        prepareInlineRemove(new SpringServiceCertificateImpl(name, parent(), new CertificateResourceInner()));
    }
}
