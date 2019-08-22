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
        Assert.assertNull(options.partitionKey());
    }

    /**
     * Verifies we can set maximumSizeInBytes on the batch options.
     */
    @Test
    public void setMaximumSize() {
        int size = 1024;
        BatchOptions options = new BatchOptions();

        options.maximumSizeInBytes(size);

        Assert.assertEquals(size, options.maximumSizeInBytes());
        Assert.assertNull(options.partitionKey());
    }

    /**
     * Verifies we can create a clone that is independent of the original.
     */
    @Test
    public void cloneIdentical() {
        // Arrange
        String partitionKey = "My partition key";
        int size = 800;
        BatchOptions options = new BatchOptions().partitionKey(partitionKey).maximumSizeInBytes(size);

        // Act
        BatchOptions clone = options.clone();

        // Assert
        Assert.assertNotSame(clone, options);
        Assert.assertEquals(size, options.maximumSizeInBytes());
        Assert.assertEquals(partitionKey, options.partitionKey());

        Assert.assertEquals(partitionKey, clone.partitionKey());
        Assert.assertEquals(size, clone.maximumSizeInBytes());
    }


    /**
     * Verifies we can modify contents of the clone without affecting the original.
     */
    @Test
    public void cloneModifyContents() {
        // Arrange
        String originalPartitionKey = "Some partition key";
        int originalSize = 100;

        String partitionKey = "A new partition key";
        int size = 24;

        BatchOptions options = new BatchOptions().partitionKey(originalPartitionKey).maximumSizeInBytes(originalSize);
        BatchOptions clone = options.clone();

        // Act
        clone.partitionKey(partitionKey)
            .maximumSizeInBytes(size);

        // Assert
        Assert.assertEquals(partitionKey, clone.partitionKey());
        Assert.assertEquals(size, clone.maximumSizeInBytes());

        Assert.assertEquals(originalSize, options.maximumSizeInBytes());
        Assert.assertEquals(originalPartitionKey, options.partitionKey());
    }
}
