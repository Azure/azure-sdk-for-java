/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.appservice.AppServiceDomain;
import com.microsoft.azure.management.appservice.AppServiceDomains;
import com.microsoft.azure.management.appservice.DomainLegalAgreement;
import rx.Completable;

/**
 * The implementation for AppServiceDomains.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class AppServiceDomainsImpl
        extends GroupableResourcesImpl<
        AppServiceDomain,
        AppServiceDomainImpl,
        DomainInner,
        DomainsInner,
        AppServiceManager>
        implements AppServiceDomains {

    AppServiceDomainsImpl(AppServiceManager manager) {
        super(manager.inner().domains(), manager);
    }

    @Override
    public AppServiceDomainImpl getByGroup(String groupName, String name) {
        return wrapModel(this.inner().get(groupName, name));
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    protected AppServiceDomainImpl wrapModel(String name) {
        return new AppServiceDomainImpl(name, new DomainInner(), this.manager());
    }

    @Override
    protected AppServiceDomainImpl wrapModel(DomainInner inner) {
        if (inner == null) {
            return null;
        }
        return new AppServiceDomainImpl(inner.name(), inner, this.manager());
    }

    @Override
    public AppServiceDomainImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedList<AppServiceDomain> list() {
        return wrapList(this.inner().list());
    }

    @Override
    public PagedList<AppServiceDomain> listByGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
    }

    @Override
    public PagedList<DomainLegalAgreement> listAgreements(String topLevelExtension) {
        return new PagedListConverter<TldLegalAgreementInner, DomainLegalAgreement>() {
            @Override
            public DomainLegalAgreement typeConvert(TldLegalAgreementInner tldLegalAgreementInner) {
                return new DomainLegalAgreementImpl(tldLegalAgreementInner);
            }
        }.convert(this.manager().inner().topLevelDomains().listAgreements(topLevelExtension));
    }
}