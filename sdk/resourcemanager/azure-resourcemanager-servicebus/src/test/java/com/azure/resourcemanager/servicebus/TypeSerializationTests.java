// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus;

import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.servicebus.fluent.models.SBQueueInner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class TypeSerializationTests {

    @Test
    public void testQueueDurationSerialization() throws Exception {
        SerializerAdapter adapter = SerializerFactory.createDefaultManagementSerializerAdapter();

        String queueJson = "{\"id\":\"/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/javasb759933551/providers/Microsoft.ServiceBus/namespaces/jvsbns56571ea/queues/queue1-39374a\",\"name\":\"queue1-39374a\",\"type\":\"Microsoft.ServiceBus/Namespaces/Queues\",\"location\":\"East US\",\"properties\":{\"lockDuration\":\"PT1M\",\"maxSizeInMegabytes\":1024,\"requiresDuplicateDetection\":false,\"requiresSession\":false,\"defaultMessageTimeToLive\":\"P10675199DT2H48M5.4775807S\",\"deadLetteringOnMessageExpiration\":false,\"enableBatchedOperations\":true,\"duplicateDetectionHistoryTimeWindow\":\"PT10M\",\"maxDeliveryCount\":10,\"sizeInBytes\":0,\"messageCount\":0,\"status\":\"Active\",\"autoDeleteOnIdle\":\"P10675199DT2H48M5.4775807S\",\"enablePartitioning\":false,\"enableExpress\":false,\"countDetails\":{\"activeMessageCount\":0,\"deadLetterMessageCount\":0,\"scheduledMessageCount\":0,\"transferMessageCount\":0,\"transferDeadLetterMessageCount\":0},\"createdAt\":\"2021-03-24T07:16:45.943Z\",\"updatedAt\":\"2021-03-24T07:16:45.973Z\",\"accessedAt\":\"0001-01-01T00:00:00\"}}";

        SBQueueInner queue = adapter.deserialize(queueJson, SBQueueInner.class, SerializerEncoding.JSON);
        Duration autoDeleteOnIdle = queue.autoDeleteOnIdle();
        Assertions.assertEquals(922337203685L, autoDeleteOnIdle.getSeconds());
        Assertions.assertEquals(477580700L, autoDeleteOnIdle.getNano());

        String json = adapter.serialize(queue, SerializerEncoding.JSON);
        Assertions.assertTrue(json.contains("P10675199DT2H48M5.4775807S"));
    }
}
