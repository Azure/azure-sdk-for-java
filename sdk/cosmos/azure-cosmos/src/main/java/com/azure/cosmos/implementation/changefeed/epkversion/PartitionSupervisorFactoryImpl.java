// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.LeaseRenewer;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisor;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisorFactory;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import reactor.core.scheduler.Scheduler;


import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for the partition supervisor factory.
 */
class PartitionSupervisorFactoryImpl<T> implements PartitionSupervisorFactory {
    private final ChangeFeedObserverFactory<T> observerFactory;
    private final LeaseManager leaseManager;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final PartitionProcessorFactory<T> partitionProcessorFactory;
    private final Scheduler scheduler;
    private final Class<T> partitionProcessItemType;


    public PartitionSupervisorFactoryImpl(
            ChangeFeedObserverFactory<T> observerFactory,
            LeaseManager leaseManager,
            PartitionProcessorFactory<T> partitionProcessorFactory,
            ChangeFeedProcessorOptions options,
            Scheduler scheduler,
            Class<T> partitionProcessItemType) {

        checkNotNull(observerFactory, "Argument 'observerFactory' can not be null");
        checkNotNull(leaseManager, "Argument 'leaseManager' can not be null");
        checkNotNull(options, "Argument 'options' can not be null");
        checkNotNull(partitionProcessorFactory, "Argument 'partitionProcessorFactory' can not be null");
        checkNotNull(partitionProcessItemType, "Argument 'partitionProcessItemType' can not be null");

        this.observerFactory = observerFactory;
        this.leaseManager = leaseManager;
        this.changeFeedProcessorOptions = options;
        this.partitionProcessorFactory = partitionProcessorFactory;
        this.scheduler = scheduler;
        this.partitionProcessItemType = partitionProcessItemType;
    }

    @Override
    public PartitionSupervisor create(Lease lease) {
        checkNotNull(lease, "Argument 'lease' can not be null");

        ChangeFeedObserver<T> changeFeedObserver = this.observerFactory.createObserver();
        PartitionProcessor processor = this.partitionProcessorFactory.create(lease, changeFeedObserver, this.partitionProcessItemType);
        LeaseRenewer renewer = new LeaseRenewerImpl(lease, this.leaseManager, this.changeFeedProcessorOptions.getLeaseRenewInterval());

        return new PartitionSupervisorImpl<>(lease, changeFeedObserver, processor, renewer, this.scheduler);
    }
}
