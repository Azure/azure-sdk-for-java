// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateBatchOptionsTest {
    /**
     * Verifies that the default values are correctly set when creating an instance.
     */
    @Test
    public void createDefault() {
        CreateBatchOptions options = new CreateBatchOptions();
        Assertions.assertNull(options.getPartitionKey());
        Assertions.assertNull(options.getPartitionId());
    }

    /**
     * Verifies we can set maximumSizeInBytes on the batch options.
     */
    @Test
    public void setMaximumSize() {
        int size = 1024;
        CreateBatchOptions options = new CreateBatchOptions();

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
        CreateBatchOptions options = new CreateBatchOptions();

        // Act
        options.setPartitionId(partitionId);

        // Assert
        Assertions.assertEquals(partitionId, options.getPartitionId());
    }
}
