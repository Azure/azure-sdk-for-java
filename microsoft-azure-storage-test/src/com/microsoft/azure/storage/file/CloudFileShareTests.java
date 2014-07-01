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

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.core.SR;

/**
 * File Share Tests
 */
@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class CloudFileShareTests {

    protected static CloudFileClient client;
    protected CloudFileShare share;

    @Before
    public void fileShareTestMethodSetUp() throws StorageException, URISyntaxException {
        this.share = FileTestHelper.getRandomShareReference();
    }

    @After
    public void fileShareTestMethodTearDown() throws StorageException {
        this.share.deleteIfExists();
    }

    /**
     * Validate share references
     * 
     * @throws StorageException
     * @throws URISyntaxException
     */
    @Test
    public void testCloudFileShareReference() throws StorageException, URISyntaxException {
        CloudFileClient client = FileTestHelper.createCloudFileClient();
        CloudFileShare share = client.getShareReference("share");

        CloudFileDirectory directory = share.getRootDirectoryReference().getSubDirectoryReference("directory3");
        CloudFileDirectory directory2 = directory.getSubDirectoryReference("directory4");

        assertEquals(share.getStorageUri().toString(), directory.getShare().getStorageUri().toString());
        assertEquals(share.getStorageUri().toString(), directory2.getShare().getStorageUri().toString());
        assertEquals(share.getStorageUri().toString(), directory2.getParent().getShare().getStorageUri().toString());
    }

    /**
     * Try to create a share after it is created
     * 
     * @throws StorageException
     */
    @Test
    public void testCloudFileShareCreate() throws StorageException {
        this.share.create();
        assertTrue(this.share.exists());
        try {
            this.share.create();
            fail("Share already existed but was created anyway.");
        }
        catch (StorageException e) {
            assertEquals(e.getErrorCode(), "ShareAlreadyExists");
            assertEquals(e.getHttpStatusCode(), 409);
            assertEquals(e.getMessage(), "The specified share already exists.");
        }
    }

    /**
     * CreateIfNotExists test.
     * 
     * @throws StorageException
     */
    @Test
    public void testCloudFileShareCreateIfNotExists() throws StorageException {
        assertTrue(this.share.createIfNotExists());
        assertTrue(this.share.exists());
        assertFalse(this.share.createIfNotExists());
    }

    /**
     * DeleteIfExists test.
     * 
     * @throws StorageException
     */
    @Test
    public void testCloudFileShareDeleteIfExists() throws StorageException {
        assertFalse(this.share.deleteIfExists());
        this.share.create();
        assertTrue(this.share.deleteIfExists());
        assertFalse(this.share.exists());
        assertFalse(this.share.deleteIfExists());
    }

    /**
     * Check a share's existence
     * 
     * @throws StorageException
     */
    @Test
    public void testCloudFileShareExists() throws StorageException {
        assertFalse(this.share.exists());

        this.share.create();
        assertTrue(this.share.exists());
        assertNotNull(this.share.getProperties().getEtag());

        this.share.delete();
        assertFalse(this.share.exists());
    }

    /**
     * Check uploading/downloading share metadata.
     * 
     * @throws StorageException
     * @throws URISyntaxException
     */
    @Test
    public void testCloudFileShareUploadMetadata() throws StorageException, URISyntaxException {
        this.share.create();

        CloudFileShare share2 = this.share.getServiceClient().getShareReference(this.share.getName());
        share2.downloadAttributes();
        Assert.assertEquals(0, share2.getMetadata().size());

        this.share.getMetadata().put("key1", "value1");
        this.share.uploadMetadata();

        share2.downloadAttributes();
        Assert.assertEquals(1, share2.getMetadata().size());
        Assert.assertEquals("value1", share2.getMetadata().get("key1"));

        Iterable<CloudFileShare> shares = this.share.getServiceClient().listShares(this.share.getName(),
                ShareListingDetails.METADATA, null, null);

        for (CloudFileShare share3 : shares) {
            Assert.assertEquals(1, share3.getMetadata().size());
            Assert.assertEquals("value1", share3.getMetadata().get("key1"));
        }

        this.share.getMetadata().clear();
        this.share.uploadMetadata();

        share2.downloadAttributes();
        Assert.assertEquals(0, share2.getMetadata().size());
    }

    /**
     * Check uploading/downloading invalid share metadata.
     */
    @Test
    public void testCloudFileShareInvalidMetadata() {
        // test client-side fails correctly
        testMetadataFailures(this.share, null, "value1", true);
        testMetadataFailures(this.share, "", "value1", true);
        testMetadataFailures(this.share, " ", "value1", true);
        testMetadataFailures(this.share, "\n \t", "value1", true);

        testMetadataFailures(this.share, "key1", null, false);
        testMetadataFailures(this.share, "key1", "", false);
        testMetadataFailures(this.share, "key1", " ", false);
        testMetadataFailures(this.share, "key1", "\n \t", false);
    }

    private static void testMetadataFailures(CloudFileShare share, String key, String value, boolean badKey) {
        share.getMetadata().put(key, value);
        try {
            share.uploadMetadata();
            fail(SR.METADATA_KEY_INVALID);
        }
        catch (StorageException e) {
            if (badKey) {
                assertEquals(SR.METADATA_KEY_INVALID, e.getMessage());
            }
            else {
                assertEquals(SR.METADATA_VALUE_INVALID, e.getMessage());
            }
        }

        share.getMetadata().remove(key);
    }

    /**
     * Test specific deleteIfExists case.
     * 
     * @throws StorageException
     */
    @Test
    public void testCloudFileShareDeleteIfExistsErrorCode() throws StorageException {
        try {
            this.share.delete();
            fail("Share should not already exist.");
        }
        catch (StorageException e) {
            assertEquals(StorageErrorCodeStrings.SHARE_NOT_FOUND, e.getErrorCode());
        }

        OperationContext ctx = new OperationContext();
        ctx.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                if (((HttpURLConnection) eventArg.getConnectionObject()).getRequestMethod().equals("DELETE")) {
                    try {
                        CloudFileShareTests.this.share.delete();
                        assertFalse(CloudFileShareTests.this.share.exists());
                    }
                    catch (StorageException e) {
                        fail("Delete should succeed.");
                    }
                }
            }
        });

        this.share.create();

        // Container deletes succeed before garbage collection occurs.
        assertTrue(this.share.deleteIfExists(null, null, ctx));
    }
}
