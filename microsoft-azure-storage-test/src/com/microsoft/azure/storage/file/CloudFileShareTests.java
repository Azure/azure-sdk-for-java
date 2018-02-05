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
import com.microsoft.azure.storage.NameValidator;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;
import com.microsoft.azure.storage.core.UriQueryBuilder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * File Share Tests
 */
@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class CloudFileShareTests {

    protected CloudFileShare share;

    @Before
    public void fileShareTestMethodSetUp() throws StorageException, URISyntaxException {
        this.share = FileTestHelper.getRandomShareReference();
    }

    @After
    public void fileShareTestMethodTearDown() throws StorageException {
        this.share.deleteIfExists(DeleteShareSnapshotsOption.INCLUDE_SNAPSHOTS, null, null, null);
    }
    
    /**
     * Test share name validation.
     */
    @Test
    public void testCloudShareNameValidation()
    {
        NameValidator.validateShareName("alpha");
        NameValidator.validateShareName("4lphanum3r1c");
        NameValidator.validateShareName("middle-dash");

        invalidShareTestHelper(null, "Null not allowed.", "Invalid share name. The name may not be null, empty, or whitespace only.");
        invalidShareTestHelper("$root", "Alphanumeric or dashes only.", "Invalid share name. Check MSDN for more information about valid naming.");
        invalidShareTestHelper("double--dash", "No double dash.", "Invalid share name. Check MSDN for more information about valid naming.");
        invalidShareTestHelper("CapsLock", "Lowercase only.", "Invalid share name. Check MSDN for more information about valid naming.");
        invalidShareTestHelper("illegal$char", "Alphanumeric or dashes only.", "Invalid share name. Check MSDN for more information about valid naming.");
        invalidShareTestHelper("illegal!char", "Alphanumeric or dashes only.", "Invalid share name. Check MSDN for more information about valid naming.");
        invalidShareTestHelper("white space", "Alphanumeric or dashes only.", "Invalid share name. Check MSDN for more information about valid naming.");
        invalidShareTestHelper("2c", "Between 3 and 63 characters.", "Invalid share name length. The name must be between 3 and 63 characters long.");
        invalidShareTestHelper(new String(new char[64]).replace("\0", "n"), "Between 3 and 63 characters.", "Invalid share name length. The name must be between 3 and 63 characters long.");
    }

    private void invalidShareTestHelper(String shareName, String failMessage, String exceptionMessage)
    {
        try
        {
            NameValidator.validateShareName(shareName);
            fail(failMessage);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(exceptionMessage, e.getMessage());
        }
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

        CloudFileDirectory directory = share.getRootDirectoryReference().getDirectoryReference("directory3");
        CloudFileDirectory directory2 = directory.getDirectoryReference("directory4");

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
     * Set and delete share permissions
     * 
     * @throws URISyntaxException
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    @Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class })
    public void testCloudFileShareSetPermissions()
            throws StorageException, InterruptedException, URISyntaxException {
        CloudFileClient client = FileTestHelper.createCloudFileClient();
        this.share.create();

        FileSharePermissions permissions = this.share.downloadPermissions();
        assertEquals(0, permissions.getSharedAccessPolicies().size());

        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        final Date start = cal.getTime();
        cal.add(Calendar.MINUTE, 30);
        final Date expiry = cal.getTime();

        SharedAccessFilePolicy policy = new SharedAccessFilePolicy();
        policy.setPermissions(EnumSet.of(SharedAccessFilePermissions.LIST, SharedAccessFilePermissions.CREATE));
        policy.setSharedAccessStartTime(start);
        policy.setSharedAccessExpiryTime(expiry);
        permissions.getSharedAccessPolicies().put("key1", policy);

        // Set permissions and wait for them to propagate
        this.share.uploadPermissions(permissions);
        Thread.sleep(30000);
        
        // Check if permissions were set
        CloudFileShare share2 = client.getShareReference(this.share.getName());
        assertPermissionsEqual(permissions, share2.downloadPermissions());

        // Clear permissions and wait for them to propagate
        permissions.getSharedAccessPolicies().clear();
        this.share.uploadPermissions(permissions);
        Thread.sleep(30000);

        // Check if permissions were cleared
        assertPermissionsEqual(permissions, share2.downloadPermissions());
    }

    /**
     * Get permissions from string
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudFileSharePermissionsFromString() {
        SharedAccessFilePolicy policy = new SharedAccessFilePolicy();

        policy.setPermissionsFromString("rcwdl");
        assertEquals(EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.CREATE,
                SharedAccessFilePermissions.WRITE, SharedAccessFilePermissions.DELETE, SharedAccessFilePermissions.LIST),
                policy.getPermissions());

        policy.setPermissionsFromString("rwdl");
        assertEquals(EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE,
                SharedAccessFilePermissions.DELETE, SharedAccessFilePermissions.LIST), policy.getPermissions());

        policy.setPermissionsFromString("rwl");
        assertEquals(EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE,
                SharedAccessFilePermissions.LIST), policy.getPermissions());

        policy.setPermissionsFromString("wr");
        assertEquals(EnumSet.of(SharedAccessFilePermissions.WRITE, SharedAccessFilePermissions.READ),
                policy.getPermissions());

        policy.setPermissionsFromString("d");
        assertEquals(EnumSet.of(SharedAccessFilePermissions.DELETE), policy.getPermissions());
    }

    /**
     * Write permission to string
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudFileSharePermissionsToString() {
        SharedAccessFilePolicy policy = new SharedAccessFilePolicy();

        policy.setPermissions(EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.CREATE,
                SharedAccessFilePermissions.WRITE, SharedAccessFilePermissions.DELETE, SharedAccessFilePermissions.LIST));
        assertEquals("rcwdl", policy.permissionsToString());

        policy.setPermissions(EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE,
                SharedAccessFilePermissions.DELETE, SharedAccessFilePermissions.LIST));
        assertEquals("rwdl", policy.permissionsToString());

        policy.setPermissions(EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE,
                SharedAccessFilePermissions.LIST));
        assertEquals("rwl", policy.permissionsToString());

        policy.setPermissions(EnumSet.of(SharedAccessFilePermissions.WRITE, SharedAccessFilePermissions.READ));
        assertEquals("rw", policy.permissionsToString());

        policy.setPermissions(EnumSet.of(SharedAccessFilePermissions.DELETE));
        assertEquals("d", policy.permissionsToString());
    }

    /**
     * Check uploading/downloading share metadata.
     * 
     * @throws StorageException
     * @throws URISyntaxException
     */
    @Test
    public void testCloudFileShareUploadMetadata() throws StorageException, URISyntaxException {
        this.share.getMetadata().put("key1", "value1");
        this.share.create();
        Assert.assertEquals(1, this.share.getMetadata().size());
        Assert.assertEquals("value1", this.share.getMetadata().get("key1"));

        CloudFileShare share2 = this.share.getServiceClient().getShareReference(this.share.getName());
        share2.downloadAttributes();
        Assert.assertEquals(1, share2.getMetadata().size());
        Assert.assertEquals("value1", share2.getMetadata().get("key1"));

        this.share.getMetadata().put("key2", "value2");

        Assert.assertEquals(2, this.share.getMetadata().size());
        Assert.assertEquals("value1", this.share.getMetadata().get("key1"));
        Assert.assertEquals("value2", this.share.getMetadata().get("key2"));
        this.share.uploadMetadata();

        Assert.assertEquals(2, this.share.getMetadata().size());
        Assert.assertEquals("value1", this.share.getMetadata().get("key1"));
        Assert.assertEquals("value2", this.share.getMetadata().get("key2"));

        share2.downloadAttributes();

        Assert.assertEquals(2, this.share.getMetadata().size());
        Assert.assertEquals("value1", this.share.getMetadata().get("key1"));
        Assert.assertEquals("value2", this.share.getMetadata().get("key2"));

        Iterable<CloudFileShare> shares = this.share.getServiceClient().listShares(this.share.getName(),
                EnumSet.of(ShareListingDetails.METADATA), null, null);

        for (CloudFileShare share3 : shares) {
            Assert.assertEquals(2, share3.getMetadata().size());
            Assert.assertEquals("value1", share3.getMetadata().get("key1"));
            Assert.assertEquals("value2", this.share.getMetadata().get("key2"));
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
     * Tests whether Share Stats can be updated and downloaded.
     * 
     * @throws StorageException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    @Category({ CloudTests.class })
    public void testGetShareStats() throws StorageException, IOException, URISyntaxException {
        share.createIfNotExists();
        ShareStats stats = share.getStats();
        Assert.assertNotNull(stats);
        Assert.assertEquals(0, stats.getUsage());

        FileTestHelper.uploadNewFile(share, 512, null);

        stats = share.getStats();
        Assert.assertNotNull(stats);
        Assert.assertEquals(1, stats.getUsage());
    }

    /**
     * Test that Share Quota can be set, but only to allowable values.
     * 
     * @throws StorageException 
     * @throws URISyntaxException 
     */
    @Test
    public void testCloudFileShareQuota() throws StorageException, URISyntaxException {
        // Share quota defaults to 5120
        this.share.createIfNotExists();
        this.share.downloadAttributes();
        assertNotNull(this.share.getProperties().getShareQuota());
        int shareQuota = FileConstants.MAX_SHARE_QUOTA;
        assertEquals(shareQuota, this.share.getProperties().getShareQuota().intValue());

        // Upload new share quota
        shareQuota = 8;
        this.share.getProperties().setShareQuota(shareQuota);
        this.share.uploadProperties();
        this.share.downloadAttributes();
        assertNotNull(this.share.getProperties().getShareQuota());
        assertEquals(shareQuota, this.share.getProperties().getShareQuota().intValue());
        this.share.delete();

        // Create a share with quota already set
        shareQuota = 16;
        this.share = FileTestHelper.getRandomShareReference();
        this.share.getProperties().setShareQuota(shareQuota);
        this.share.create();
        assertNotNull(this.share.getProperties().getShareQuota());
        assertEquals(shareQuota, this.share.getProperties().getShareQuota().intValue());
        this.share.downloadAttributes();
        assertNotNull(this.share.getProperties().getShareQuota());
        assertEquals(shareQuota, this.share.getProperties().getShareQuota().intValue());

        // Attempt to set illegal share quota
        try {
            shareQuota = FileConstants.MAX_SHARE_QUOTA + 1;
            this.share.getProperties().setShareQuota(shareQuota);
            this.share.uploadProperties();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(String.format(SR.PARAMETER_NOT_IN_RANGE, "Share Quota", 1, FileConstants.MAX_SHARE_QUOTA),
                    e.getMessage());
        }
    }

    /**
     * Test that Share Quota can be set, but only to allowable values.
     * 
     * @throws StorageException 
     * @throws URISyntaxException 
     */
    @Test
    public void testCloudFileShareQuotaListing() throws StorageException, URISyntaxException {
        int shareQuota = 16;
        this.share.getProperties().setShareQuota(shareQuota);
        this.share.createIfNotExists();

        Iterable<CloudFileShare> shares = this.share.getServiceClient().listShares(this.share.getName());

        for (CloudFileShare fileShare : shares) {
            assertEquals(shareQuota, fileShare.getProperties().getShareQuota().intValue());
        }
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

        // Share deletes succeed before garbage collection occurs.
        assertTrue(this.share.deleteIfExists(null, null, ctx));
    }

    @Test
    public void testCreateShareSnapshot() throws StorageException, URISyntaxException, IOException {
        // create share with metadata
        this.share.create();
        assertTrue(this.share.exists());
        HashMap<String, String> shareMeta = new HashMap<String, String>();
        shareMeta.put("key1", "value1");
        this.share.setMetadata(shareMeta);
        this.share.uploadMetadata();

        CloudFileDirectory dir1 = this.share.getRootDirectoryReference().getDirectoryReference("dir1");
        dir1.create();
        CloudFile file1 = dir1.getFileReference("file1");
        file1.create(1024);
        ByteArrayInputStream srcStream = FileTestHelper.getRandomDataStream(1024);
        file1.upload(srcStream, 1024);

        // create directory with metadata
        HashMap<String, String> dirMeta = new HashMap<String, String>();
        dirMeta.put("key2", "value2");
        dir1.setMetadata(dirMeta);
        dir1.uploadMetadata();

        // verify that exists() call on snapshot populates metadata
        CloudFileShare snapshot = this.share.createSnapshot();
        CloudFileClient client = FileTestHelper.createCloudFileClient();
        CloudFileShare snapshotRef = client.getShareReference(snapshot.name, snapshot.snapshotID);
        assertTrue(snapshotRef.exists());
        assertTrue(snapshotRef.getMetadata().size() == 1 && snapshotRef.getMetadata().get("key1").equals("value1"));

        // verify that downloadAttributes() populates metadata
        CloudFileShare snapshotRef2 = client.getShareReference(snapshot.name, snapshot.snapshotID);
        snapshotRef2.downloadAttributes();
        snapshot.downloadAttributes();
        assertTrue(snapshotRef2.getMetadata().size() == 1 && snapshotRef2.getMetadata().get("key1").equals("value1"));
        assertTrue(snapshot.getMetadata().size() == 1 && snapshot.getMetadata().get("key1").equals("value1"));

        // verify that exists() populates the metadata
        CloudFileDirectory snapshotDir1 = snapshot.getRootDirectoryReference().getDirectoryReference("dir1");
        snapshotDir1.exists();
        assertTrue(snapshotDir1.getMetadata().size() == 1 && snapshotDir1.getMetadata().get("key2").equals("value2"));

        // verify that downloadAttributes() populates the metadata
        CloudFileDirectory snapshotDir2 = snapshot.getRootDirectoryReference().getDirectoryReference("dir1");
        snapshotDir2.downloadAttributes();
        assertTrue(snapshotDir2.getMetadata().size() == 1 && snapshotDir2.getMetadata().get("key2").equals("value2"));

        // create snapshot with metadata
        HashMap<String, String> shareMeta2 = new HashMap<String, String>();
        shareMeta2.put("abc", "def");
        CloudFileShare snapshotRef3 = this.share.createSnapshot(shareMeta2, null, null, null);
        CloudFileShare snapshotRef4 = client.getShareReference(snapshotRef3.name, snapshotRef3.snapshotID);
        assertTrue(snapshotRef4.exists());
        assertTrue(snapshotRef4.getMetadata().size() == 1 && snapshotRef4.getMetadata().get("abc").equals("def"));

        final UriQueryBuilder uriBuilder = new UriQueryBuilder();
        uriBuilder.add("sharesnapshot", snapshot.snapshotID);
        CloudFileShare snapshotRef5 = new CloudFileShare(uriBuilder.addToURI(this.share.getUri()),
                                                         this.share.getServiceClient().getCredentials());
        assertEquals(snapshot.snapshotID, snapshotRef5.snapshotID);
        assertTrue(snapshotRef5.exists());

        snapshot.delete();
    }
    
    @Test
    public void testDeleteShareSnapshotOptions() throws StorageException, URISyntaxException, IOException {
        // create share with metadata
        this.share.create();
        assertTrue(this.share.exists());

        // verify that exists() call on snapshot populates metadata
        CloudFileShare snapshot = this.share.createSnapshot();
        CloudFileClient client = FileTestHelper.createCloudFileClient();
        CloudFileShare snapshotRef = client.getShareReference(snapshot.name, snapshot.snapshotID);
        assertTrue(snapshotRef.exists());

        try {
            share.delete();
        }
        catch (final StorageException e) {
            assertEquals(StorageErrorCodeStrings.SHARE_HAS_SNAPSHOTS, e.getErrorCode());
        }
        
        share.delete(DeleteShareSnapshotsOption.INCLUDE_SNAPSHOTS, null, null, null);
        assertFalse(share.exists());
        assertFalse(snapshot.exists());
    }

    @Test
    public void testListFilesAndDirectoriesWithinShareSnapshot() throws StorageException, URISyntaxException {
        this.share.create();

        CloudFileDirectory myDir = this.share.getRootDirectoryReference().getDirectoryReference("mydir");
        myDir.create();
        myDir.getFileReference("myfile").create(1024);
        myDir.getDirectoryReference("yourDir").create();
        assertTrue(this.share.exists());

        CloudFileShare snapshot = this.share.createSnapshot();
        CloudFileClient client = FileTestHelper.createCloudFileClient();
        CloudFileShare snapshotRef = client.getShareReference(snapshot.name, snapshot.snapshotID);

        Iterable<ListFileItem> listResult = snapshotRef.getRootDirectoryReference().listFilesAndDirectories();
        int count = 0;
        for (ListFileItem listFileItem : listResult) {
            count++;
            assertEquals("mydir", ((CloudFileDirectory) listFileItem).getName());
        }

        assertEquals(1, count);

        count = 0;
        listResult = snapshotRef.getRootDirectoryReference().getDirectoryReference("mydir").listFilesAndDirectories();
        for (ListFileItem listFileItem : listResult) {
            if (listFileItem instanceof CloudFileDirectory) {
                count++;
                assertEquals("yourDir", ((CloudFileDirectory) listFileItem).getName());
            }
            else {
                count++;
                assertEquals("myfile", ((CloudFile) listFileItem).getName());
            }
        }

        assertEquals(2, count);

        snapshot.delete();
    }

    @Test
    public void testUnsupportedApisShareSnapshot() throws StorageException, URISyntaxException {
        this.share.create();
        this.share.downloadPermissions();
        CloudFileShare snapshot = this.share.createSnapshot();
        try {
            snapshot.createSnapshot();
            fail("Shouldn't get here");
        }
        catch (IllegalArgumentException e) {
            assertEquals(SR.INVALID_OPERATION_FOR_A_SHARE_SNAPSHOT, e.getMessage());
        }
        try {
            snapshot.downloadPermissions();
            fail("Shouldn't get here");
        }
        catch (IllegalArgumentException e) {
            assertEquals(SR.INVALID_OPERATION_FOR_A_SHARE_SNAPSHOT, e.getMessage());
        }
        try {
            snapshot.getStats();
            fail("Shouldn't get here");
        }
        catch (IllegalArgumentException e) {
            assertEquals(SR.INVALID_OPERATION_FOR_A_SHARE_SNAPSHOT, e.getMessage());
        }
        try {
            snapshot.uploadMetadata();
            fail("Shouldn't get here");
        }
        catch (IllegalArgumentException e) {
            assertEquals(SR.INVALID_OPERATION_FOR_A_SHARE_SNAPSHOT, e.getMessage());
        }
        try {
            FileSharePermissions permissions = new FileSharePermissions();
            snapshot.uploadPermissions(permissions);
            fail("Shouldn't get here");
        }
        catch (IllegalArgumentException e) {
            assertEquals(SR.INVALID_OPERATION_FOR_A_SHARE_SNAPSHOT, e.getMessage());
        }
        try {
            snapshot.uploadProperties();
            fail("Shouldn't get here");
        }
        catch (IllegalArgumentException e) {
            assertEquals(SR.INVALID_OPERATION_FOR_A_SHARE_SNAPSHOT, e.getMessage());
        }

        snapshot.delete();
    }

    private static void assertPermissionsEqual(FileSharePermissions expected, FileSharePermissions actual) {
        HashMap<String, SharedAccessFilePolicy> expectedPolicies = expected.getSharedAccessPolicies();
        HashMap<String, SharedAccessFilePolicy> actualPolicies = actual.getSharedAccessPolicies();
        assertEquals("SharedAccessPolicies.Count", expectedPolicies.size(), actualPolicies.size());
        for (String name : expectedPolicies.keySet()) {
            assertTrue("Key" + name + " doesn't exist", actualPolicies.containsKey(name));
            SharedAccessFilePolicy expectedPolicy = expectedPolicies.get(name);
            SharedAccessFilePolicy actualPolicy = actualPolicies.get(name);
            assertEquals("Policy: " + name + "\tPermissions\n", expectedPolicy.getPermissions().toString(),
                    actualPolicy.getPermissions().toString());
            assertEquals("Policy: " + name + "\tStartDate\n", expectedPolicy.getSharedAccessStartTime().toString(),
                    actualPolicy.getSharedAccessStartTime().toString());
            assertEquals("Policy: " + name + "\tExpireDate\n", expectedPolicy.getSharedAccessExpiryTime().toString(),
                    actualPolicy.getSharedAccessExpiryTime().toString());
        }
    }
}
