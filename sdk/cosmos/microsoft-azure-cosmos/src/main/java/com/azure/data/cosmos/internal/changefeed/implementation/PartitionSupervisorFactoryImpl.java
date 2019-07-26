// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.ChangeFeedProcessorOptions;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverFactory;
import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.LeaseManager;
import com.azure.data.cosmos.internal.changefeed.LeaseRenewer;
import com.azure.data.cosmos.internal.changefeed.PartitionProcessor;
import com.azure.data.cosmos.internal.changefeed.PartitionProcessorFactory;
import com.azure.data.cosmos.internal.changefeed.PartitionSupervisor;
import com.azure.data.cosmos.internal.changefeed.PartitionSupervisorFactory;

import java.util.concurrent.ExecutorService;

/**
 * Implementation for the partition supervisor factory.
 */
class PartitionSupervisorFactoryImpl  implements PartitionSupervisorFactory {
    private final ChangeFeedObserverFactory observerFactory;
    private final LeaseManager leaseManager;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final PartitionProcessorFactory partitionProcessorFactory;
    private final ExecutorService executorService;


    public PartitionSupervisorFactoryImpl(
        ChangeFeedObserverFactory observerFactory,
        LeaseManager leaseManager,
        PartitionProcessorFactory partitionProcessorFactory,
        ChangeFeedProcessorOptions options,
        ExecutorService executorService) {
        if (observerFactory == null) throw new IllegalArgumentException("observerFactory");
        if (leaseManager == null) throw new IllegalArgumentException("leaseManager");
        if (options == null) throw new IllegalArgumentException("options");
        if (partitionProcessorFactory == null) throw new IllegalArgumentException("partitionProcessorFactory");

        this.observerFactory = observerFactory;
        this.leaseManager = leaseManager;
        this.changeFeedProcessorOptions = options;
        this.partitionProcessorFactory = partitionProcessorFactory;
        this.executorService = executorService;
    }

    @Override
    public PartitionSupervisor create(Lease lease) {
        if (lease == null) throw new IllegalArgumentException("lease");

        ChangeFeedObserver changeFeedObserver = this.observerFactory.createObserver();
        PartitionProcessor processor = this.partitionProcessorFactory.create(lease, changeFeedObserver);
        LeaseRenewer renewer = new LeaseRenewerImpl(lease, this.leaseManager, this.changeFeedProcessorOptions.leaseRenewInterval());

        return new PartitionSupervisorImpl(lease, changeFeedObserver, processor, renewer, this.executorService);
    }
}
