/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.ApplicationGatewaysInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Flux;

import java.util.Collection;


/**
 * Entry point to application gateway management API in Azure.
 */
@Fluent()
public interface ApplicationGateways extends
        SupportsCreating<ApplicationGateway.DefinitionStages.Blank>,
        SupportsListing<ApplicationGateway>,
        SupportsListingByResourceGroup<ApplicationGateway>,
        SupportsGettingByResourceGroup<ApplicationGateway>,
        SupportsGettingById<ApplicationGateway>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<ApplicationGateway>,
        SupportsBatchDeletion,
        HasManager<NetworkManager>,
        HasInner<ApplicationGatewaysInner> {

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
