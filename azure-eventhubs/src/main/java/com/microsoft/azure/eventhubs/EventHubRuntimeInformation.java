/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

public final class EventHubRuntimeInformation
{
    final String path;
    final int partitionCount;
    final String[] partitionIds;
    
    EventHubRuntimeInformation(final String path, final int partitionCount, final String[] partitionIds)
    {
        this.path = path;
        this.partitionCount = partitionCount;
        this.partitionIds = partitionIds;
    }
    
    public String getPath()
    {
        return this.path;
    }
    
    public int getPartitionCount()
    {
        return this.partitionCount;
    }
    
    public String[] getPartitionIds()
    {
        return this.partitionIds;
    }
}
