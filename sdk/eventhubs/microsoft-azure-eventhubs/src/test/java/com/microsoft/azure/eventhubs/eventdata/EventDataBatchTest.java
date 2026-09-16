// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.eventdata;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PayloadSizeExceededException;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Executors;

public class EventDataBatchTest extends ApiTestBase {
    private EventHubClient ehClient;

    @Test
    public void payloadExceededException() throws EventHubException, IOException {
        final ConnectionStringBuilder connStrBuilder = TestContext.getConnectionString();
        ehClient = EventHubClient.createFromConnectionStringSync(connStrBuilder.toString(), Executors.newScheduledThreadPool(1));

        final EventDataBatch batch = ehClient.createBatch();

        final EventData within = EventData.create(new byte[1024]);
        final EventData tooBig = EventData.create(new byte[1024 * 1024 * 2]);

        Assertions.assertTrue(batch.tryAdd(within));
        Assertions.assertThrows(PayloadSizeExceededException.class, () -> batch.tryAdd(tooBig));
    }

    @AfterEach
    public void cleanup() throws EventHubException {
        if (ehClient != null) {
            ehClient.closeSync();
        }
    }
}
