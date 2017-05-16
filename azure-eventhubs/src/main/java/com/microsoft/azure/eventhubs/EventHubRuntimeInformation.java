/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.time.Instant;

final public class EventHubRuntimeInformation {
	//final String type;
    final String path;
	final Instant createdAt;
    final int partitionCount;
    final String[] partitionIds;

    EventHubRuntimeInformation(final String path, final Instant createdAt, final int partitionCount, final String[] partitionIds) {
        this.path = path;
        this.createdAt = createdAt;
        this.partitionCount = partitionCount;
        this.partitionIds = partitionIds;
    }

    /*
	public String getType() {
		return this.type;
	}
	*/

    public String getPath() {
        return this.path;
    }

	public Instant getCreatedAt() {
		return this.createdAt;
	}

    public int getPartitionCount() {
        return this.partitionCount;
    }

    public String[] getPartitionIds() {
        return this.partitionIds;
    }
}
