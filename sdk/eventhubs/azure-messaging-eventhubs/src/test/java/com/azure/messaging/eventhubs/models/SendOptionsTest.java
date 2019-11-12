// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SendOptionsTest {

    /**
     * Verifies that the default values are correctly set when creating an instance.
     */
    @Test
    public void createDefault() {
        // Arrange
        SendOptions options = new SendOptions();

        // Act & Assert
        Assertions.assertNull(options.getPartitionKey());
        Assertions.assertNull(options.getPartitionId());
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
        options.setPartitionKey(partitionKey);

        // Assert
        Assertions.assertEquals(partitionKey, options.getPartitionKey());
    }

    /**
     * Verifies we can set partitionId on the send options.
     */
    @Test
    public void setPartitionId() {
        // Arrange
        String partitionId = "partition-id-9";
        SendOptions options = new SendOptions();

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
        String partitionId = "My-id";
        SendOptions options = new SendOptions()
            .setPartitionKey(partitionKey)
            .setPartitionId(partitionId);

        // Act
        SendOptions clone = options.clone();

        // Assert
        Assertions.assertNotSame(clone, options);

        Assertions.assertEquals(partitionId, options.getPartitionId());
        Assertions.assertEquals(partitionId, clone.getPartitionId());

        Assertions.assertEquals(partitionKey, options.getPartitionKey());
        Assertions.assertEquals(partitionKey, clone.getPartitionKey());
    }

    /**
     * Verifies we can modify contents of the clone without affecting the original.
     */
    @Test
    public void cloneModifyContents() {
        // Arrange
        String originalPartitionKey = "Some partition key";
        String partitionKey = "A new partition key";
        String originalPartitionId = "Original Id";
        String partitionId = "New id";

        SendOptions options = new SendOptions()
            .setPartitionKey(originalPartitionKey)
            .setPartitionId(originalPartitionId);
        SendOptions clone = options.clone();

        // Act
        clone.setPartitionKey(partitionKey);
        clone.setPartitionId(partitionId);

        // Assert
        Assertions.assertEquals(partitionKey, clone.getPartitionKey());
        Assertions.assertEquals(originalPartitionKey, options.getPartitionKey());

        Assertions.assertEquals(partitionId, clone.getPartitionId());
        Assertions.assertEquals(originalPartitionId, options.getPartitionId());
    }
}
