// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.inner.EventhubInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.storage.models.StorageAccount;

import java.util.Set;

/**
 * Type representing an Azure EventHub.
 */
@Fluent
public interface EventHub extends
    NestedResource,
    HasManager<EventHubsManager>,
    Refreshable<EventHub>,
    Updatable<EventHub.Update>,
    HasInner<EventhubInner> {
    /**
     * @return the resource group of the parent namespace
     */
    String namespaceResourceGroupName();

    /**
     * @return name of the parent namespace
     */
    String namespaceName();

    /**
     * @return true if the data capture enabled for the event hub events, false otherwise
     */
    boolean isDataCaptureEnabled();

    /**
     * @return configured window in seconds to be used for event capturing when capturing is enabled
     */
    int dataCaptureWindowSizeInSeconds();

    /**
     * @return configured window in MB to be used for event capturing when capturing is enabled
     */
    int dataCaptureWindowSizeInMB();

    /**
     * @return whether to skip empty archives when capturing is enabled
     */
    boolean dataCaptureSkipEmptyArchives();

    /**
     * @return the format file name that stores captured data when capturing is enabled
     */
    String dataCaptureFileNameFormat();

    /**
     * @return description of the destination where captured data will be stored
     */
    Destination captureDestination();

    /**
     * @return the partition identifiers
     */
    Set<String> partitionIds();

    /**
     * @return retention period of events in days
     */
    int messageRetentionPeriodInDays();

    /**
     * @return consumer group in the event hub
     */
    PagedFlux<EventHubConsumerGroup> listConsumerGroupsAsync();
    /**
     * @return authorization rules enabled for the event hub
     */
    PagedFlux<EventHubAuthorizationRule> listAuthorizationRulesAsync();
    /**
     * @return consumer group in the event hub
     */
    PagedIterable<EventHubConsumerGroup> listConsumerGroups();
    /**
     * @return authorization rules enabled for the event hub
     */
    PagedIterable<EventHubAuthorizationRule> listAuthorizationRules();

    /**
     * The entirety of the event hub definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithNamespace,
            DefinitionStages.WithCaptureProviderOrCreate,
            DefinitionStages.WithCaptureEnabledDisabled,
            DefinitionStages.WithCaptureOptionalSettingsOrCreate,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of event hub definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a event hub definition.
         */
        interface Blank extends WithNamespace {
        }

        /**
         * The stage of the event hub definition allowing to specify the name space in which
         * event hub needs to be created.
         */
        interface WithNamespace {
            /**
             * Specifies the new namespace in which event hub needs to be created.
             *
             * @param namespaceCreatable namespace creatable definition
             * @return next stage of the event hub definition
             */
            WithCaptureProviderOrCreate withNewNamespace(Creatable<EventHubNamespace> namespaceCreatable);

            /**
             * Specifies an existing event hub namespace in which event hub needs to be created.
             *
             * @param namespace event hub namespace
             * @return next stage of the event hub definition
             */
            WithCaptureProviderOrCreate withExistingNamespace(EventHubNamespace namespace);

            /**
             * Specifies an existing event hub namespace in which event hub needs to be created.
             *
             * @param resourceGroupName namespace resource group name
             * @param namespaceName event hub namespace
             * @return next stage of the event hub definition
             */
            WithCaptureProviderOrCreate withExistingNamespace(String resourceGroupName, String namespaceName);

            /**
             * Specifies id of an existing event hub namespace in which event hub needs to be created.
             *
             * @param namespaceId event hub namespace resource id
             * @return next stage of the event hub definition
             */
            WithCaptureProviderOrCreate withExistingNamespaceId(String namespaceId);
        }

        /**
         * The stage of the event hub definition allowing to specify provider to store captured data
         * when data capturing is enabled.
         */
        interface WithCaptureProviderOrCreate extends WithCreate {
            /**
             * Specifies a new storage account to store the captured data when data capturing is enabled.
             *
             * @param storageAccountCreatable creatable storage account definition
             * @param containerName container to store the files containing captured data
             * @return next stage of the event hub definition
             */
            WithCaptureEnabledDisabled withNewStorageAccountForCapturedData(
                Creatable<StorageAccount> storageAccountCreatable, String containerName);

            /**
             * Specifies an existing storage account to store the captured data when data capturing is enabled.
             *
             * @param storageAccount storage account
             * @param containerName an existing or new container to store the files containing captured data
             * @return next stage of the event hub definition
             */
            WithCaptureEnabledDisabled withExistingStorageAccountForCapturedData(
                StorageAccount storageAccount, String containerName);

            /**
             * Specifies an existing storage account to store the captured data when data capturing is enabled.
             *
             * @param storageAccountId storage account arm id
             * @param containerName an existing or new container to store the files containing captured data
             * @return next stage of the event hub definition
             */
            WithCaptureEnabledDisabled withExistingStorageAccountForCapturedData(
                String storageAccountId, String containerName);
        }

        /**
         * The stage of the event hub definition allowing to enable or disable data capturing.
         */
        interface WithCaptureEnabledDisabled {
            /**
             * Specifies that data capture should be enabled for the event hub.
             *
             * @return next stage of the event hub definition
             */
            WithCaptureOptionalSettingsOrCreate withDataCaptureEnabled();

            /**
             * Specifies that data capture should be disabled for the event hub.
             *
             * @return next stage of the event hub definition
             */
            WithCaptureOptionalSettingsOrCreate withDataCaptureDisabled();
        }

        /**
         * The stage of the event hub definition allowing to configure data capturing.
         */
        interface WithCaptureOptionalSettingsOrCreate extends WithCreate {
            /**
             * Specifies the capture window size in seconds.
             *
             * @param sizeInSeconds window size in seconds
             * @return next stage of the event hub definition
             */
            WithCaptureOptionalSettingsOrCreate withDataCaptureWindowSizeInSeconds(int sizeInSeconds);

            /**
             * Set a value that indicates whether to Skip Empty Archives.
             *
             * @param skipEmptyArchives the skipEmptyArchives value to set
             * @return next stage of the event hub definition
             */
            WithCaptureOptionalSettingsOrCreate withDataCaptureSkipEmptyArchives(Boolean skipEmptyArchives);

            /**
             * Specifies the capture window size in MB.
             *
             * @param sizeInMB window size in MB
             * @return next stage of the event hub definition
             */
            WithCaptureOptionalSettingsOrCreate withDataCaptureWindowSizeInMB(int sizeInMB);

            /**
             * Specifies file name format containing captured data.
             *
             * @param format the file name format
             * @return next stage of the event hub definition
             */
            WithCaptureOptionalSettingsOrCreate withDataCaptureFileNameFormat(String format);
        }

        /**
         * The stage of the event hub definition allowing to add authorization rule for accessing
         * the event hub.
         */
        interface WithAuthorizationRule {
            /**
             * Specifies that a new authorization rule should be created that has send access to the event hub.
             *
             * @param ruleName rule name
             * @return next stage of the event hub definition
             */
            WithCreate withNewSendRule(String ruleName);

            /**
             * Specifies that a new authorization rule should be created that has listen access to the event hub.
             *
             * @param ruleName rule name
             * @return next stage of the event hub definition
             */
            WithCreate withNewListenRule(String ruleName);

            /**
             * Specifies that a new authorization rule should be created
             * that has send and listen access to the event hub.
             *
             * @param ruleName rule name
             * @return next stage of the event hub definition
             */
            WithCreate withNewSendAndListenRule(String ruleName);

            /**
             * Specifies that a new authorization rule should be created that has manage access to the event hub.
             *
             * @param ruleName rule name
             * @return next stage of the event hub definition
             */
            WithCreate withNewManageRule(String ruleName);
        }

        /**
         * The stage of the event hub definition allowing to add consumer group for the event hub.
         */
        interface  WithConsumerGroup {
            /**
             * Specifies that a new consumer group should be created for the event hub.
             *
             * @param name consumer group name
             * @return next stage of the event hub definition
             */
            WithCreate withNewConsumerGroup(String name);

            /**
             * Specifies that a new consumer group should be created for the event hub.
             *
             * @param name consumer group name
             * @param metadata consumer group metadata
             * @return next stage of the event hub definition
             */
            WithCreate withNewConsumerGroup(String name, String metadata);
        }

        /**
         * The stage of the event hub definition allowing to specify partition count for event hub.
         */
        interface WithPartitionCount {
            /**
             * Specifies the number of partitions in the event hub.
             *
             * @param count partitions count
             * @return next stage of the event hub definition
             */
            WithCreate withPartitionCount(long count);
        }

        /**
         *  The stage of the event hub definition allowing to specify retention period for event hub events.
         */
        interface WithRetentionPeriod {
            /**
             * Specifies the retention period for events in days.
             *
             * @param period retention period
             * @return next stage of the event hub definition
             */
            WithCreate withRetentionPeriodInDays(long period);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<EventHub>,
                DefinitionStages.WithAuthorizationRule,
                DefinitionStages.WithConsumerGroup,
                DefinitionStages.WithPartitionCount,
                DefinitionStages.WithRetentionPeriod {
        }
    }

    /**
     * Grouping of event hub update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the event hub definition allowing to add an authorization rule for accessing
         * the event hub.
         */
        interface WithAuthorizationRule {
            /**
             * Specifies that a new authorization rule should be created that has send access to the event hub.
             *
             * @param name rule name
             * @return next stage of the event hub update
             */
            Update withNewSendRule(String name);

            /**
             * Specifies that a new authorization rule should be created that has listen access to the event hub.
             *
             * @param name rule name
             * @return next stage of the event hub update
             */
            Update withNewListenRule(String name);

            /**
             * Specifies that a new authorization rule should be created
             * that has send and listen access to the event hub.
             *
             * @param name rule name
             * @return next stage of the event hub update
             */
            Update withNewSendAndListenRule(String name);

            /**
             * Specifies that a new authorization rule should be created that has manage access to the event hub.
             *
             * @param name rule name
             * @return next stage of the event hub update
             */
            Update withNewManageRule(String name);

            /**
             * Specifies that an authorization rule associated with the event hub should be deleted.
             *
             * @param ruleName rule name
             * @return next stage of the event hub update
             */
            Update withoutAuthorizationRule(String ruleName);
        }

        /**
         * The stage of the event hub update allowing to add consumer group for event hub.
         */
        interface  WithConsumerGroup {
            /**
             * Specifies that a new consumer group should be created for the event hub.
             *
             * @param name group name
             * @return next stage of the event hub update
             */
            Update withNewConsumerGroup(String name);

            /**
             * Specifies that a new consumer group should be created for the event hub.
             *
             * @param name group name
             * @param metadata group metadata
             * @return next stage of the event hub update
             */
            Update withNewConsumerGroup(String name, String metadata);

            /**
             * Specifies that a consumer group associated with the event hub should be deleted.
             *
             * @param name group name
             * @return next stage of the event hub update
             */
            Update withoutConsumerGroup(String name);
        }

        /**
         * The stage of the event hub update allowing to configure data capture.
         */
        interface WithCapture {
            /**
             * Specifies a new storage account to store the captured data when data capturing is enabled.
             *
             * @param storageAccountCreatable creatable storage account definition
             * @param containerName container to store the files containing captured data
             * @return next stage of the event hub update
             */
            Update withNewStorageAccountForCapturedData(
                Creatable<StorageAccount> storageAccountCreatable, String containerName);

            /**
             * Specifies an existing storage account to store the captured data when data capturing is enabled.
             *
             * @param storageAccount storage account
             * @param containerName an existing or new container to store the files containing captured data
             * @return next stage of the event hub update
             */
            Update withExistingStorageAccountForCapturedData(StorageAccount storageAccount, String containerName);

            /**
             * Specifies an existing storage account to store the captured data when data capturing is enabled.
             *
             * @param storageAccountId storage account arm id
             * @param containerName an existing or new container to store the files containing captured data
             * @return next stage of the event hub update
             */
            Update withExistingStorageAccountForCapturedData(String storageAccountId, String containerName);

            /**
             * Specifies that data capture should be enabled for the event hub.
             *
             * @return next stage of the event hub update
             */
            Update withDataCaptureEnabled();

            /**
             * Specifies that data capture should be disabled for the event hub.
             *
             * @return next stage of the event hub update
             */
            Update withDataCaptureDisabled();

            /**
             * Specifies the capture window size in seconds.
             *
             * @param sizeInSeconds window size in seconds
             * @return next stage of the event hub update
             */
            Update withDataCaptureWindowSizeInSeconds(int sizeInSeconds);

           /**
             * Specified the capture whether to Skip Empty Archives.
             *
             * @param skipEmptyArchives the skipEmptyArchives value to set
             * @return next stage of the event hub update
             */
            Update withDataCaptureSkipEmptyArchives(Boolean skipEmptyArchives);

            /**
             * Specifies the capture window size in MB.
             *
             * @param sizeInMB window size in MB
             * @return next stage of the event hub update
             */
            Update withDataCaptureWindowSizeInMB(int sizeInMB);
            /**
             * Specifies the format of the file containing captured data.
             *
             * @param format the file name format
             * @return next stage of the event hub update
             */
            Update withDataCaptureFileNameFormat(String format);
        }

        /**
         * The stage of the event hub update allowing to specify partition count for event hub.
         */
        interface WithPartitionCount {
            /**
             * Specifies the number of partitions in the event hub.
             *
             * @param count partitions count
             * @return next stage of the event hub update
             */
            Update withPartitionCount(long count);
        }

        /**
         *  The stage of the event hub definition allowing to specify retention period for event hub events.
         */
        interface WithRetentionPeriod {
            /**
             * Specifies the retention period for events in days.
             *
             * @param period retention period
             * @return next stage of the event hub update
             */
            Update withRetentionPeriodInDays(long period);
        }
    }

    /**
     * The template for a event hub update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<EventHub>,
        UpdateStages.WithConsumerGroup,
        UpdateStages.WithAuthorizationRule,
        UpdateStages.WithCapture,
        UpdateStages.WithPartitionCount,
        UpdateStages.WithRetentionPeriod {
    }
}
