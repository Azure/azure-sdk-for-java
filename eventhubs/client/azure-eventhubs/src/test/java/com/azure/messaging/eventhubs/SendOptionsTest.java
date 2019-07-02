// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.Assert;
import org.junit.Test;

public class SendOptionsTest {

    /**
     * Verifies that the default values are correctly set when creating an instance.
     */
    @Test
    public void createDefault() {
        SendOptions options = new SendOptions();

        Assert.assertEquals(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, options.maximumSizeInBytes());
        Assert.assertNull(options.partitionKey());
    }

    /**
     * Verifies we can set maximumSizeInBytes on the send options.
     */
    @Test
    public void setMaximumSize() {
        int size = 1024;
        SendOptions options = new SendOptions();

        options.maximumSizeInBytes(size);

        Assert.assertEquals(size, options.maximumSizeInBytes());
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

        Assert.assertEquals(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, options.maximumSizeInBytes());
        Assert.assertEquals(partitionKey, options.partitionKey());
    }
}
