/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.time.Instant;

final public class EventHubPartitionRuntimeInformation {
	//final String type;
    private final String eventHubPath;
    private final String partitionId;
    private final long beginSequenceNumber;
    private final long lastEnqueuedSequenceNumber;
    private final String lastEnqueuedOffset;
    private final Instant lastEnqueuedTimeUtc;

    EventHubPartitionRuntimeInformation(
            final String eventHubPath,
            final String partitionId,
            final long beginSequenceNumber,
            final long lastEnqueuedSequenceNumber,
            final String lastEnqueuedOffset,
            final Instant lastEnqueuedTimeUtc) {
        this.eventHubPath = eventHubPath;
        this.partitionId = partitionId;
        this.beginSequenceNumber = beginSequenceNumber;
        this.lastEnqueuedSequenceNumber = lastEnqueuedSequenceNumber;
        this.lastEnqueuedOffset = lastEnqueuedOffset;
        this.lastEnqueuedTimeUtc = lastEnqueuedTimeUtc;
    }

    /*
	public String getType() {
		return this.type;
	}
	*/

    public String getEventHubPath() {
        return this.eventHubPath;
    }

    public String getPartitionId() {
        return this.partitionId;
    }

    public long getBeginSequenceNumber() {
        return this.beginSequenceNumber;
    }

    public long getLastEnqueuedSequenceNumber() {
        return this.lastEnqueuedSequenceNumber;
    }

    public String getLastEnqueuedOffset() {
        return this.lastEnqueuedOffset;
    }

    public Instant getLastEnqueuedTimeUtc() {
        return this.lastEnqueuedTimeUtc;
    }
}
