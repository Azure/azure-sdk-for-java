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

import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.LeaseCheckpointer;
import com.azure.data.cosmos.internal.changefeed.PartitionCheckpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Checkpoint the given partition up to the given continuation token.
 */
class PartitionCheckpointerImpl implements PartitionCheckpointer {
    private final Logger logger = LoggerFactory.getLogger(PartitionCheckpointerImpl.class);
    private final LeaseCheckpointer leaseCheckpointer;
    private Lease lease;

    public PartitionCheckpointerImpl(LeaseCheckpointer leaseCheckpointer, Lease lease) {
        this.leaseCheckpointer = leaseCheckpointer;
        this.lease = lease;
    }

    @Override
    public Mono<Void> checkpointPartition(String сontinuationToken) {
        PartitionCheckpointerImpl self = this;
        return this.leaseCheckpointer.checkpoint(this.lease, сontinuationToken)
            .map(lease1 -> {
                self.lease = lease1;
                logger.info(String.format("Checkpoint: partition %s, new continuation %s", self.lease.getLeaseToken(), self.lease.getContinuationToken()));
                return lease1;
            })
            .then();
    }
}
