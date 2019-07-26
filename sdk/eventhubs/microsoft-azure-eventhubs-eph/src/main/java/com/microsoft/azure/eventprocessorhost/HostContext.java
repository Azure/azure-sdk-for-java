// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import java.util.concurrent.ScheduledExecutorService;

final class HostContext {
    private final ScheduledExecutorService executor;

    // Ideally we wouldn't need the host, but there are certain things which can be dynamically changed
    // by the user via APIs on the host and which need to be exposed on the HostContext. Passing the
    // call through is easier and safer than trying to keep two copies in sync.
    private final EventProcessorHost host;
    private final String hostName;

    private final String eventHubPath;
    private final String consumerGroupName;
    private final EventHubClientFactory eventHubClientFactory;

    private final ILeaseManager leaseManager;
    private final ICheckpointManager checkpointManager;

    // Cannot be final because it is not available at HostContext construction time.
    private EventProcessorOptions eventProcessorOptions = null;

    // Cannot be final because it is not available at HostContext construction time.
    private IEventProcessorFactory<?> processorFactory = null;


    HostContext(ScheduledExecutorService executor,
                EventProcessorHost host, String hostName,
                String eventHubPath, String consumerGroupName, EventHubClientFactory eventHubClientFactory,
                ILeaseManager leaseManager, ICheckpointManager checkpointManager) {
        this.executor = executor;

        this.host = host;
        this.hostName = hostName;

        this.eventHubPath = eventHubPath;
        this.consumerGroupName = consumerGroupName;
        this.eventHubClientFactory = eventHubClientFactory;

        this.leaseManager = leaseManager;
        this.checkpointManager = checkpointManager;
    }

    ScheduledExecutorService getExecutor() {
        return this.executor;
    }

    String getHostName() {
        return this.hostName;
    }

    String getEventHubPath() {
        return this.eventHubPath;
    }

    String getConsumerGroupName() {
        return this.consumerGroupName;
    }

    EventHubClientFactory getEventHubClientFactory() {
        return this.eventHubClientFactory;
    }

    ILeaseManager getLeaseManager() {
        return this.leaseManager;
    }

    ICheckpointManager getCheckpointManager() {
        return this.checkpointManager;
    }

    PartitionManagerOptions getPartitionManagerOptions() {
        return this.host.getPartitionManagerOptions();
    }

    // May be null if called too early! Not set until register time.
    // In particular, store initialization happens before this is set.
    EventProcessorOptions getEventProcessorOptions() {
        return this.eventProcessorOptions;
    }

    void setEventProcessorOptions(EventProcessorOptions epo) {
        this.eventProcessorOptions = epo;
    }

    // May be null if called too early! Not set until register time.
    // In particular, store initialization happens before this is set.
    IEventProcessorFactory<?> getEventProcessorFactory() {
        return this.processorFactory;
    }

    void setEventProcessorFactory(IEventProcessorFactory<?> pf) {
        this.processorFactory = pf;
    }

    //
    // Logging utility functions. They are here rather than on LoggingUtils because they
    // make use of this.hostName.
    //

    String withHost(String logMessage) {
        return "host " + this.hostName + ": " + logMessage;
    }

    String withHostAndPartition(String partitionId, String logMessage) {
        return withHost(partitionId + ": " + logMessage);
    }

    String withHostAndPartition(PartitionContext context, String logMessage) {
        return withHostAndPartition(context.getPartitionId(), logMessage);
    }

    String withHostAndPartition(BaseLease lease, String logMessage) {
        return withHostAndPartition(lease.getPartitionId(), logMessage);
    }
}
