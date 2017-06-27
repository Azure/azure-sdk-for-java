/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.FlowLogInformationInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Client-side representation of the configuration of flow log, associated with network watcher and an Azure resource.
 */
@Fluent
@Beta
public interface FlowLogSettings extends
        HasParent<NetworkWatcher>,
        HasInner<FlowLogInformationInner>,
        Updatable<FlowLogSettings.Update>,
        Refreshable<FlowLogSettings> {
    /**
     * Get the ID of the resource to configure for flow logging.
     *
     * @return the targetResourceId value
     */
    String targetResourceId();

    /**
     * Get the ID of the storage account which is used to store the flow log.
     *
     * @return the storageId value
     */
    String storageId();

    /**
     * Get the flag to enable/disable flow logging.
     *
     * @return the enabled value
     */
    boolean enabled();

    /**
     * Get the flag if retention is enabled/disabled.
     *
     * @return the enabled value
     */
    boolean isRetentionEnabled();

    /**
     * Get the number of days to retain flow log records.
     *
     * @return number of days
     */
    int retentionDays();

    /**
     * Grouping of flow log information update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the flow log information update allowing to set enable/disable property.
         */
        interface WithEnabled {
            /**
             * Enable flow logging.
             *
             * @return the next stage of the flow log information update
             */
            Update withLoggingEnabled();

            /**
             * Disable flow logging.
             *
             * @return the next stage of the flow log information update
             */
            Update withLoggingDisabled();
        }

        /**
         * The stage of the flow log information update allowing to specify storage account.
         */
        interface WithStorageAccount {
            /**
             * Specifies the storage account to use for storing log
             * @param storageId id of the storage account
             * @return the next stage of the flow log information update
             */
            Update withStorageAccount(String storageId);
        }

        /**
         * The stage of the flow log information update allowing to configure retention policy.
         */
        interface WithRetentionPolicy {
            /**
             * Enable retention policy
             * @return the next stage of the flow log information update
             */
            Update withRetentionPolicyEnabled();

            /**
             * Disable retention policy
             * @return the next stage of the flow log information update
             */
            Update withRetentionPolicyDisabled();

            /**
             * @param days the number of days to store flow log
             * @return the next stage of the flow log information update
             */
            Update withRetentionPolicyDays(Integer days);
        }
    }

    /**
     * The template for a flow log information update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<FlowLogSettings>,
            UpdateStages.WithEnabled,
            UpdateStages.WithStorageAccount,
            UpdateStages.WithRetentionPolicy {
    }
}
