/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.appservice.AppServiceDomain;
import com.azure.management.appservice.AppServiceDomains;
import com.azure.management.appservice.DomainLegalAgreement;
import com.azure.management.appservice.TopLevelDomainAgreementOption;
import com.azure.management.appservice.models.DomainInner;
import com.azure.management.appservice.models.DomainsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * The implementation for AppServiceDomains.
 */
class AppServiceDomainsImpl
        extends TopLevelModifiableResourcesImpl<
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
    protected AppServiceDomainImpl wrapModel(String name) {
        return new AppServiceDomainImpl(name, new DomainInner(), this.manager());
    }

    @Override
    protected AppServiceDomainImpl wrapModel(DomainInner inner) {
        if (inner == null) {
            return null;
        }
        return new AppServiceDomainImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public AppServiceDomainImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedIterable<DomainLegalAgreement> listAgreements(String topLevelExtension) {
        return this.manager().inner().topLevelDomains().listAgreements(topLevelExtension, new TopLevelDomainAgreementOption())
                .mapPage(DomainLegalAgreementImpl::new);
    }
}
