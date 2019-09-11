// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.amqp.RetryOptions;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class EventHubProducerOptionsTest {
    @Test
    public void cloneProperties() {
        // Arrange
        String partitionId = "my-partition-id";
        Duration timeout = Duration.ofMinutes(10);
        RetryOptions retryOptions = new RetryOptions()
            .setTryTimeout(timeout)
            .setDelay(Duration.ofSeconds(20))
            .setMaxDelay(Duration.ofSeconds(30))
            .setMaxRetries(3);
        EventHubProducerOptions options = new EventHubProducerOptions();

        options.partitionId(partitionId)
            .retry(retryOptions)
            .partitionId(partitionId);

        // Act
        EventHubProducerOptions clone = options.clone();

        // Assert
        Assert.assertEquals(partitionId, clone.partitionId());
        Assert.assertEquals(timeout, clone.retry().getTryTimeout());
        Assert.assertEquals(retryOptions, clone.retry());

        Assert.assertNotSame(retryOptions, clone.retry());
    }
}
