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
