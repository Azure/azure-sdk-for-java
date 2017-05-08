/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.NetworkWatcherInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Entry point for Network Watcher API in Azure.
 */
@Fluent
public interface NetworkWatcher extends
        GroupableResource<NetworkManager, NetworkWatcherInner>,
        Refreshable<NetworkWatcher>,
        Updatable<NetworkWatcher.Update> {

    /**
     * Container interface for all the definitions.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of network watcher definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a virtual network definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the virtual network definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithCreate> {
        }

        /**
         * A network watcher with sufficient inputs to create a new network watcher in the cloud,
         * but exposing additional optional inputs to specify.
         */
        interface WithCreate extends
                Creatable<NetworkWatcher>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * The template for a domain update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<NetworkWatcher>,
            Resource.UpdateWithTags<Update> {
    }

}
