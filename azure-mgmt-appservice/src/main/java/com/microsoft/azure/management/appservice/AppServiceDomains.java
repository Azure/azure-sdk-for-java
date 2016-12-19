/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point for domain management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public interface AppServiceDomains extends
        SupportsCreating<AppServiceDomain.DefinitionStages.Blank>,
        SupportsListing<AppServiceDomain>,
        SupportsListingByGroup<AppServiceDomain>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsGettingByGroup<AppServiceDomain>,
        SupportsGettingById<AppServiceDomain> {
    /**
     * List the agreements for purchasing a domain with a specific top level extension.
     *
     * @param topLevelExtension the top level extension of the domain, e.g., "com", "net", "org"
     * @return the list of agreements required for the purchase
     */
    PagedList<DomainLegalAgreement> listAgreements(String topLevelExtension);
}