/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
