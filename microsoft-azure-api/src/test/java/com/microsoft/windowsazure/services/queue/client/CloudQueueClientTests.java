/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.queue.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.StorageCredentials;
import com.microsoft.windowsazure.services.core.storage.StorageException;

public final class CloudQueueClientTests extends QueueTestBase {

    @Test
    public void testQueueClientConstructor() throws URISyntaxException, StorageException {
        StorageCredentials credentials = httpAcc.getCredentials();
        URI baseAddressUri = new URI(httpAcc.getQueueEndpoint().toString());

        CloudQueueClient queueClient = new CloudQueueClient(baseAddressUri, credentials);

        Assert.assertEquals(baseAddressUri, queueClient.getEndpoint());
        Assert.assertEquals(credentials, queueClient.getCredentials());
    }

    @Test
    public void testQueueClientConstructorInvalidParam() throws URISyntaxException, StorageException {
        StorageCredentials credentials = httpAcc.getCredentials();
        try {
            new CloudQueueClient(null, credentials);
            Assert.fail();
        }
        catch (IllegalArgumentException e) {

        }

        try {
            char[] name = new char[2000];
            new CloudQueueClient(new URI(name.toString()), credentials);
            Assert.fail();
        }
        catch (URISyntaxException e) {

        }
    }

    @Test
    public void testListQueuesSmallNumber() throws URISyntaxException, StorageException {
        int initialCount = 0;

        for (CloudQueue queue : qClient.listQueues()) {
            initialCount++;
        }

        HashMap<String, String> metadata1 = new HashMap<String, String>();
        metadata1.put("ExistingMetadata1", "ExistingMetadataValue1");

        for (int i = 0; i < 25; i++) {
            CloudQueue q = new CloudQueue(AppendQueueName(httpAcc.getQueueEndpoint(), UUID.randomUUID().toString()
                    .toLowerCase()), qClient);
            q.setMetadata(metadata1);
            q.create();
        }

        int count = 0;
        for (CloudQueue queue : qClient.listQueues()) {
            count++;
        }

        Assert.assertEquals(count, initialCount + 25);

        String perfix = "prefixtest" + UUID.randomUUID().toString().substring(0, 8).toLowerCase();
        for (int i = 0; i < 25; i++) {
            CloudQueue q = new CloudQueue(AppendQueueName(httpAcc.getQueueEndpoint(), perfix
                    + UUID.randomUUID().toString().toLowerCase()), qClient);
            HashMap<String, String> metadata2 = new HashMap<String, String>();
            metadata2.put("tags", q.getName());
            q.setMetadata(metadata2);
            q.create();
        }

        count = 0;
        for (CloudQueue queue : qClient.listQueues(perfix, QueueListingDetails.METADATA, null, null)) {
            count++;
            Assert.assertTrue(queue.getMetadata().size() == 1
                    && queue.getMetadata().get("tags").equals(queue.getName()));
        }

        Assert.assertEquals(count, 25);
    }

    @Test
    public void testListQueuesAndListQueuesSegmentedLargeNumber() throws URISyntaxException, StorageException {
        int count = 0;
        for (CloudQueue queue : qClient.listQueues()) {
            count++;
        }

        int totalLimit = 5005;
        if (count < totalLimit) {

            NumberFormat myFormat = NumberFormat.getInstance();
            myFormat.setMinimumIntegerDigits(4);

            for (int i = 0; i < totalLimit - count;) {
                String sub = myFormat.format(i);
                CloudQueue q = new CloudQueue(AppendQueueName(httpAcc.getQueueEndpoint(),
                        String.format("listqueue" + sub.replace(",", ""))), qClient);
                if (q.createIfNotExist())
                    i++;
            }

            count = 0;
            for (CloudQueue queue : qClient.listQueues()) {
                count++;
            }
        }

        Assert.assertTrue(count >= totalLimit);

        ResultSegment<CloudQueue> segment = qClient.listQueuesSegmented();
        Assert.assertTrue(segment.getLength() == 5000);
        Assert.assertTrue(segment.getContinuationToken() != null);
    }

    @Test
    public void testListQueuesSegmented() throws URISyntaxException, StorageException {
        String perfix = "segment" + UUID.randomUUID().toString().substring(0, 8).toLowerCase();

        HashMap<String, String> metadata1 = new HashMap<String, String>();
        metadata1.put("ExistingMetadata1", "ExistingMetadataValue1");

        for (int i = 0; i < 35; i++) {
            CloudQueue q = new CloudQueue(AppendQueueName(httpAcc.getQueueEndpoint(), perfix
                    + UUID.randomUUID().toString().toLowerCase()), qClient);
            q.setMetadata(metadata1);
            q.create();
        }

        ResultSegment<CloudQueue> segment1 = qClient.listQueuesSegmented(perfix);
        Assert.assertTrue(segment1.getLength() == 35);

        ResultSegment<CloudQueue> segment2 = qClient.listQueuesSegmented(perfix, QueueListingDetails.NONE, 5, null,
                null, null);
        Assert.assertTrue(segment2.getLength() == 5);

        int totalRoundTrip = 1;
        while (segment2.getHasMoreResults()) {
            segment2 = qClient.listQueuesSegmented(perfix, QueueListingDetails.NONE, 5,
                    segment2.getContinuationToken(), null, null);
            Assert.assertTrue(segment2.getLength() == 5);
            totalRoundTrip++;
        }

        Assert.assertTrue(totalRoundTrip == 7);

        ResultSegment<CloudQueue> segment3 = qClient.listQueuesSegmented(perfix, QueueListingDetails.NONE, 0, null,
                null, null);
        Assert.assertTrue(segment3.getLength() == 35);
    }

    @Test
    public void testListQueuesEqual() throws URISyntaxException, StorageException {
        int count1 = 0;
        for (CloudQueue queue : qClient.listQueues()) {
            count1++;
        }

        int count2 = 0;
        for (CloudQueue queue : qClient.listQueues("")) {
            count2++;
        }

        int count3 = 0;
        for (CloudQueue queue : qClient.listQueues(null)) {
            count3++;
        }

        Assert.assertEquals(count1, count2);
        Assert.assertEquals(count1, count3);
    }

    @Test
    public void testTimeout() throws URISyntaxException, StorageException {
        Assert.assertTrue(qClient.getTimeoutInMs() == 30 * 1000);
        qClient.setTimeoutInMs(60 * 1000);
        Assert.assertTrue(qClient.getTimeoutInMs() == 60 * 1000);
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
}
