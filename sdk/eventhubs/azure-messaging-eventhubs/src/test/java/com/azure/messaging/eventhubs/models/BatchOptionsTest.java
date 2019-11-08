// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import org.junit.Assert;
import org.junit.Test;

public class BatchOptionsTest {
    /**
     * Verifies that the default values are correctly set when creating an instance.
     */
    @Test
    public void createDefault() {
        BatchOptions options = new BatchOptions();
        Assert.assertNull(options.getPartitionKey());
        Assert.assertNull(options.getPartitionId());
    }

    /**
     * Verifies we can set maximumSizeInBytes on the batch options.
     */
    @Test
    public void setMaximumSize() {
        int size = 1024;
        BatchOptions options = new BatchOptions();

        options.setMaximumSizeInBytes(size);

        Assert.assertEquals(size, options.getMaximumSizeInBytes());
        Assert.assertNull(options.getPartitionKey());
    }

    /**
     * Verifies we can set partitionId on the batch options.
     */
    @Test
    public void setPartitionId() {
        // Arrange
        String partitionId = "partition-id-9";
        BatchOptions options = new BatchOptions();

        // Act
        options.setPartitionId(partitionId);

        // Assert
        Assert.assertEquals(partitionId, options.getPartitionId());
    }

    /**
     * Verifies we can create a clone that is independent of the original.
     */
    @Test
    public void cloneIdentical() {
        // Arrange
        String partitionKey = "My partition key";
        String partitionId = "partition-id";

        int size = 800;
        BatchOptions options = new BatchOptions()
            .setPartitionKey(partitionKey)
            .setMaximumSizeInBytes(size)
            .setPartitionId(partitionId);

        // Act
        BatchOptions clone = options.clone();

        // Assert
        Assert.assertNotSame(clone, options);
        Assert.assertEquals(size, options.getMaximumSizeInBytes());
        Assert.assertEquals(partitionKey, options.getPartitionKey());
        Assert.assertEquals(partitionId, options.getPartitionId());

        Assert.assertEquals(partitionKey, clone.getPartitionKey());
        Assert.assertEquals(size, clone.getMaximumSizeInBytes());
        Assert.assertEquals(partitionId, clone.getPartitionId());
    }


    /**
     * Verifies we can modify contents of the clone without affecting the original.
     */
    @Test
    public void cloneModifyContents() {
        // Arrange
        String originalPartitionKey = "Some partition key";
        String originalPartitionId = "Original-id";
        int originalSize = 100;

        String partitionKey = "A new partition key";
        String partitionId = "new-id";
        int size = 24;

        BatchOptions options = new BatchOptions()
            .setPartitionKey(originalPartitionKey)
            .setMaximumSizeInBytes(originalSize)
            .setPartitionId(originalPartitionId);
        BatchOptions clone = options.clone();

        // Act
        clone.setPartitionKey(partitionKey)
            .setMaximumSizeInBytes(size)
            .setPartitionId(partitionId);

        // Assert
        Assert.assertEquals(partitionKey, clone.getPartitionKey());
        Assert.assertEquals(size, clone.getMaximumSizeInBytes());
        Assert.assertEquals(partitionId, clone.getPartitionId());

        Assert.assertEquals(originalSize, options.getMaximumSizeInBytes());
        Assert.assertEquals(originalPartitionKey, options.getPartitionKey());
        Assert.assertEquals(originalPartitionId, options.getPartitionId());
    }
}
