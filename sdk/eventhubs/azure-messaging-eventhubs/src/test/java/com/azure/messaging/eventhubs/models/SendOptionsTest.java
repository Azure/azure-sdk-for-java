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
}
