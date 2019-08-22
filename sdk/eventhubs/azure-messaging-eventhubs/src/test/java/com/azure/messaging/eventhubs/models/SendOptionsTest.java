// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import org.junit.Assert;
import org.junit.Test;

public class SendOptionsTest {

    /**
     * Verifies that the default values are correctly set when creating an instance.
     */
    @Test
    public void createDefault() {
        // Arrange
        SendOptions options = new SendOptions();

        // Act & Assert
        Assert.assertNull(options.partitionKey());
    }

    /**
     * Verifies we can set partitionKey on the send options.
     */
    @Test
    public void setPartitionKey() {
        // Arrange
        String partitionKey = "My partition key";
        SendOptions options = new SendOptions();

        // Act
        options.partitionKey(partitionKey);

        // Assert
        Assert.assertEquals(partitionKey, options.partitionKey());
    }

    /**
     * Verifies we can create a clone that is independent of the original.
     */
    @Test
    public void cloneIdentical() {
        // Arrange
        String partitionKey = "My partition key";
        SendOptions options = new SendOptions().partitionKey(partitionKey);

        // Act
        SendOptions clone = options.clone();

        // Assert
        Assert.assertNotSame(clone, options);

        Assert.assertEquals(partitionKey, options.partitionKey());
        Assert.assertEquals(partitionKey, clone.partitionKey());
    }


    /**
     * Verifies we can modify contents of the clone without affecting the original.
     */
    @Test
    public void cloneModifyContents() {
        // Arrange
        String originalPartitionKey = "Some partition key";
        String partitionKey = "A new partition key";

        SendOptions options = new SendOptions().partitionKey(originalPartitionKey);
        SendOptions clone = options.clone();

        // Act
        clone.partitionKey(partitionKey);

        // Assert
        Assert.assertEquals(partitionKey, clone.partitionKey());
        Assert.assertEquals(originalPartitionKey, options.partitionKey());
    }
}
