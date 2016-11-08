/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.PagedList;
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
public interface Domains extends
        SupportsCreating<Domain.DefinitionStages.Blank>,
        SupportsListing<Domain>,
        SupportsListingByGroup<Domain>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsGettingByGroup<Domain>,
        SupportsGettingById<Domain>{
    /**
     * List the agreements for purchasing a domain with a specific top level extension.
     *
     * @param topLevelExtension the top level extension of the domain, e.g., "com", "net", "org"
     * @return the list of agreements required for the purchase
     */
    PagedList<DomainLegalAgreement> listAgreements(String topLevelExtension);
}