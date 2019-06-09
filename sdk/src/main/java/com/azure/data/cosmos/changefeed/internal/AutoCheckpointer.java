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
import com.azure.data.cosmos.ChangeFeedObserverCloseReason;
import com.azure.data.cosmos.ChangeFeedObserverContext;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.changefeed.CheckpointFrequency;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Auto check-pointer implementation for {@link ChangeFeedObserver}.
 */
public class AutoCheckpointer implements ChangeFeedObserver {
    private final CheckpointFrequency checkpointFrequency;
    private final ChangeFeedObserver observer;
    private int processedDocCount;
    private ZonedDateTime lastCheckpointTime;

    public AutoCheckpointer(CheckpointFrequency checkpointFrequency, ChangeFeedObserver observer) {
        if (checkpointFrequency == null) throw new IllegalArgumentException("checkpointFrequency");
        if (observer == null) throw new IllegalArgumentException("observer");

        this.checkpointFrequency = checkpointFrequency;
        this.observer = observer;
        this.lastCheckpointTime = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @Override
    public void open(ChangeFeedObserverContext context) {
        this.observer.open(context);
    }

    @Override
    public void close(ChangeFeedObserverContext context, ChangeFeedObserverCloseReason reason) {
        this.observer.close(context, reason);
    }

    @Override
    public void processChanges(ChangeFeedObserverContext context, List<CosmosItemProperties> docs) {
        this.observer.processChanges(context, docs);
        this.processedDocCount ++;

        if (this.isCheckpointNeeded()) {
            context.checkpoint().block();
            this.processedDocCount = 0;
            this.lastCheckpointTime = ZonedDateTime.now(ZoneId.of("UTC"));
        }
    }

    private boolean isCheckpointNeeded() {
        if (this.checkpointFrequency.getProcessedDocumentCount() == 0 && this.checkpointFrequency.getTimeInterval() == null) {
            return true;
        }

        if (this.processedDocCount >= this.checkpointFrequency.getProcessedDocumentCount()) {
            return true;
        }

        Duration delta = Duration.between(this.lastCheckpointTime, ZonedDateTime.now(ZoneId.of("UTC")));

        if (delta.compareTo(this.checkpointFrequency.getTimeInterval()) >= 0) {
            return true;
        }

        return false;
    }
}
