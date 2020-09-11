// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.DomainsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point for domain management API. */
@Fluent
public interface AppServiceDomains
    extends SupportsCreating<AppServiceDomain.DefinitionStages.Blank>,
        SupportsListing<AppServiceDomain>,
        SupportsListingByResourceGroup<AppServiceDomain>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsGettingByResourceGroup<AppServiceDomain>,
        SupportsGettingById<AppServiceDomain>,
        HasManager<AppServiceManager>,
        HasInner<DomainsClient> {
    /**
     * List the agreements for purchasing a domain with a specific top level extension.
     *
     * @param topLevelExtension the top level extension of the domain, e.g., "com", "net", "org"
     * @return the list of agreements required for the purchase
     */
    PagedIterable<DomainLegalAgreement> listAgreements(String topLevelExtension);
}
