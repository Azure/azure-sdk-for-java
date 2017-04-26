/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.Collection;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewaysInner;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

import rx.Observable;


/**
 * Entry point to application gateway management API in Azure.
 */
@Fluent()
@Beta()
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
     * @param ids application gateway resource ids
     */
    void start(String...ids);

    /**
     * Starts the specified application gateways.
     * @param ids application gateway resource ids
     */
    void start(Collection<String> ids);

    /**
     * Starts the specified application gateways in parallel asynchronously.
     * @param ids application gateway resource id
     * @return an Observable emitting the resource ID for each successfully started application gateway
     */
    @Beta
    Observable<String> startAsync(String... ids);

    /**
     * Starts the specified application gateways in parallel asynchronously.
     * @param ids application gateway resource id
     * @return an Observable emitting the resource ID for each successfully started application gateway
     */
    @Beta
    Observable<String> startAsync(Collection<String> ids);

    /**
     * Stops the specified application gateways.
     * @param ids application gateway resource ids
     */
    void stop(String...ids);

    /**
     * Stops the specified application gateways.
     * @param ids application gateway resource ids
     */
    void stop(Collection<String> ids);

    /**
     * Stops the specified application gateways in parallel asynchronously.
     * @param ids application gateway resource ids
     * @return an Observable emitting the resource ID for each successfully stopped application gateway
     */
    @Beta
    Observable<String> stopAsync(String...ids);

    /**
     * Stops the specified application gateways in parallel asynchronously.
     * @param ids application gateway resource id
     * @return an Observable emitting the resource ID for each successfully stopped application gateway
     */
    @Beta
    Observable<String> stopAsync(Collection<String> ids);
}
