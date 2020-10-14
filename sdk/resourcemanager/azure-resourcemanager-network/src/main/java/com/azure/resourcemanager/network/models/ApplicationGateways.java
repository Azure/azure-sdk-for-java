// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
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
import reactor.core.publisher.Flux;

import java.util.Collection;

/** Entry point to application gateway management API in Azure. */
@Fluent()
public interface ApplicationGateways
    extends SupportsCreating<ApplicationGateway.DefinitionStages.Blank>,
        SupportsListing<ApplicationGateway>,
        SupportsListingByResourceGroup<ApplicationGateway>,
        SupportsGettingByResourceGroup<ApplicationGateway>,
        SupportsGettingById<ApplicationGateway>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<ApplicationGateway>,
        SupportsBatchDeletion,
        HasManager<NetworkManager> {

    /**
     * Starts the specified application gateways.
     *
     * @param ids application gateway resource ids
     */
    void start(String... ids);

    /**
     * Starts the specified application gateways.
     *
     * @param ids application gateway resource ids
     */
    void start(Collection<String> ids);

    /**
     * Starts the specified application gateways in parallel asynchronously.
     *
     * @param ids application gateway resource id
     * @return an emitter of the resource ID for each successfully started application gateway
     */
    Flux<String> startAsync(String... ids);

    /**
     * Starts the specified application gateways in parallel asynchronously.
     *
     * @param ids application gateway resource id
     * @return an emitter of the resource ID for each successfully started application gateway
     */
    Flux<String> startAsync(Collection<String> ids);

    /**
     * Stops the specified application gateways.
     *
     * @param ids application gateway resource ids
     */
    void stop(String... ids);

    /**
     * Stops the specified application gateways.
     *
     * @param ids application gateway resource ids
     */
    void stop(Collection<String> ids);

    /**
     * Stops the specified application gateways in parallel asynchronously.
     *
     * @param ids application gateway resource ids
     * @return an emitter of the resource ID for each successfully stopped application gateway
     */
    Flux<String> stopAsync(String... ids);

    /**
     * Stops the specified application gateways in parallel asynchronously.
     *
     * @param ids application gateway resource id
     * @return an emitter of the resource ID for each successfully stopped application gateway
     */
    Flux<String> stopAsync(Collection<String> ids);
}
