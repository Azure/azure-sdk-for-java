// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.storage.queue;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("storage-queue")
public class StorageQueueIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageQueueIT.class);
    private static final String DATA = "storage queue test";

    @Autowired
    private QueueClient client;

    @Test
    public void testStorageQueueOperation() {
        LOGGER.info("StorageQueueIT begin.");
        client.create();
        client.sendMessage(DATA);
        QueueMessageItem queueMessageItem = client.receiveMessage();
        Assertions.assertEquals(DATA, queueMessageItem.getBody().toString());
        LOGGER.info("StorageQueueIT end.");
    }

}
