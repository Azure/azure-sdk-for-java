/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor;

import com.azure.core.annotation.Fluent;
import com.azure.management.monitor.models.ActionGroupsInner;
import com.azure.management.monitor.implementation.MonitorManager;
import com.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * Entry point for Action Group management API.
 */
@Fluent
public interface ActionGroups extends
        SupportsCreating<ActionGroup.DefinitionStages.Blank>,
        SupportsListing<ActionGroup>,
        SupportsListingByResourceGroup<ActionGroup>,
        SupportsGettingById<ActionGroup>,
        SupportsBatchCreation<ActionGroup>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchDeletion,
        HasManager<MonitorManager>,
        HasInner<ActionGroupsInner> {

    /**
     * Enable a receiver in an action group. This changes the receiver's status from Disabled to Enabled. This operation is only supported for Email or SMS receivers.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param receiverName The name of the receiver to resubscribe.
     */
    void enableReceiver(String resourceGroupName, String actionGroupName, String receiverName);

    /**
     * Enable a receiver in an action group. This changes the receiver's status from Disabled to Enabled. This operation is only supported for Email or SMS receivers.
     *
     * @param resourceGroupName The name of the resource group.
     * @param actionGroupName The name of the action group.
     * @param receiverName The name of the receiver to resubscribe.
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> enableReceiverAsync(String resourceGroupName, String actionGroupName, String receiverName);
}
