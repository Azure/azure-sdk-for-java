// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BatchOptionsTest {
    /**
     * Verifies that the default values are correctly set when creating an instance.
     */
    @Test
    public void createDefault() {
        BatchOptions options = new BatchOptions();
        Assertions.assertNull(options.getPartitionKey());
        Assertions.assertNull(options.getPartitionId());
    }

    /**
     * Verifies we can set maximumSizeInBytes on the batch options.
     */
    @Test
    public void setMaximumSize() {
        int size = 1024;
        BatchOptions options = new BatchOptions();

        options.setMaximumSizeInBytes(size);

        Assertions.assertEquals(size, options.getMaximumSizeInBytes());
        Assertions.assertNull(options.getPartitionKey());
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
        Assertions.assertEquals(partitionId, options.getPartitionId());
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
        Assertions.assertNotSame(clone, options);
        Assertions.assertEquals(size, options.getMaximumSizeInBytes());
        Assertions.assertEquals(partitionKey, options.getPartitionKey());
        Assertions.assertEquals(partitionId, options.getPartitionId());

        Assertions.assertEquals(partitionKey, clone.getPartitionKey());
        Assertions.assertEquals(size, clone.getMaximumSizeInBytes());
        Assertions.assertEquals(partitionId, clone.getPartitionId());
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
        Assertions.assertEquals(partitionKey, clone.getPartitionKey());
        Assertions.assertEquals(size, clone.getMaximumSizeInBytes());
        Assertions.assertEquals(partitionId, clone.getPartitionId());

        Assertions.assertEquals(originalSize, options.getMaximumSizeInBytes());
        Assertions.assertEquals(originalPartitionKey, options.getPartitionKey());
        Assertions.assertEquals(originalPartitionId, options.getPartitionId());
    }
}
