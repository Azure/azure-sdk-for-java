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
package com.microsoft.azure.storage.blob;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.NameValidator;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;
import com.microsoft.azure.storage.core.SR;

/**
 * Blob Container Tests
 */
@Category({ CloudTests.class })
public class CloudBlobContainerTests {

    protected static CloudBlobClient client;
    protected CloudBlobContainer container;

    @Before
    public void blobContainerTestMethodSetUp() throws StorageException, URISyntaxException {
        this.container = BlobTestHelper.getRandomContainerReference();
    }

    @After
    public void blobContainerTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }
    
    /**
     * Test container name validation.
     */
    @Test
    public void testCloudBlobContainerNameValidation()
    {
        NameValidator.validateContainerName("alpha");
        NameValidator.validateContainerName("4lphanum3r1c");
        NameValidator.validateContainerName("middle-dash");
        NameValidator.validateContainerName("$root");
        NameValidator.validateContainerName("$logs");

        invalidContainertTestHelper(null, "Null containers invalid.", "Invalid container name. The name may not be null, empty, or whitespace only.");
        invalidContainertTestHelper("$ROOT", "Root container case sensitive.", "Invalid container name. Check MSDN for more information about valid naming.");
        invalidContainertTestHelper("double--dash", "Double dashes not allowed.", "Invalid container name. Check MSDN for more information about valid naming.");
        invalidContainertTestHelper("-start-dash", "Start dashes not allowed.", "Invalid container name. Check MSDN for more information about valid naming.");
        invalidContainertTestHelper("CapsLock", "Lowercase only.", "Invalid container name. Check MSDN for more information about valid naming.");
        invalidContainertTestHelper("illegal$char", "Only alphanumeric and hyphen characters.", "Invalid container name. Check MSDN for more information about valid naming.");
        invalidContainertTestHelper("illegal!char", "Only alphanumeric and hyphen characters.", "Invalid container name. Check MSDN for more information about valid naming.");
        invalidContainertTestHelper("white space", "Only alphanumeric and hyphen characters.", "Invalid container name. Check MSDN for more information about valid naming.");
        invalidContainertTestHelper("2c", "Root container case sensitive.", "Invalid container name length. The name must be between 3 and 63 characters long.");
        invalidContainertTestHelper(new String(new char[64]).replace("\0", "n"), "Between 3 and 64 characters.", "Invalid container name length. The name must be between 3 and 63 characters long.");
    }

    private void invalidContainertTestHelper(String containerName, String failMessage, String exceptionMessage)
    {
        try
        {
            NameValidator.validateContainerName(containerName);
            fail(failMessage);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(exceptionMessage, e.getMessage());
        }
    }

    /**
     * Validate container references
     * 
     * @throws StorageException
     * @throws URISyntaxException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerReference() throws StorageException, URISyntaxException {
        CloudBlobClient client = BlobTestHelper.createCloudBlobClient();
        CloudBlobContainer container = client.getContainerReference("container");
        CloudBlockBlob blockBlob = container.getBlockBlobReference("directory1/blob1");
        CloudPageBlob pageBlob = container.getPageBlobReference("directory2/blob2");
        CloudBlobDirectory directory = container.getDirectoryReference("directory3");
        CloudBlobDirectory directory2 = directory.getDirectoryReference("directory4");

        assertEquals(container.getStorageUri().toString(), blockBlob.getContainer().getStorageUri().toString());
        assertEquals(container.getStorageUri().toString(), pageBlob.getContainer().getStorageUri().toString());
        assertEquals(container.getStorageUri().toString(), directory.getContainer().getStorageUri().toString());
        assertEquals(container.getStorageUri().toString(), directory2.getContainer().getStorageUri().toString());
        assertEquals(container.getStorageUri().toString(), directory2.getParent().getContainer().getStorageUri()
                .toString());
        assertEquals(container.getStorageUri().toString(), blockBlob.getParent().getContainer().getStorageUri()
                .toString());
        assertEquals(container.getStorageUri().toString(), blockBlob.getParent().getContainer().getStorageUri()
                .toString());
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerReferenceFromServer() throws StorageException, URISyntaxException, IOException {
        this.container.create();

        CloudBlob blob = BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, null, 1024, null);
        blob.getProperties().setContentType("application/octet-stream");
        blob.getProperties().setLength(1024);

        CloudBlob blobRef = this.container.getBlobReferenceFromServer(blob.getName());
        BlobTestHelper.assertAreEqual(blob, blobRef);

        blob = BlobTestHelper.uploadNewBlob(this.container, BlobType.PAGE_BLOB, null, 1024, null);
        blob.getProperties().setContentType("application/octet-stream");
        blob.getProperties().setLength(1024);

        blobRef = this.container.getBlobReferenceFromServer(blob.getName());
        BlobTestHelper.assertAreEqual(blob, blobRef);

        blob = BlobTestHelper.uploadNewBlob(this.container, BlobType.APPEND_BLOB, null, 1024, null);
        blob.getProperties().setContentType("application/octet-stream");
        blob.getProperties().setLength(1024);

        blobRef = this.container.getBlobReferenceFromServer(blob.getName());
        BlobTestHelper.assertAreEqual(blob, blobRef);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerReferenceFromServerSnapshot() throws StorageException, URISyntaxException,
            IOException {
        this.container.create();

        CloudBlob blob = BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, null, 1024, null);
        CloudBlob snapshot = blob.createSnapshot();
        snapshot.getProperties().setContentType("application/octet-stream");
        snapshot.getProperties().setLength(1024);

        CloudBlob blobRef = this.container.getBlobReferenceFromServer(snapshot.getName(), snapshot.getSnapshotID(),
                null, null, null);
        BlobTestHelper.assertAreEqual(snapshot, blobRef);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerReferenceFromServerSAS() throws StorageException, URISyntaxException,
            IOException, InvalidKeyException {
        this.container.create();
        CloudBlob blob = BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, null, 1024, null);

        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 10);
        policy.setSharedAccessExpiryTime(now.getTime());
        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
        String token = this.container.generateSharedAccessSignature(policy, null);

        CloudBlobContainer containerSAS = new CloudBlobContainer(this.container.getStorageUri(),
                new StorageCredentialsSharedAccessSignature(token));
        CloudBlob blobRef = containerSAS.getBlobReferenceFromServer(blob.getName());
        assertEquals(blob.getClass(), blobRef.getClass());
        assertEquals(blob.getUri(), blobRef.getUri());
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerReferenceFromServerMissingBlob() throws StorageException, URISyntaxException,
            IOException {
        this.container.create();

        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("missing");

        try {
            this.container.getBlobReferenceFromServer(blobName);
            fail("Get reference from server should fail.");
        } catch (StorageException ex) {
            assertEquals(404, ex.getHttpStatusCode());
        }
    }

    /**
     * Create a container
     * 
     * @throws StorageException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerCreate() throws StorageException {
        this.container.create();
        try {
            this.container.create();
            fail("Should not be able to create twice.");
        }
        catch (StorageException e) {
            assertEquals(e.getErrorCode(), "ContainerAlreadyExists");
            assertEquals(e.getHttpStatusCode(), 409);
            assertEquals(e.getMessage(), "The specified container already exists.");
        }
    }

    /**
     * Try to create a container after it is created
     * 
     * @throws StorageException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerCreateIfNotExists() throws StorageException {
        assertTrue(this.container.createIfNotExists());
        assertTrue(this.container.exists());
        assertFalse(this.container.createIfNotExists());
    }

    /**
     * Try to delete a non-existing container
     * 
     * @throws StorageException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerDeleteIfExists() throws StorageException {
        assertFalse(this.container.deleteIfExists());
        this.container.create();
        assertTrue(this.container.deleteIfExists());
        assertFalse(this.container.exists());
        assertFalse(this.container.deleteIfExists());
    }

    @Test
    public void testCloudBlobContainerDeleteIfExistsErrorCode() throws StorageException {
        try {
            this.container.delete();
            fail("Container should not already exist.");
        }
        catch (StorageException e) {
            assertEquals(StorageErrorCodeStrings.CONTAINER_NOT_FOUND, e.getErrorCode());
        }

        OperationContext ctx = new OperationContext();
        ctx.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                if (((HttpURLConnection) eventArg.getConnectionObject()).getRequestMethod().equals("DELETE")) {
                    try {
                        CloudBlobContainerTests.this.container.delete();
                        assertFalse(CloudBlobContainerTests.this.container.exists());
                    }
                    catch (StorageException e) {
                        fail("Delete should succeed.");
                    }
                }
            }
        });

        this.container.create();

        // Container deletes succeed before garbage collection occurs.
        assertTrue(this.container.deleteIfExists(null, null, ctx));
    }

    /**
     * Check a container's existence
     * 
     * @throws StorageException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerExists() throws StorageException {
        assertFalse(this.container.exists());

        this.container.create();
        assertTrue(this.container.exists());
        assertNotNull(this.container.getProperties().getEtag());

        this.container.delete();
        assertFalse(this.container.exists());
    }

    /**
     * Set and delete container permissions
     * 
     * @throws URISyntaxException
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    @Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerSetPermissions()
            throws StorageException, InterruptedException, URISyntaxException {
        CloudBlobClient client = BlobTestHelper.createCloudBlobClient();
        this.container.create();

        BlobContainerPermissions permissions = this.container.downloadPermissions();
        assertTrue(BlobContainerPublicAccessType.OFF.equals(permissions.getPublicAccess()));
        assertEquals(0, permissions.getSharedAccessPolicies().size());

        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        final Date start = cal.getTime();
        cal.add(Calendar.MINUTE, 30);
        final Date expiry = cal.getTime();

        permissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.CREATE));
        policy.setSharedAccessStartTime(start);
        policy.setSharedAccessExpiryTime(expiry);
        permissions.getSharedAccessPolicies().put("key1", policy);

        this.container.uploadPermissions(permissions);
        Thread.sleep(30000);
        // Check if permissions were set
        CloudBlobContainer container2 = client.getContainerReference(this.container.getName());
        assertPermissionsEqual(permissions, container2.downloadPermissions());

        // Clear permissions
        permissions.getSharedAccessPolicies().clear();
        this.container.uploadPermissions(permissions);
        Thread.sleep(30000);

        // Check if permissions were cleared
        // Public access should still be the same
        permissions = container2.downloadPermissions();
        assertPermissionsEqual(permissions, container2.downloadPermissions());
    }

    /**
     * Get permissions from string
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerPermissionsFromString() {
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();

        policy.setPermissionsFromString("racwdl");
        assertEquals(EnumSet.of(
                SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.ADD, SharedAccessBlobPermissions.CREATE,
                SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.DELETE, SharedAccessBlobPermissions.LIST),
                policy.getPermissions());

        policy.setPermissionsFromString("rawdl");
        assertEquals(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.ADD,
                SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.DELETE, SharedAccessBlobPermissions.LIST),
                policy.getPermissions());

        policy.setPermissionsFromString("rwdl");
        assertEquals(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE,
                SharedAccessBlobPermissions.DELETE, SharedAccessBlobPermissions.LIST), policy.getPermissions());

        policy.setPermissionsFromString("rwl");
        assertEquals(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE,
                SharedAccessBlobPermissions.LIST), policy.getPermissions());

        policy.setPermissionsFromString("wr");
        assertEquals(EnumSet.of(SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.READ),
                policy.getPermissions());

        policy.setPermissionsFromString("d");
        assertEquals(EnumSet.of(SharedAccessBlobPermissions.DELETE), policy.getPermissions());
    }

    /**
     * Write permission to string
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerPermissionsToString() {
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();

        policy.setPermissions(EnumSet.of(
                SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.ADD, SharedAccessBlobPermissions.CREATE,
                SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.DELETE, SharedAccessBlobPermissions.LIST));
        assertEquals("racwdl", policy.permissionsToString());

        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.ADD,
                SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.DELETE, SharedAccessBlobPermissions.LIST));
        assertEquals("rawdl", policy.permissionsToString());

        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE,
                SharedAccessBlobPermissions.DELETE, SharedAccessBlobPermissions.LIST));
        assertEquals("rwdl", policy.permissionsToString());

        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE,
                SharedAccessBlobPermissions.LIST));
        assertEquals("rwl", policy.permissionsToString());

        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.READ));
        assertEquals("rw", policy.permissionsToString());

        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.DELETE));
        assertEquals("d", policy.permissionsToString());
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerUploadMetadata() throws StorageException, URISyntaxException {
        this.container.create();

        CloudBlobContainer container2 = this.container.getServiceClient().getContainerReference(
                this.container.getName());
        container2.downloadAttributes();
        Assert.assertEquals(0, container2.getMetadata().size());

        this.container.getMetadata().put("key1", "value1");
        this.container.uploadMetadata();

        container2.downloadAttributes();
        Assert.assertEquals(1, container2.getMetadata().size());
        Assert.assertEquals("value1", container2.getMetadata().get("key1"));

        Iterable<CloudBlobContainer> containers = this.container.getServiceClient().listContainers(
                this.container.getName(), ContainerListingDetails.METADATA, null, null);

        for (CloudBlobContainer container3 : containers) {
            Assert.assertEquals(1, container3.getMetadata().size());
            Assert.assertEquals("value1", container3.getMetadata().get("key1"));
        }

        this.container.getMetadata().clear();
        this.container.uploadMetadata();

        container2.downloadAttributes();
        Assert.assertEquals(0, container2.getMetadata().size());
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerInvalidMetadata() throws StorageException {
        // test client-side fails correctly
        testMetadataFailures(this.container, null, "value1", true);
        testMetadataFailures(this.container, "", "value1", true);
        testMetadataFailures(this.container, " ", "value1", true);
        testMetadataFailures(this.container, "\n \t", "value1", true);

        testMetadataFailures(this.container, "key1", null, false);
        testMetadataFailures(this.container, "key1", "", false);
        testMetadataFailures(this.container, "key1", " ", false);
        testMetadataFailures(this.container, "key1", "\n \t", false);

        // test client can get empty metadata
        this.container.create();

        OperationContext opContext = new OperationContext();
        opContext.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {
            // insert a metadata element with an empty value
            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection request = (HttpURLConnection) eventArg.getConnectionObject();
                request.setRequestProperty(Constants.HeaderConstants.PREFIX_FOR_STORAGE_METADATA + "key1", "");
            }
        });
        this.container.uploadMetadata(null, null, opContext);

        this.container.downloadAttributes();
        assertEquals(1, this.container.getMetadata().size());
        assertEquals("", this.container.getMetadata().get("key1"));
    }

    private static void testMetadataFailures(CloudBlobContainer container, String key, String value, boolean badKey) {
        container.getMetadata().put(key, value);
        try {
            container.uploadMetadata();
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
        container.getMetadata().remove(key);
    }

    /**
     * List the blobs in a container
     * 
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerListBlobs() throws StorageException, IOException, URISyntaxException {
        this.container.create();
        int numBlobs = 200;
        List<String> blobNames = BlobTestHelper
                .uploadNewBlobs(this.container, BlobType.BLOCK_BLOB, numBlobs, 128, null);

        assertEquals(numBlobs, blobNames.size());

        int count = 0;
        for (ListBlobItem blob : this.container.listBlobs()) {
            assertEquals(CloudBlockBlob.class, blob.getClass());
            count++;
        }
        assertEquals(200, count);

        ResultContinuation token = null;

        do {
            ResultSegment<ListBlobItem> result = this.container.listBlobsSegmented("bb", false,
                    EnumSet.noneOf(BlobListingDetails.class), 150, token, null, null);
            for (ListBlobItem blob : result.getResults()) {
                assertEquals(CloudBlockBlob.class, blob.getClass());
                assertTrue(blobNames.remove(((CloudBlockBlob) blob).getName()));
            }
            token = result.getContinuationToken();
        } while (token != null);

        assertTrue(blobNames.size() == 0);
    }
    
    /**
     * List the blobs in a container with a prefix
     * 
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerListBlobsPrefix() throws StorageException, IOException, URISyntaxException {
        this.container.create();
        int numBlobs = 2;
        List<String> blobNames = BlobTestHelper
                .uploadNewBlobs(this.container, BlobType.BLOCK_BLOB, numBlobs, 128, null);

        BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, "pref/blob1", 128, null);
        blobNames.add("pref/blob1");
        
        BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, "pref/blob2", 128, null);
        blobNames.add("pref/blob2");

        // Flat listing false
        int count = 0;
        for (ListBlobItem blob : this.container.listBlobs("pref")) {
            assertEquals(CloudBlobDirectory.class, blob.getClass());
            assertTrue(((CloudBlobDirectory)blob).getPrefix().startsWith("pref"));
            count++;
        }
        assertEquals(1, count);
        
        // Flat listing true
        count = 0;
        for (ListBlobItem blob : this.container.listBlobs("pref", true)) {
            assertEquals(CloudBlockBlob.class, blob.getClass());
            assertTrue(((CloudBlockBlob)blob).getName().startsWith("pref/blob"));
            count++;
        }
        assertEquals(2, count);
    }
    
    /**
     * List the blobs in a container with next(). This tests for the item in the changelog: "Fixed a bug for all 
     * listing API's where next() would sometimes throw an exception if hasNext() had not been called even if 
     * there were more elements to iterate on."
     * 
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerListBlobsNext() throws StorageException, IOException, URISyntaxException {
        this.container.create();
        
        int numBlobs = 10;
        List<String> blobNames = BlobTestHelper.uploadNewBlobs(this.container, BlobType.PAGE_BLOB, 10, 512, null);
        assertEquals(numBlobs, blobNames.size());

        // hasNext first
        Iterator<ListBlobItem> iter = this.container.listBlobs().iterator();
        iter.hasNext();
        iter.next();
        iter.next();
        
        // next without hasNext
        iter = this.container.listBlobs().iterator();
        iter.next();
        iter.next();
    }
    
    /**
     * Try to list the blobs in a container to ensure maxResults validation is working.
     * 
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerListBlobsMaxResultsValidation()
            throws StorageException, IOException, URISyntaxException {
        this.container.create();

        // Validation should cause each of these to fail.
        for (int i = 0; i >= -2; i--) {
            try {
                this.container.listBlobsSegmented(
                        "bb", false, EnumSet.noneOf(BlobListingDetails.class), i, null, null, null);
                fail();
            }
            catch (IllegalArgumentException e) {
                assertTrue(String.format(SR.PARAMETER_SHOULD_BE_GREATER_OR_EQUAL, "maxResults", 1)
                        .equals(e.getMessage()));
            }
        }
        assertNotNull(this.container.listBlobsSegmented("thereshouldntbeanyblobswiththisprefix"));
    }

    /**
     * List the blobs in a container
     * 
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerListBlobsOptions() throws StorageException, IOException, InterruptedException,
            URISyntaxException {
        this.container.create();
        final int length = 128;

        // regular blob
        CloudBlockBlob originalBlob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container,
                BlobType.BLOCK_BLOB, "originalBlob", length, null);

        // leased blob
        CloudBlockBlob leasedBlob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB,
                "originalBlobLeased", length, null);
        leasedBlob.acquireLease();

        // copy of regular blob
        CloudBlockBlob copyBlob = this.container.getBlockBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("originalBlobCopy"));
        copyBlob.startCopy(originalBlob);
        BlobTestHelper.waitForCopy(copyBlob);

        // snapshot of regular blob
        CloudBlockBlob blobSnapshot = (CloudBlockBlob) originalBlob.createSnapshot();

        // snapshot of the copy of the regular blob
        CloudBlockBlob copySnapshot = this.container.getBlockBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("originalBlobSnapshotCopy"));
        copySnapshot.startCopy(copyBlob);
        BlobTestHelper.waitForCopy(copySnapshot);

        int count = 0;
        for (ListBlobItem item : this.container.listBlobs("originalBlob", true,
                EnumSet.allOf(BlobListingDetails.class), null, null)) {
            CloudBlockBlob blob = (CloudBlockBlob) item;
            if (blob.getName().equals(originalBlob.getName()) && !blob.isSnapshot()) {
                assertCreatedAndListedBlobsEquivalent(originalBlob, blob, length);
            }
            else if (blob.getName().equals(leasedBlob.getName())) {
                assertCreatedAndListedBlobsEquivalent(leasedBlob, blob, length);
            }
            else if (blob.getName().equals(copyBlob.getName())) {
                assertCreatedAndListedBlobsEquivalent(copyBlob, blob, length);
            }
            else if (blob.getName().equals(blobSnapshot.getName()) && blob.isSnapshot()) {
                assertCreatedAndListedBlobsEquivalent(blobSnapshot, blob, length);
            }
            else if (blob.getName().equals(copySnapshot.getName())) {
                assertCreatedAndListedBlobsEquivalent(copySnapshot, blob, length);
            }
            else {
                fail("An unexpected blob " + blob.getName() + " was listed.");
            }
            count++;
        }
        assertEquals(5, count);
    }

    /**
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlobContainerSharedKey() throws StorageException, InterruptedException {
        BlobContainerPermissions expectedPermissions;
        BlobContainerPermissions testPermissions;

        this.container.create();

        // Test new permissions.
        expectedPermissions = new BlobContainerPermissions();
        testPermissions = this.container.downloadPermissions();
        assertPermissionsEqual(expectedPermissions, testPermissions);

        // Test setting empty permissions.
        this.container.uploadPermissions(expectedPermissions);
        testPermissions = this.container.downloadPermissions();
        assertPermissionsEqual(expectedPermissions, testPermissions);

        // Add a policy, check setting and getting.
        SharedAccessBlobPolicy policy1 = new SharedAccessBlobPolicy();
        Calendar now = Calendar.getInstance();
        policy1.setSharedAccessStartTime(now.getTime());
        now.add(Calendar.MINUTE, 10);
        policy1.setSharedAccessExpiryTime(now.getTime());

        policy1.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.CREATE,
                SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.DELETE));
        expectedPermissions.getSharedAccessPolicies().put(UUID.randomUUID().toString(), policy1);

        this.container.uploadPermissions(expectedPermissions);
        Thread.sleep(30000);

        testPermissions = this.container.downloadPermissions();
        assertPermissionsEqual(expectedPermissions, testPermissions);
    }

    // Helper Method
    private static void assertPermissionsEqual(BlobContainerPermissions expected, BlobContainerPermissions actual) {
        assertEquals(expected.getPublicAccess(), actual.getPublicAccess());
        HashMap<String, SharedAccessBlobPolicy> expectedPolicies = expected.getSharedAccessPolicies();
        HashMap<String, SharedAccessBlobPolicy> actualPolicies = actual.getSharedAccessPolicies();
        assertEquals("SharedAccessPolicies.Count", expectedPolicies.size(), actualPolicies.size());
        for (String name : expectedPolicies.keySet()) {
            assertTrue("Key" + name + " doesn't exist", actualPolicies.containsKey(name));
            SharedAccessBlobPolicy expectedPolicy = expectedPolicies.get(name);
            SharedAccessBlobPolicy actualPolicy = actualPolicies.get(name);
            assertEquals("Policy: " + name + "\tPermissions\n", expectedPolicy.getPermissions().toString(),
                    actualPolicy.getPermissions().toString());
            assertEquals("Policy: " + name + "\tStartDate\n", expectedPolicy.getSharedAccessStartTime().toString(),
                    actualPolicy.getSharedAccessStartTime().toString());
            assertEquals("Policy: " + name + "\tExpireDate\n", expectedPolicy.getSharedAccessExpiryTime().toString(),
                    actualPolicy.getSharedAccessExpiryTime().toString());
        }
    }

    /**
     * Checks that a given created blob is listed correctly
     * 
     * @param createdBlob
     * @param listedBlob
     * @param length
     * @throws StorageException
     * @throws URISyntaxException
     */
    private static void assertCreatedAndListedBlobsEquivalent(CloudBlockBlob createdBlob, CloudBlockBlob listedBlob,
            int length) throws StorageException, URISyntaxException {
        assertEquals(createdBlob.getContainer().getName(), listedBlob.getContainer().getName());
        assertEquals(createdBlob.getMetadata(), listedBlob.getMetadata());
        assertEquals(createdBlob.getName(), listedBlob.getName());
        assertEquals(createdBlob.getQualifiedUri(), listedBlob.getQualifiedUri());
        assertEquals(createdBlob.getSnapshotID(), listedBlob.getSnapshotID());
        assertEquals(createdBlob.getUri(), listedBlob.getUri());

        // Compare Properties
        BlobProperties props1 = createdBlob.getProperties();
        BlobProperties props2 = listedBlob.getProperties();
        assertEquals(props1.getBlobType(), props2.getBlobType());
        assertEquals(props1.getContentDisposition(), props2.getContentDisposition());
        assertEquals(props1.getContentEncoding(), props2.getContentEncoding());
        assertEquals(props1.getContentLanguage(), props2.getContentLanguage());

        if (props1.getContentType() == null) {
            assertEquals("application/octet-stream", props2.getContentType());
        }
        else {
            assertEquals(props1.getContentType(), props2.getContentType());
        }

        if (props1.getContentMD5() != null) {
            assertEquals(props1.getContentMD5(), props2.getContentMD5());
        }
        assertEquals(props1.getEtag(), props2.getEtag());

        assertEquals(props1.getLeaseStatus(), props2.getLeaseStatus());

        if (props1.getLeaseState() != null) {
            assertEquals(props1.getLeaseState(), props2.getLeaseState());
        }

        assertEquals(length, props2.getLength());
        assertEquals(props1.getLastModified(), props2.getLastModified());
        assertEquals(props1.getCacheControl(), props2.getCacheControl());

        // Compare CopyState
        CopyState state1 = props1.getCopyState();
        CopyState state2 = props2.getCopyState();
        if (state1 == null && state2 == null) {
            return;
        }
        else {
            assertEquals(new Long(length), state2.getBytesCopied());
            assertNotNull(state2.getCompletionTime());
            assertEquals(state1.getCopyId(), state2.getCopyId());
            assertNotNull(state2.getSource());
            assertEquals(state1.getStatus(), state2.getStatus());
            assertEquals(new Long(length), state2.getTotalBytes());
        }
    }
}
