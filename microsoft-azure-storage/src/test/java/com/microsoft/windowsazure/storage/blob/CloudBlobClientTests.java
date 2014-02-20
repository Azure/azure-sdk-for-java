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
package com.microsoft.windowsazure.storage.blob;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.LocationMode;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.ResultContinuation;
import com.microsoft.windowsazure.storage.ResultSegment;
import com.microsoft.windowsazure.storage.SendingRequestEvent;
import com.microsoft.windowsazure.storage.StorageEvent;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.TestHelper;
import com.microsoft.windowsazure.storage.TestRunners.CloudTests;
import com.microsoft.windowsazure.storage.TestRunners.DevFabricTests;
import com.microsoft.windowsazure.storage.TestRunners.DevStoreTests;

/**
 * Blob Client Tests
 */
public class CloudBlobClientTests extends BlobTestBase {
    /**
     * 
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListContainersTest() throws StorageException, URISyntaxException, IOException, InterruptedException {
        CloudBlobClient bClient = createCloudBlobClient();
        ArrayList<String> containerList = new ArrayList<String>();
        String prefix = UUID.randomUUID().toString();
        for (int i = 0; i < 30; i++) {
            containerList.add(prefix + i);
            bClient.getContainerReference(prefix + i).create();
        }

        int count = 0;
        for (final CloudBlobContainer container : bClient.listContainers(prefix)) {
            assertEquals(CloudBlobContainer.class, container.getClass());
            count++;
        }
        assertEquals(30, count);

        ResultContinuation token = null;
        do {

            ResultSegment<CloudBlobContainer> segment = bClient.listContainersSegmented(prefix,
                    ContainerListingDetails.ALL, 15, token, null, null);

            for (final CloudBlobContainer container : segment.getResults()) {
                container.downloadAttributes();
                assertEquals(CloudBlobContainer.class, container.getClass());
                containerList.remove(container.getName());
            }

            token = segment.getContinuationToken();
        } while (token != null);

        assertEquals(0, containerList.size());
    }

    @Test
    @Category({ CloudTests.class })
    public void testGetServiceStats() throws StorageException {
        CloudBlobClient bClient = createCloudBlobClient();
        bClient.setLocationMode(LocationMode.SECONDARY_ONLY);
        TestHelper.verifyServiceStats(bClient.getServiceStats());
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testSingleBlobPutThresholdInBytes() throws URISyntaxException, StorageException, IOException {
        CloudBlobClient bClient = createCloudBlobClient();

        try {
            bClient.setSingleBlobPutThresholdInBytes(BlobConstants.MAX_SINGLE_UPLOAD_BLOB_SIZE_IN_BYTES + 1);
            fail("Cannot set upload blob threshold above 64 MB");
        }
        catch (IllegalArgumentException e) {
            assertEquals("SingleBlobUploadThresholdInBytes", e.getMessage());
        }

        try {
            bClient.setSingleBlobPutThresholdInBytes(Constants.MB - 1);
            fail("Cannot set upload blob threshold below 1 MB");
        }
        catch (IllegalArgumentException e) {
            assertEquals("SingleBlobUploadThresholdInBytes", e.getMessage());
        }

        int maxSize = 2 * Constants.MB;

        bClient.setSingleBlobPutThresholdInBytes(maxSize);

        final ArrayList<Boolean> callList = new ArrayList<Boolean>();
        OperationContext sendingRequestEventContext = new OperationContext();
        sendingRequestEventContext.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                callList.add(true);
            }
        });

        assertEquals(0, callList.size());

        CloudBlobContainer container = null;
        try {
            container = bClient.getContainerReference(BlobTestBase.generateRandomContainerName());
            container.createIfNotExists();
            CloudBlockBlob blob = container.getBlockBlobReference(BlobTestBase
                    .generateRandomBlobNameWithPrefix("uploadThreshold"));

            // this should make a single call as it is less than the max
            blob.upload(BlobTestBase.getRandomDataStream(maxSize - 1), maxSize - 1, null, null,
                    sendingRequestEventContext);

            assertEquals(1, callList.size());

            // this should make one call as it is equal to the max
            blob.upload(BlobTestBase.getRandomDataStream(maxSize), maxSize, null, null, sendingRequestEventContext);

            assertEquals(2, callList.size());

            // this should make two calls as it is greater than the max
            blob.upload(BlobTestBase.getRandomDataStream(maxSize + 1), maxSize + 1, null, null,
                    sendingRequestEventContext);

            assertEquals(4, callList.size());
        }
        finally {
            container.deleteIfExists();
        }
    }
}