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
package com.microsoft.windowsazure.storage.queue;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.UUID;

import org.junit.Test;

import com.microsoft.windowsazure.storage.LocationMode;
import com.microsoft.windowsazure.storage.ResultSegment;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageUri;
import com.microsoft.windowsazure.storage.TestHelper;

public final class CloudQueueClientTests extends QueueTestBase {

    @Test
    public void testQueueClientConstructor() throws URISyntaxException, StorageException {
        CloudQueueClient queueClient = new CloudQueueClient(account.getQueueEndpoint(), account.getCredentials());

        assertEquals(account.getQueueEndpoint(), queueClient.getEndpoint());
        assertEquals(account.getCredentials(), queueClient.getCredentials());
    }

    @Test
    public void testQueueClientConstructorInvalidParam() throws URISyntaxException, StorageException {
        try {
            new CloudQueueClient(new StorageUri(null), account.getCredentials());
            fail();
        }
        catch (IllegalArgumentException e) {

        }

        try {
            char[] name = new char[2000];
            new CloudQueueClient(new URI(name.toString()), account.getCredentials());
            fail();
        }
        catch (URISyntaxException e) {

        }
    }

    @Test
    public void testListQueuesSmallNumber() throws URISyntaxException, StorageException {
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
    public void testListQueuesAndListQueuesSegmentedLargeNumber() throws URISyntaxException, StorageException {
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
    public void testListQueuesSegmented() throws URISyntaxException, StorageException {
        String prefix = "segment" + UUID.randomUUID().toString().substring(0, 8).toLowerCase();

        HashMap<String, String> metadata1 = new HashMap<String, String>();
        metadata1.put("ExistingMetadata1", "ExistingMetadataValue1");

        for (int i = 0; i < 35; i++) {
            CloudQueue q = new CloudQueue(AppendQueueName(account.getQueueEndpoint(), prefix
                    + UUID.randomUUID().toString().toLowerCase()), qClient);
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

        ResultSegment<CloudQueue> segment3 = qClient.listQueuesSegmented(prefix, QueueListingDetails.NONE, 0, null,
                null, null);
        assertTrue(segment3.getLength() == 35);
    }

    @Test
    public void testListQueuesEqual() throws URISyntaxException, StorageException {
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

    @Test
    public void testTimeout() throws URISyntaxException, StorageException {
        assertTrue(qClient.getTimeoutInMs() == 30 * 1000);
        qClient.setTimeoutInMs(60 * 1000);
        assertTrue(qClient.getTimeoutInMs() == 60 * 1000);
    }

    static String AppendQueueName(URI baseURI, String queueName) throws URISyntaxException {
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
    public void testGetServiceStats() throws StorageException {
        CloudQueueClient qClient = createCloudQueueClient();
        qClient.setLocationMode(LocationMode.SECONDARY_ONLY);
        TestHelper.verifyServiceStats(qClient.getServiceStats());
    }
}
