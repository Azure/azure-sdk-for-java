// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.ExponentialRetry;
import com.azure.core.amqp.Retry;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class EventHubProducerOptionsTest {
    @Test
    public void cloneProperties() {
        // Arrange
        String partitionId = "my-partition-id";
        Duration timeout = Duration.ofMinutes(10);
        Retry retry = new ExponentialRetry(Duration.ofSeconds(20), Duration.ofSeconds(30), 3);
        EventHubProducerOptions options = new EventHubProducerOptions();

        options.partitionId(partitionId)
            .timeout(timeout)
            .retry(retry)
            .partitionId(partitionId);

        // Act
        EventHubProducerOptions clone = (EventHubProducerOptions) options.clone();

        // Assert
        Assert.assertEquals(partitionId, clone.partitionId());
        Assert.assertEquals(timeout, clone.timeout());
        Assert.assertEquals(retry, clone.retry());

        Assert.assertNotSame(retry, clone.retry());
    }
}
