// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.storage.queue.implementation.models.MessageIdsUpdateHeaders;
import com.azure.storage.queue.implementation.models.QueuesGetPropertiesHeaders;
import com.azure.storage.queue.implementation.util.ModelHelper;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.UpdateMessageResult;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link ModelHelper} projections that are driven purely by response headers. These operations
 * return no body, so the header models are the only carrier of their result and nothing else in the test suite asserts
 * the values they produce.
 */
public class ModelHelperTests {

    @Test
    public void transformUpdateMessageResultReadsPopReceiptAndTimeNextVisible() {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-popreceipt"), "AgAAAAMAAAAAAAAA")
            .set(HttpHeaderName.fromString("x-ms-time-next-visible"), "Mon, 17 Apr 2023 15:28:23 GMT");

        UpdateMessageResult result = ModelHelper.transformUpdateMessageResult(new MessageIdsUpdateHeaders(headers));

        assertEquals("AgAAAAMAAAAAAAAA", result.getPopReceipt());
        assertEquals(OffsetDateTime.of(2023, 4, 17, 15, 28, 23, 0, ZoneOffset.UTC), result.getTimeNextVisible());
    }

    @Test
    public void transformUpdateMessageResultToleratesMissingHeaders() {
        UpdateMessageResult result
            = ModelHelper.transformUpdateMessageResult(new MessageIdsUpdateHeaders(new HttpHeaders()));

        assertNull(result.getPopReceipt());
        assertNull(result.getTimeNextVisible());
    }

    @Test
    public void transformQueuePropertiesReadsMetadataCollectionAndCount() {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-meta-owner"), "queueTeam")
            .set(HttpHeaderName.fromString("X-MS-META-Region"), "westus2")
            .set(HttpHeaderName.fromString("x-ms-approximate-messages-count"), "7");

        QueueProperties properties = ModelHelper.transformQueueProperties(new QueuesGetPropertiesHeaders(headers));

        assertEquals(7, properties.getApproximateMessagesCount());
        assertEquals(2, properties.getMetadata().size());
        assertEquals("queueTeam", properties.getMetadata().get("owner"));
        // The x-ms-meta- prefix match is case-insensitive, and only the key suffix is retained.
        assertEquals("westus2", properties.getMetadata().get("Region"));
    }

    @Test
    public void transformQueuePropertiesReturnsEmptyMetadataWhenNoneOnTheWire() {
        QueueProperties properties
            = ModelHelper.transformQueueProperties(new QueuesGetPropertiesHeaders(new HttpHeaders()));

        // QueueProperties shipped returning an empty map (not null) when a queue carries no metadata.
        assertNotNull(properties.getMetadata());
        assertTrue(properties.getMetadata().isEmpty());
        assertEquals(0, properties.getApproximateMessagesCount());
    }
}
