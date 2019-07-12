// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.Assert;
import org.junit.Test;

public class SendOptionsTest {

    /**
     * Verifies that the default values are correctly set when creating an instance.
     */
    @Test
    public void createDefault() {
        SendOptions options = new SendOptions();

        // Assert.assertEquals(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, options.maximumSizeInBytes());
        Assert.assertNull(options.partitionKey());
    }

    /**
     * Verifies we can set maximumSizeInBytes on the send options.
     */
    @Test
    public void setMaximumSize() {
        int size = 1024;
        SendOptions options = new SendOptions();

        // options.maximumSizeInBytes(size);

        // Assert.assertEquals(size, options.maximumSizeInBytes());
        Assert.assertNull(options.partitionKey());
    }

    /**
     * Verifies we can set partitionKey on the send options.
     */
    @Test
    public void setPartitionKey() {
        String partitionKey = "My partition key";
        SendOptions options = new SendOptions();

        options.partitionKey(partitionKey);

        // Assert.assertEquals(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, options.maximumSizeInBytes());
        Assert.assertEquals(partitionKey, options.partitionKey());
    }

    /**
     * Verifies we can create a clone that is independent of the original.
     */
    @Test
    public void cloneIdentical() {
        // Arrange
        String partitionKey = "My partition key";
        int size = 800;
        // SendOptions options = new SendOptions().partitionKey(partitionKey).maximumSizeInBytes(size);
        SendOptions options = new SendOptions().partitionKey(partitionKey);

        // Act
        SendOptions clone = options.clone();

        // Assert
        Assert.assertNotSame(clone, options);
        // Assert.assertEquals(size, options.maximumSizeInBytes());
        Assert.assertEquals(partitionKey, options.partitionKey());

        Assert.assertEquals(partitionKey, clone.partitionKey());
        // Assert.assertEquals(size, clone.maximumSizeInBytes());
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

        // SendOptions options = new SendOptions().partitionKey(originalPartitionKey).maximumSizeInBytes(originalSize);
        SendOptions options = new SendOptions().partitionKey(originalPartitionKey);
        SendOptions clone = options.clone();

        // Act
        // clone.partitionKey(partitionKey).maximumSizeInBytes(size);
        clone.partitionKey(partitionKey);

        // Assert
        Assert.assertEquals(partitionKey, clone.partitionKey());
        // Assert.assertEquals(size, clone.maximumSizeInBytes());

        // Assert.assertEquals(originalSize, options.maximumSizeInBytes());
        Assert.assertEquals(originalPartitionKey, options.partitionKey());
    }
}
