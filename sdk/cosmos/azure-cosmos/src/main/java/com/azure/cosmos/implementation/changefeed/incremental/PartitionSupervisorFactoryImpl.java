// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.incremental;

import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.LeaseRenewer;
import com.azure.cosmos.implementation.changefeed.PartitionProcessor;
import com.azure.cosmos.implementation.changefeed.PartitionProcessorFactory;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisor;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisorFactory;
import reactor.core.scheduler.Scheduler;

/**
 * Implementation for the partition supervisor factory.
 */
class PartitionSupervisorFactoryImpl  implements PartitionSupervisorFactory {
    private final ChangeFeedObserverFactory observerFactory;
    private final LeaseManager leaseManager;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final PartitionProcessorFactory partitionProcessorFactory;
    private final Scheduler scheduler;


    public PartitionSupervisorFactoryImpl(
            ChangeFeedObserverFactory observerFactory,
            LeaseManager leaseManager,
            PartitionProcessorFactory partitionProcessorFactory,
            ChangeFeedProcessorOptions options,
            Scheduler scheduler) {

        if (observerFactory == null) {
            throw new IllegalArgumentException("observerFactory");
        }

        if (leaseManager == null) {
            throw new IllegalArgumentException("leaseManager");
        }

        if (options == null) {
            throw new IllegalArgumentException("options");
        }

        if (partitionProcessorFactory == null) {
            throw new IllegalArgumentException("partitionProcessorFactory");
        }

        this.observerFactory = observerFactory;
        this.leaseManager = leaseManager;
        this.changeFeedProcessorOptions = options;
        this.partitionProcessorFactory = partitionProcessorFactory;
        this.scheduler = scheduler;
    }

    @Override
    public PartitionSupervisor create(Lease lease) {
        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        ChangeFeedObserver changeFeedObserver = this.observerFactory.createObserver();
        PartitionProcessor processor = this.partitionProcessorFactory.create(lease, changeFeedObserver);
        LeaseRenewer renewer = new LeaseRenewerImpl(lease, this.leaseManager, this.changeFeedProcessorOptions.getLeaseRenewInterval());

        return new PartitionSupervisorImpl(lease, changeFeedObserver, processor, renewer, this.scheduler);
    }
}
