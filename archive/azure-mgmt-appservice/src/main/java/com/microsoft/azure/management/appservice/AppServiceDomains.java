/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.appservice.implementation.DomainsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for domain management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
@Beta
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
    PagedList<DomainLegalAgreement> listAgreements(String topLevelExtension);
}