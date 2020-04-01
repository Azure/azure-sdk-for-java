/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.appservice.implementation.AppServiceManager;
import com.azure.management.appservice.models.DomainsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for domain management API.
 */
@Fluent
public interface AppServiceDomains extends
        SupportsCreating<AppServiceDomain.DefinitionStages.Blank>,
        SupportsListing<AppServiceDomain>,
        SupportsListingByResourceGroup<AppServiceDomain>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsGettingByResourceGroup<AppServiceDomain>,
        SupportsGettingById<AppServiceDomain>,
        HasManager<AppServiceManager>,
        HasInner<DomainsInner> {
    /**
     * List the agreements for purchasing a domain with a specific top level extension.
     *
     * @param topLevelExtension the top level extension of the domain, e.g., "com", "net", "org"
     * @return the list of agreements required for the purchase
     */
    PagedIterable<DomainLegalAgreement> listAgreements(String topLevelExtension);
}