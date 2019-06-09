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
package com.azure.data.cosmos.changefeed;

import com.azure.data.cosmos.CosmosContainer;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Implementation for the partition processor settings.
 */
public class ProcessorSettings {
    private CosmosContainer collectionSelfLink;
    private String partitionKeyRangeId;
    private Integer maxItemCount;
    private Duration feedPollDelay;
    private String startContinuation;
    private OffsetDateTime startTime;
//    private STRING sessionToken;

    public CosmosContainer getCollectionSelfLink() {
        return this.collectionSelfLink;
    }

    public ProcessorSettings withCollectionLink(CosmosContainer collectionLink) {
        this.collectionSelfLink = collectionLink;
        return this;
    }

    public String getPartitionKeyRangeId() {
        return this.partitionKeyRangeId;
    }

    public ProcessorSettings withPartitionKeyRangeId(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
        return this;
    }

    public int getMaxItemCount() {
        return this.maxItemCount;
    }

    public ProcessorSettings withMaxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    public Duration getFeedPollDelay() {
        return this.feedPollDelay;
    }

    public ProcessorSettings withFeedPollDelay(Duration feedPollDelay) {
        this.feedPollDelay = feedPollDelay;
        return this;
    }

    public String getStartContinuation() {
        return this.startContinuation;
    }

    public ProcessorSettings withStartContinuation(String startContinuation) {
        this.startContinuation = startContinuation;
        return this;
    }

    private boolean startFromBeginning;

    public boolean isStartFromBeginning() {
        return this.startFromBeginning;
    }

    public ProcessorSettings withStartFromBeginning(boolean startFromBeginning) {
        this.startFromBeginning = startFromBeginning;
        return this;
    }

    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    public ProcessorSettings withStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    // This is not currently supported in Java implementation.
//    public STRING sessionToken() {
//        return this.sessionToken;
//    }
//
//    public ProcessorSettings sessionToken(STRING sessionToken) {
//        this.sessionToken = sessionToken;
//        return this;
//    }
}
