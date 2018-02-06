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
package com.microsoft.azure.storage.file;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * File Client Tests
 */
public class CloudFileClientTests {
    /**
     * Tests doing a listShares.
     * 
     * @throws StorageException
     * @throws URISyntaxException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListSharesTest() throws StorageException, URISyntaxException {
        CloudFileClient fileClient = FileTestHelper.createCloudFileClient();
        ArrayList<String> shareList = new ArrayList<String>();
        String prefix = UUID.randomUUID().toString();
        try {
            for (int i = 0; i < 30; i++) {
                shareList.add(prefix + i);
                fileClient.getShareReference(prefix + i).create();
            }

            int count = 0;
            for (final CloudFileShare share : fileClient.listShares(prefix)) {
                assertEquals(CloudFileShare.class, share.getClass());
                count++;
            }
            assertEquals(30, count);

            ResultContinuation token = null;
            do {

                ResultSegment<CloudFileShare> segment = fileClient.listSharesSegmented(prefix,
                        EnumSet.allOf(ShareListingDetails.class), 15, token, null, null);

                for (final CloudFileShare share : segment.getResults()) {
                    share.downloadAttributes();
                    assertEquals(CloudFileShare.class, share.getClass());
                    shareList.remove(share.getName());
                    share.delete();
                }

                token = segment.getContinuationToken();
            } while (token != null);

            assertEquals(0, shareList.size());
        }
        finally {
            for (final String shareName : shareList) {
                fileClient.getShareReference(shareName).deleteIfExists();
            }
        }
    }
    
    /**
     * Tests doing a listShares to ensure maxResults validation is working.
     * 
     * @throws StorageException
     * @throws URISyntaxException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListSharesMaxResultsValidationTest() throws StorageException, URISyntaxException {
        CloudFileClient fileClient = FileTestHelper.createCloudFileClient();
        String prefix = UUID.randomUUID().toString();
            
        // Validation should cause each of these to fail
        for (int i = 0; i >= -2; i--) {
            try{ 
                fileClient.listSharesSegmented(
                        prefix, EnumSet.allOf(ShareListingDetails.class), i, null, null, null);
                fail();
            }
            catch (IllegalArgumentException e) {
                assertTrue(String.format(SR.PARAMETER_SHOULD_BE_GREATER_OR_EQUAL, "maxResults", 1)
                        .equals(e.getMessage()));
            }
        }
        assertNotNull(fileClient.listSharesSegmented("thereshouldntbeanyshareswiththisprefix"));
    }

    @Test
    public void testListSharesWithSnapshot() throws StorageException, URISyntaxException {
        CloudFileClient fileClient = FileTestHelper.createCloudFileClient();
        CloudFileShare share = fileClient.getShareReference(UUID.randomUUID().toString());
        try {
            share.create();

            HashMap<String, String> shareMeta = new HashMap<String, String>();
            shareMeta.put("key1", "value1");
            share.setMetadata(shareMeta);
            share.uploadMetadata();

            CloudFileShare snapshot = share.createSnapshot();
            HashMap<String, String> meta2 = new HashMap<String, String>();
            meta2.put("key2", "value2");
            share.setMetadata(meta2);
            share.uploadMetadata();

            CloudFileClient client = FileTestHelper.createCloudFileClient();
            Iterable<CloudFileShare> listResult = client.listShares(share.name, EnumSet.allOf(ShareListingDetails.class), null, null);

            int count = 0;
            boolean originalFound = false;
            boolean snapshotFound = false;
            for (CloudFileShare listShareItem : listResult) {
                if (listShareItem.getName().equals(share.getName()) && !listShareItem.isSnapshot() && !originalFound) {
                    count++;
                    originalFound = true;
                    assertEquals(share.getMetadata(), listShareItem.getMetadata());
                    assertEquals(share.getStorageUri(), listShareItem.getStorageUri());
                } else if (listShareItem.getName().equals(share.getName()) &&
                        listShareItem.isSnapshot() && !snapshotFound) {
                    count++;
                    snapshotFound = true;
                    assertEquals(snapshot.getMetadata(), listShareItem.getMetadata());
                    assertEquals(snapshot.getStorageUri(), listShareItem.getStorageUri());
                }
            }

            assertEquals(2, count);
        }
        finally
        {
            share.deleteIfExists(DeleteShareSnapshotsOption.INCLUDE_SNAPSHOTS, null, null, null);
        }
    }
}