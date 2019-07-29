// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.Bootstrapper;
import com.azure.data.cosmos.internal.changefeed.PartitionController;
import com.azure.data.cosmos.internal.changefeed.PartitionLoadBalancer;
import com.azure.data.cosmos.internal.changefeed.PartitionManager;
import reactor.core.publisher.Mono;

/**
 * Implementation for {@link PartitionManager}.
 */
class PartitionManagerImpl implements PartitionManager {
    private final Bootstrapper bootstrapper;
    private final PartitionController partitionController;
    private final PartitionLoadBalancer partitionLoadBalancer;

    public PartitionManagerImpl(Bootstrapper bootstrapper, PartitionController partitionController, PartitionLoadBalancer partitionLoadBalancer) {
        this.bootstrapper = bootstrapper;
        this.partitionController = partitionController;
        this.partitionLoadBalancer = partitionLoadBalancer;
    }

    @Override
    public Mono<Void> start() {
        PartitionManagerImpl self = this;

        return self.bootstrapper.initialize()
            .then(self.partitionController.initialize())
            .then(self.partitionLoadBalancer.start());
    }

    @Override
    public Mono<Void> stop() {
        PartitionManagerImpl self = this;
        return self.partitionLoadBalancer.stop();
    }
}
