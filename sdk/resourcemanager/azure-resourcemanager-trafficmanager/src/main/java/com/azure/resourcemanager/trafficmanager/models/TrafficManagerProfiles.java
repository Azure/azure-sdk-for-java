// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.models;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.trafficmanager.TrafficManager;
import reactor.core.publisher.Mono;

/** Entry point to traffic manager profile management API in Azure. */
public interface TrafficManagerProfiles
    extends SupportsCreating<TrafficManagerProfile.DefinitionStages.Blank>,
        SupportsListing<TrafficManagerProfile>,
        SupportsListingByResourceGroup<TrafficManagerProfile>,
        SupportsGettingByResourceGroup<TrafficManagerProfile>,
        SupportsGettingById<TrafficManagerProfile>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<TrafficManagerProfile>,
        SupportsBatchDeletion,
        HasManager<TrafficManager> {

    /**
     * Checks that the DNS name is valid for traffic manager profile and is not in use.
     *
     * @param dnsNameLabel the DNS name to check
     * @return whether the DNS is available to be used for a traffic manager profile and other info if not
     */
    CheckProfileDnsNameAvailabilityResult checkDnsNameAvailability(String dnsNameLabel);

    /**
     * Asynchronously checks that the DNS name is valid for traffic manager profile and is not in use.
     *
     * @param dnsNameLabel the DNS name to check
     * @return a representation of the deferred computation of this call, returning whether the DNS is available to be
     *     used for a traffic manager profile and other info if not
     */
    Mono<CheckProfileDnsNameAvailabilityResult> checkDnsNameAvailabilityAsync(String dnsNameLabel);

    /** @return the default geographic hierarchy used by the Geographic traffic routing method. */
    GeographicLocation getGeographicHierarchyRoot();
}
