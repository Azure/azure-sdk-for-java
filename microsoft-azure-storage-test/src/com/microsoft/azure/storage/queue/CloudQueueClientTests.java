/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.queue;

import com.microsoft.azure.storage.LocationMode;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestHelper;
import com.microsoft.azure.storage.core.SR;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.UUID;

import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;

import static org.junit.Assert.*;

public final class CloudQueueClientTests {

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListQueuesSmallNumber() throws URISyntaxException, StorageException {
        CloudQueueClient qClient = QueueTestHelper.createCloudQueueClient();
        int initialCount = 0;
        String prefix = UUID.randomUUID().toString().toLowerCase();
        for (CloudQueue queue : qClient.listQueues(prefix)) {
            assertNotNull(queue);
            initialCount++;
        }

        HashMap<String, String> metadata1 = new HashMap<String, String>();
        metadata1.put("ExistingMetadata1", "ExistingMetadataValue1");

        for (int i = 0; i < 25; i++) {
            CloudQueue q = qClient.getQueueReference(prefix + i);
            q.setMetadata(metadata1);
            q.create();
        }

        int count = 0;
        for (CloudQueue queue : qClient.listQueues(prefix)) {
            assertNotNull(queue);
            count++;
        }

        assertEquals(count, initialCount + 25);

        // with metadata
        for (int i = 0; i < 25; i++) {
            CloudQueue q = qClient.getQueueReference(prefix + "a" + i);
            HashMap<String, String> metadata2 = new HashMap<String, String>();
            metadata2.put("tags", q.getName());
            q.setMetadata(metadata2);
            q.create();
        }

        count = 0;
        for (CloudQueue queue : qClient.listQueues(prefix + "a", QueueListingDetails.METADATA, null, null)) {
            count++;
            assertTrue(queue.getMetadata().size() == 1 && queue.getMetadata().get("tags").equals(queue.getName()));
        }

        assertEquals(count, 25);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListQueuesAndListQueuesSegmentedLargeNumber() throws URISyntaxException, StorageException {
        CloudQueueClient qClient = QueueTestHelper.createCloudQueueClient();
        int count = 0;
        String prefix = UUID.randomUUID().toString();
        for (CloudQueue queue : qClient.listQueues(prefix)) {
            assertNotNull(queue);
            count++;
        }

        int totalLimit = 505;
        if (count < totalLimit) {

            NumberFormat myFormat = NumberFormat.getInstance();
            myFormat.setMinimumIntegerDigits(4);

            for (int i = 0; i < totalLimit - count; i++) {
                CloudQueue q = qClient.getQueueReference(prefix + i);
                q.createIfNotExists();
            }
        }

        ResultSegment<CloudQueue> segment = qClient.listQueuesSegmented(prefix, QueueListingDetails.NONE, 500, null,
                null, null);
        assertTrue(segment.getLength() == 500);
        assertTrue(segment.getContinuationToken() != null);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListQueuesSegmented() throws URISyntaxException, StorageException {
        CloudQueueClient qClient = QueueTestHelper.createCloudQueueClient();
        String prefix = "segment" + UUID.randomUUID().toString().substring(0, 8).toLowerCase();

        HashMap<String, String> metadata1 = new HashMap<String, String>();
        metadata1.put("ExistingMetadata1", "ExistingMetadataValue1");

        for (int i = 0; i < 35; i++) {
            CloudQueue q = qClient.getQueueReference(prefix + UUID.randomUUID().toString().toLowerCase());
            q.setMetadata(metadata1);
            q.create();
        }

        ResultSegment<CloudQueue> segment1 = qClient.listQueuesSegmented(prefix);
        assertTrue(segment1.getLength() == 35);

        ResultSegment<CloudQueue> segment2 = qClient.listQueuesSegmented(prefix, QueueListingDetails.NONE, 5, null,
                null, null);
        assertTrue(segment2.getLength() == 5);

        int totalRoundTrip = 1;
        while (segment2.getHasMoreResults()) {
            segment2 = qClient.listQueuesSegmented(prefix, QueueListingDetails.NONE, 5,
                    segment2.getContinuationToken(), null, null);
            assertTrue(segment2.getLength() == 5);
            totalRoundTrip++;
        }

        assertTrue(totalRoundTrip == 7);

        ResultSegment<CloudQueue> segment3 = qClient.listQueuesSegmented(prefix, QueueListingDetails.NONE,
                null, null, null, null);
        assertTrue(segment3.getLength() == 35);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListQueuesSegmentedMaxResultsValidation() throws URISyntaxException, StorageException {
        CloudQueueClient qClient = QueueTestHelper.createCloudQueueClient();

        // Validation should cause each of these to fail
        for (int i = 0; i >= -2; i--) {
            try {
                qClient.listQueuesSegmented(null, QueueListingDetails.NONE, i, null, null, null);
                fail();
            }
            catch (IllegalArgumentException e) {
                assertTrue(String.format(SR.PARAMETER_SHOULD_BE_GREATER_OR_EQUAL, "maxResults", 1)
                        .equals(e.getMessage()));
            }
        }
        assertNotNull(qClient.listQueuesSegmented("thereshouldntbeanyqueueswiththisprefix"));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListQueuesEqual() throws StorageException {
        CloudQueueClient qClient = QueueTestHelper.createCloudQueueClient();
        int count1 = 0;
        for (CloudQueue queue : qClient.listQueues()) {
            assertNotNull(queue);
            count1++;
        }

        int count2 = 0;
        for (CloudQueue queue : qClient.listQueues("")) {
            assertNotNull(queue);
            count2++;
        }

        int count3 = 0;
        for (CloudQueue queue : qClient.listQueues(null)) {
            assertNotNull(queue);
            count3++;
        }

        assertEquals(count1, count2);
        assertEquals(count1, count3);
    }

    static String AppendQueueName(URI baseURI, String queueName) {
        if (baseURI == null)
            return queueName;

        String baseAddress = baseURI.toString();
        if (baseAddress.endsWith("/")) {
            return baseAddress + queueName;
        }
        else {
            return baseAddress + "/" + queueName;
        }
    }

    @Test
    @Category({ CloudTests.class })
    public void testGetServiceStats() throws StorageException {
        CloudQueueClient qClient = QueueTestHelper.createCloudQueueClient();
        qClient.getDefaultRequestOptions().setLocationMode(LocationMode.SECONDARY_ONLY);
        TestHelper.verifyServiceStats(qClient.getServiceStats());
    }
}
