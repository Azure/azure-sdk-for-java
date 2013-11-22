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

import com.microsoft.windowsazure.storage.LocationMode;
import com.microsoft.windowsazure.storage.ResultContinuation;
import com.microsoft.windowsazure.storage.ResultSegment;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageLocation;
import com.microsoft.windowsazure.storage.TestHelper;

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
    public void getServiceStats() throws StorageException {
        CloudBlobClient bClient = createCloudBlobClient();
        bClient.setLocationMode(LocationMode.SECONDARY_ONLY);
        TestHelper.verifyServiceStats(bClient.getServiceStats());
    }

    @Test
    public void testCloudBlobClientListContainersInMultiLocations() throws StorageException, URISyntaxException {
        String name = generateRandomContainerName();
        ArrayList<String> containerNames = new ArrayList<String>();
        CloudBlobClient blobClient = createCloudBlobClient();

        try {
            for (Integer i = 0; i < 2; i++) {
                String containerName = name + i.toString();
                containerNames.add(containerName);
                blobClient.getContainerReference(containerName).create();
            }

            ResultSegment<CloudBlobContainer> resultSegment = blobClient.listContainersSegmented(name,
                    ContainerListingDetails.NONE, 1, null, null, null);
            assertEquals(StorageLocation.PRIMARY, resultSegment.getContinuationToken().getTargetLocation());

            BlobRequestOptions options = new BlobRequestOptions();
            options.setLocationMode(LocationMode.SECONDARY_ONLY);

            try {
                blobClient.listContainersSegmented(name, ContainerListingDetails.NONE, 1,
                        resultSegment.getContinuationToken(), options, null);
            }
            catch (StorageException ex) {
                assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
            }
        }
        finally {
            for (String containerName : containerNames) {
                blobClient.getContainerReference(containerName).delete();
            }
        }
    }
}
