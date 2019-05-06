// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

public class PerTestSettings {
    // In-out properties: may be set before test setup and then changed by setup.
    final EPHConstructorArgs inoutEPHConstructorArgs;

    // Output properties: any value set before test setup is ignored. The real value is
    // established during test setup.
    RealEventHubUtilities outUtils;
    String outTelltale;
    ArrayList<String> outPartitionIds;
    PrefabGeneralErrorHandler outGeneralErrorHandler;
    PrefabProcessorFactory outProcessorFactory;
    EventProcessorHost outHost;

    // Properties which are inputs to test setup. Constructor sets up defaults, except for hostName.
    private String inDefaultHostName;
    EventProcessorOptions inOptions; // can be null
    PrefabEventProcessor.CheckpointChoices inDoCheckpoint;
    boolean inEventHubDoesNotExist; // Prevents test code from doing certain checks that would fail on nonexistence before reaching product code.
    boolean inSkipIfNoEventHubConnectionString; // Requires valid connection string even though event hub may not exist.
    boolean inTelltaleOnTimeout; // Generates an empty telltale string, which causes PrefabEventProcessor to trigger telltale on timeout.
    boolean inHasSenders;

    PerTestSettings(String defaultHostName) {
        this.inDefaultHostName = defaultHostName;
        this.inOptions = EventProcessorOptions.getDefaultOptions();
        this.inDoCheckpoint = PrefabEventProcessor.CheckpointChoices.CKP_NONE;
        this.inEventHubDoesNotExist = false;
        this.inSkipIfNoEventHubConnectionString = false;
        this.inTelltaleOnTimeout = false;
        this.inHasSenders = true;

        this.inoutEPHConstructorArgs = new EPHConstructorArgs();
    }

    String getDefaultHostName() {
        return this.inDefaultHostName;
    }

    class EPHConstructorArgs {
        static final int HOST_OVERRIDE = 0x0001;
        static final int EH_PATH_OVERRIDE = 0x0002;
        static final int EH_PATH_REPLACE_IN_CONNECTION = 0x0004;
        static final int EH_PATH_OVERRIDE_AND_REPLACE = EH_PATH_OVERRIDE | EH_PATH_REPLACE_IN_CONNECTION;
        static final int CONSUMER_GROUP_OVERRIDE = 0x0008;
        static final int EH_CONNECTION_OVERRIDE = 0x0010;
        static final int EH_CONNECTION_REMOVE_PATH = 0x0020;
        static final int STORAGE_CONNECTION_OVERRIDE = 0x0040;
        static final int STORAGE_CONTAINER_OVERRIDE = 0x0080;
        static final int STORAGE_BLOB_PREFIX_OVERRIDE = 0x0100;
        static final int EXECUTOR_OVERRIDE = 0x0200;
        static final int CHECKPOINT_MANAGER_OVERRIDE = 0x0400;
        static final int LEASE_MANAGER_OVERRIDE = 0x0800;
        static final int EXPLICIT_MANAGER = CHECKPOINT_MANAGER_OVERRIDE | LEASE_MANAGER_OVERRIDE;
        static final int TELLTALE_ON_TIMEOUT = 0x1000;

        private int flags;

        private String hostName;
        private String ehPath;
        private String consumerGroupName;
        private String ehConnection;
        private String storageConnection;
        private String storageContainerName;
        private String storageBlobPrefix;
        private ScheduledExecutorService executor;
        private ICheckpointManager checkpointManager;
        private ILeaseManager leaseManager;

        EPHConstructorArgs() {
            this.flags = 0;

            this.hostName = null;
            this.ehPath = null;
            this.consumerGroupName = null;
            this.ehConnection = null;
            this.storageConnection = null;
            this.storageContainerName = null;
            this.storageBlobPrefix = null;
            this.executor = null;
            this.checkpointManager = null;
            this.leaseManager = null;
        }

        int getFlags() {
            return this.flags;
        }

        boolean isFlagSet(int testFlag) {
            return ((this.flags & testFlag) != 0);
        }

        String getHostName() {
            return this.hostName;
        }

        void setHostName(String hostName) {
            this.hostName = hostName;
            this.flags |= HOST_OVERRIDE;
        }

        void setEHPath(String ehPath, int flags) {
            this.ehPath = ehPath;
            this.flags |= (flags & EH_PATH_OVERRIDE_AND_REPLACE);
        }

        String getEHPath() {
            return this.ehPath;
        }

        String getConsumerGroupName() {
            return this.consumerGroupName;
        }

        void setConsumerGroupName(String consumerGroupName) {
            this.consumerGroupName = consumerGroupName;
            this.flags |= CONSUMER_GROUP_OVERRIDE;
        }

        void removePathFromEHConnection() {
            this.flags |= EH_CONNECTION_REMOVE_PATH;
        }

        String getEHConnection() {
            return this.ehConnection;
        }

        void setEHConnection(String ehConnection) {
            this.ehConnection = ehConnection;
            this.flags |= EH_CONNECTION_OVERRIDE;
        }

        String getStorageConnection() {
            return this.storageConnection;
        }

        void setStorageConnection(String storageConnection) {
            this.storageConnection = storageConnection;
            this.flags |= STORAGE_CONNECTION_OVERRIDE;
        }

        void dummyStorageConnection() {
            setStorageConnection("DefaultEndpointsProtocol=https;AccountName=doesnotexist;AccountKey=dGhpcyBpcyBub3QgYSB2YWxpZCBrZXkgYnV0IGl0IGRvZXMgaGF2ZSA2MCBjaGFyYWN0ZXJzLjEyMzQ1Njc4OTAK;EndpointSuffix=core.windows.net");
        }

        void setDefaultStorageContainerName(String defaultStorageContainerName) {
            this.storageContainerName = defaultStorageContainerName;
        }

        String getStorageContainerName() {
            return this.storageContainerName;
        }

        void setStorageContainerName(String storageContainerName) {
            this.storageContainerName = storageContainerName;
            this.flags |= STORAGE_CONTAINER_OVERRIDE;
        }

        String getStorageBlobPrefix() {
            return this.storageBlobPrefix;
        }

        void setStorageBlobPrefix(String storageBlobPrefix) {
            this.storageBlobPrefix = storageBlobPrefix;
            this.flags |= STORAGE_BLOB_PREFIX_OVERRIDE;
        }

        ScheduledExecutorService getExecutor() {
            return this.executor;
        }

        void setExecutor(ScheduledExecutorService executor) {
            this.executor = executor;
            this.flags |= EXECUTOR_OVERRIDE;
        }

        boolean useExplicitManagers() {
            return ((this.flags & EXPLICIT_MANAGER) != 0);
        }

        void setCheckpointManager(ICheckpointManager checkpointManager) {
            this.checkpointManager = checkpointManager;
            this.flags |= CHECKPOINT_MANAGER_OVERRIDE;
        }

        ICheckpointManager getCheckpointMananger() {
            return this.checkpointManager;
        }

        ILeaseManager getLeaseManager() {
            return this.leaseManager;
        }

        void setLeaseManager(ILeaseManager leaseManager) {
            this.leaseManager = leaseManager;
            this.flags |= LEASE_MANAGER_OVERRIDE;
        }
    }
}
