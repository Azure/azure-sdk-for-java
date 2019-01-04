/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.eventdata;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Executors;

public class EventDataBatchTest extends ApiTestBase {

    private static EventHubClient ehClient;

    @Test(expected = PayloadSizeExceededException.class)
    public void payloadExceededException() throws EventHubException, IOException {
        final ConnectionStringBuilder connStrBuilder = TestContext.getConnectionString();
        ehClient = EventHubClient.createSync(connStrBuilder.toString(), Executors.newScheduledThreadPool(1));

        final EventDataBatch batch = ehClient.createBatch();

        final EventData within = EventData.create(new byte[1024]);
        final EventData tooBig = EventData.create(new byte[1024 * 1024 * 2]);

        Assert.assertTrue(batch.tryAdd(within));
        batch.tryAdd(tooBig);
    }
}
