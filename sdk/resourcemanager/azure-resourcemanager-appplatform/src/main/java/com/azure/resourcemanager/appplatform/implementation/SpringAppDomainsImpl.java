// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.CustomDomainsClient;
import com.azure.resourcemanager.appplatform.fluent.inner.CustomDomainResourceInner;
import com.azure.resourcemanager.appplatform.models.CustomDomainProperties;
import com.azure.resourcemanager.appplatform.models.CustomDomainValidateResult;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDomain;
import com.azure.resourcemanager.appplatform.models.SpringAppDomains;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import reactor.core.publisher.Mono;

public class SpringAppDomainsImpl
    extends ExternalChildResourcesNonCachedImpl<
        SpringAppDomainImpl, SpringAppDomain, CustomDomainResourceInner, SpringAppImpl, SpringApp>
    implements SpringAppDomains {
    SpringAppDomainsImpl(SpringAppImpl parent) {
        super(parent, parent.taskGroup(), "SpringAppDomain");
    }

    @Override
    public SpringAppDomain getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<SpringAppDomain> getByIdAsync(String id) {
        return getByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public SpringAppDomain getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<SpringAppDomain> getByNameAsync(String name) {
        return inner().getAsync(parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name)
            .map(this::wrapModel);
    }

    SpringAppDomainImpl wrapModel(CustomDomainResourceInner inner) {
        return inner == null ? null : new SpringAppDomainImpl(inner.name(), parent(), inner);
    }

    @Override
    public AppPlatformManager manager() {
        return parent().manager();
    }

    @Override
    public SpringAppImpl parent() {
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
        return inner().deleteAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name);
    }

    @Override
    public PagedIterable<SpringAppDomain> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<SpringAppDomain> listAsync() {
        return inner().listAsync(parent().parent().resourceGroupName(), parent().parent().name(), parent().name())
            .mapPage(this::wrapModel);
    }

    @Override
    public CustomDomainsClient inner() {
        return manager().inner().getCustomDomains();
    }

    @Override
    public CustomDomainValidateResult validate(String domain) {
        return validateAsync(domain).block();
    }

    @Override
    public Mono<CustomDomainValidateResult> validateAsync(String domain) {
        return manager().inner().getApps().validateDomainAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), domain);
    }

    SpringAppDomain prepareCreateOrUpdate(String name, CustomDomainProperties properties) {
        return prepareInlineDefine(
            new SpringAppDomainImpl(name, parent(), new CustomDomainResourceInner().withProperties(properties)));
    }

    void prepareDelete(String name) {
        prepareInlineRemove(new SpringAppDomainImpl(name, parent(), new CustomDomainResourceInner()));
    }
}
