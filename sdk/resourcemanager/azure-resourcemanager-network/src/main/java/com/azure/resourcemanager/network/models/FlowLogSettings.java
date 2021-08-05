// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.FlowLogInformationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/**
 * Client-side representation of the configuration of flow log, associated with network watcher and an Azure resource.
 */
@Fluent
public interface FlowLogSettings
    extends HasParent<NetworkWatcher>,
        HasInnerModel<FlowLogInformationInner>,
        Updatable<FlowLogSettings.Update>,
        Refreshable<FlowLogSettings> {
    /**
     * Get the ID of the resource to configure for flow logging.
     *
     * @return the targetResourceId value
     */
    String targetResourceId();

    /** @return the id of the storage account used to store the flow log */
    String storageId();

    /** @return true if logging is enabled, false otherwise */
    boolean enabled();

    /** @return true if retention policy enabled, false otherwise */
    boolean isRetentionEnabled();

    /** @return the number of days to retain flow log records */
    int retentionDays();

    /** @return network security group id these flow log settings apply to */
    String networkSecurityGroupId();

    /** Grouping of flow log information update stages. */
    interface UpdateStages {
        /** The stage of the flow log information update allowing to set enable/disable property. */
        interface WithEnabled {
            /**
             * Enable flow logging.
             *
             * @return the next stage of the flow log information update
             */
            Update withLogging();

            /**
             * Disable flow logging.
             *
             * @return the next stage of the flow log information update
             */
            Update withoutLogging();
        }

        /** The stage of the flow log information update allowing to specify storage account. */
        interface WithStorageAccount {
            /**
             * Specifies the storage account to use for storing log.
             *
             * @param storageId id of the storage account
             * @return the next stage of the flow log information update
             */
            Update withStorageAccount(String storageId);
        }

        /** The stage of the flow log information update allowing to configure retention policy. */
        interface WithRetentionPolicy {
            /**
             * Enable retention policy.
             *
             * @return the next stage of the flow log information update
             */
            Update withRetentionPolicyEnabled();

            /**
             * Disable retention policy.
             *
             * @return the next stage of the flow log information update
             */
            Update withRetentionPolicyDisabled();

            /**
             * Set the number of days to store flow log.
             *
             * @param days the number of days
             * @return the next stage of the flow log information update
             */
            Update withRetentionPolicyDays(int days);
        }
    }

    /**
     * The template for a flow log information update operation, containing all the settings that can be modified.
     *
     * <p>Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update
        extends Appliable<FlowLogSettings>,
            UpdateStages.WithEnabled,
            UpdateStages.WithStorageAccount,
            UpdateStages.WithRetentionPolicy {
    }
}
