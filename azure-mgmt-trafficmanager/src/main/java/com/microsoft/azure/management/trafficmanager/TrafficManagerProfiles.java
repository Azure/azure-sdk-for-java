/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to traffic manager profile management API in Azure.
 */
@Fluent
public interface TrafficManagerProfiles extends
        SupportsCreating<TrafficManagerProfile.DefinitionStages.Blank>,
        SupportsListing<TrafficManagerProfile>,
        SupportsListingByGroup<TrafficManagerProfile>,
        SupportsGettingByGroup<TrafficManagerProfile>,
        SupportsGettingById<TrafficManagerProfile>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<TrafficManagerProfile> {

    /**
     * Checks that the DNS name is valid for traffic manager profile and is not in use.
     *
     * @param dnsNameLabel the DNS name to check
     * @return whether the DNS is available to be used for a traffic manager profile and other info if not
     */
    CheckProfileDnsNameAvailabilityResult checkDnsNameAvailability(String dnsNameLabel);
}