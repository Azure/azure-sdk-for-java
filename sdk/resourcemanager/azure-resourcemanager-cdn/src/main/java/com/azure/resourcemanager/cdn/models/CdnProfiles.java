// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.cdn.CdnManager;
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
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Entry point for CDN profile management API.
 */
@Fluent
public interface CdnProfiles extends
    SupportsCreating<CdnProfile.DefinitionStages.Blank>,
    SupportsListing<CdnProfile>,
    SupportsListingByResourceGroup<CdnProfile>,
    SupportsGettingByResourceGroup<CdnProfile>,
    SupportsGettingById<CdnProfile>,
    SupportsDeletingById,
    SupportsDeletingByResourceGroup,
    SupportsBatchCreation<CdnProfile>,
    SupportsBatchDeletion,
    HasManager<CdnManager> {

    /**
     * Generates a dynamic SSO URI used to sign in to the CDN supplemental portal.
     * Supplemental portal is used to configure advanced feature capabilities that are not
     * yet available in the Azure portal, such as core reports in a standard profile;
     * rules engine, advanced HTTP reports, and real-time stats and alerts in a premium profile.
     * The SSO URI changes approximately every 10 minutes.
     *
     * @param resourceGroupName name of the resource group within the Azure subscription.
     * @param profileName name of the CDN profile which is unique within the resource group.
     * @return the Sso Uri string if successful.
     */
    String generateSsoUri(String resourceGroupName, String profileName);

    /**
     * Checks the availability of a endpoint name without creating the CDN endpoint.
     *
     * @param name The endpoint resource name to validate.
     * @return the CheckNameAvailabilityResult object if successful.
     */
    CheckNameAvailabilityResult checkEndpointNameAvailability(String name);

    /**
     * Checks the availability of a endpoint name without creating the CDN endpoint asynchronously.
     *
     * @param name the endpoint resource name to validate.
     * @return a representation of the deferred computation of this call
     */
    Mono<CheckNameAvailabilityResult> checkEndpointNameAvailabilityAsync(String name);

    /**
     * Lists all of the available CDN REST API operations.
     *
     * @return list of available CDN REST operations.
     */
    PagedIterable<Operation> listOperations();

    /**
     * Check the quota and actual usage of the CDN profiles under the current subscription.
     *
     * @return quotas and actual usages of the CDN profiles under the current subscription.
     */
    PagedIterable<ResourceUsage> listResourceUsage();

    /**
     * Lists all the edge nodes of a CDN service.
     *
     * @return list of all the edge nodes of a CDN service.
     */
    PagedIterable<EdgeNode> listEdgeNodes();

    /**
     * Starts an existing stopped CDN endpoint.
     *
     * @param resourceGroupName name of the resource group within the Azure subscription.
     * @param profileName name of the CDN profile which is unique within the resource group.
     * @param endpointName name of the endpoint under the profile which is unique globally.
     */
    void startEndpoint(String resourceGroupName, String profileName, String endpointName);

    /**
     * Stops an existing running CDN endpoint.
     *
     * @param resourceGroupName name of the resource group within the Azure subscription.
     * @param profileName name of the CDN profile which is unique within the resource group.
     * @param endpointName name of the endpoint under the profile which is unique globally.
     */
    void stopEndpoint(String resourceGroupName, String profileName, String endpointName);

    /**
     * Forcibly purges CDN endpoint content.
     *
     * @param resourceGroupName name of the resource group within the Azure subscription.
     * @param profileName name of the CDN profile which is unique within the resource group.
     * @param endpointName name of the endpoint under the profile which is unique globally.
     * @param contentPaths the path to the content to be purged. Can describe a file path or a wild card directory.
     */
    void purgeEndpointContent(
        String resourceGroupName, String profileName, String endpointName, List<String> contentPaths);

    /**
     * Forcibly pre-loads CDN endpoint content. Available for Verizon profiles.
     *
     * @param resourceGroupName name of the resource group within the Azure subscription.
     * @param profileName name of the CDN profile which is unique within the resource group.
     * @param endpointName name of the endpoint under the profile which is unique globally.
     * @param contentPaths the path to the content to be loaded. Should describe a file path.
     */
    void loadEndpointContent(
        String resourceGroupName, String profileName, String endpointName, List<String> contentPaths);
}
