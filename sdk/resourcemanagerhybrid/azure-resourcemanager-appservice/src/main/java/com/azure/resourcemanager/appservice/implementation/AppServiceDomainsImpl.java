// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.AppServiceDomains;
import com.azure.resourcemanager.appservice.models.DomainLegalAgreement;
import com.azure.resourcemanager.appservice.models.TopLevelDomainAgreementOption;
import com.azure.resourcemanager.appservice.fluent.models.DomainInner;
import com.azure.resourcemanager.appservice.fluent.DomainsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for AppServiceDomains. */
public class AppServiceDomainsImpl
    extends TopLevelModifiableResourcesImpl<
        AppServiceDomain, AppServiceDomainImpl, DomainInner, DomainsClient, AppServiceManager>
    implements AppServiceDomains {

    public AppServiceDomainsImpl(AppServiceManager manager) {
        super(manager.serviceClient().getDomains(), manager);
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
    public PagedIterable<DomainLegalAgreement> listAgreements(String topLevelExtension) {
        return PagedConverter.mapPage(this
            .manager()
            .serviceClient()
            .getTopLevelDomains()
            .listAgreements(topLevelExtension, new TopLevelDomainAgreementOption()),
            DomainLegalAgreementImpl::new);
    }
}
