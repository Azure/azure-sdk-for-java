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
package com.azure.data.cosmos.changefeed.internal;

import com.azure.data.cosmos.ChangeFeedObserver;
import com.azure.data.cosmos.ChangeFeedProcessorOptions;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.changefeed.Lease;
import com.azure.data.cosmos.changefeed.LeaseCheckpointer;
import com.azure.data.cosmos.changefeed.PartitionCheckpointer;
import com.azure.data.cosmos.changefeed.PartitionProcessor;
import com.azure.data.cosmos.changefeed.PartitionProcessorFactory;
import com.azure.data.cosmos.changefeed.ProcessorSettings;

/**
 * Implementation for {@link PartitionProcessorFactory}.
 */
public class PartitionProcessorFactoryImpl implements PartitionProcessorFactory {
    private final ChangeFeedContextClient documentClient;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final LeaseCheckpointer leaseCheckpointer;
    private final CosmosContainer collectionSelfLink;

    public PartitionProcessorFactoryImpl(
        ChangeFeedContextClient documentClient,
        ChangeFeedProcessorOptions changeFeedProcessorOptions,
        LeaseCheckpointer leaseCheckpointer,
        CosmosContainer collectionSelfLink) {

        if (documentClient == null) throw new IllegalArgumentException("documentClient");
        if (changeFeedProcessorOptions == null) throw new IllegalArgumentException("changeFeedProcessorOptions");
        if (leaseCheckpointer == null) throw new IllegalArgumentException("leaseCheckpointer");
        if (collectionSelfLink == null) throw new IllegalArgumentException("collectionSelfLink");

        this.documentClient = documentClient;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.leaseCheckpointer = leaseCheckpointer;
        this.collectionSelfLink = collectionSelfLink;
    }

    @Override
    public PartitionProcessor create(Lease lease, ChangeFeedObserver observer) {
        if (observer == null) throw new IllegalArgumentException("observer");
        if (lease == null) throw new IllegalArgumentException("lease");

        String startContinuation = lease.getContinuationToken();

        if (startContinuation == null || startContinuation.isEmpty()) {
            startContinuation = this.changeFeedProcessorOptions.startContinuation();
        }

        ProcessorSettings settings = new ProcessorSettings()
            .withCollectionLink(this.collectionSelfLink)
            .withStartContinuation(startContinuation)
            .withPartitionKeyRangeId(lease.getLeaseToken())
            .withFeedPollDelay(this.changeFeedProcessorOptions.feedPollDelay())
            .withMaxItemCount(this.changeFeedProcessorOptions.maxItemCount())
            .withStartFromBeginning(this.changeFeedProcessorOptions.startFromBeginning())
            .withStartTime(this.changeFeedProcessorOptions.startTime());  // .sessionToken(this.changeFeedProcessorOptions.sessionToken());

        PartitionCheckpointer checkpointer = new PartitionCheckpointerImpl(this.leaseCheckpointer, lease);
        return new PartitionProcessorImpl(observer, this.documentClient, settings, checkpointer);
    }
}
