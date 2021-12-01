// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import reactor.core.publisher.Mono;

/** Entry point for Action Group management API. */
@Fluent
public interface ActionGroups
    extends SupportsCreating<ActionGroup.DefinitionStages.Blank>,
        SupportsListing<ActionGroup>,
        SupportsListingByResourceGroup<ActionGroup>,
        SupportsGettingById<ActionGroup>,
        SupportsBatchCreation<ActionGroup>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchDeletion,
        HasManager<MonitorManager> {

    /**
     * Enable a receiver in an action group. This changes the receiver's status from Disabled to Enabled. This operation
     * is only supported for Email or SMS receivers.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param receiverName The name of the receiver to resubscribe.
     */
    void enableReceiver(String resourceGroupName, String actionGroupName, String receiverName);

    /**
     * Enable a receiver in an action group. This changes the receiver's status from Disabled to Enabled. This operation
     * is only supported for Email or SMS receivers.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param receiverName The name of the receiver to resubscribe.
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> enableReceiverAsync(String resourceGroupName, String actionGroupName, String receiverName);
}
