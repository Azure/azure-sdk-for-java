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

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.NameValidator;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageCredentialsAnonymous;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.FileProperties;
import com.microsoft.azure.storage.file.FileTestHelper;
import com.microsoft.azure.storage.file.SharedAccessFilePermissions;
import com.microsoft.azure.storage.file.SharedAccessFilePolicy;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.NameValidator;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageCredentialsAnonymous;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.FileProperties;
import com.microsoft.azure.storage.file.FileTestHelper;
import com.microsoft.azure.storage.file.SharedAccessFilePermissions;
import com.microsoft.azure.storage.file.SharedAccessFilePolicy;

import static org.junit.Assert.*;

/**
 * Block Blob Tests
 */
@Category(CloudTests.class)
public class CloudBlockBlobTests {

    protected CloudBlobContainer container;

    @Before
    public void blobEncryptionTestMethodSetUp() throws URISyntaxException, StorageException {
        this.container = BlobTestHelper.getRandomContainerReference();
        this.container.create();
    }

    @After
    public void blobEncryptionTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }

    /**
     * Create a block blob.
     *
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlockBlobCreate() throws StorageException, URISyntaxException, IOException {
        final CloudBlockBlob blob = this.container.getBlockBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("testBlob"));

        assertFalse(blob.exists());

        // Create
        blob.uploadText("text");
        assertTrue(blob.exists());

        // Create again (should succeed)
        blob.uploadText("text");
        assertTrue(blob.exists());

        // Create again, specifying not to if it already exists
        // This should fail
        // Add 15 min to account for clock skew
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 15);
        AccessCondition condition = new AccessCondition();
        condition.setIfModifiedSinceDate(cal.getTime());

        try {
            blob.uploadText("text", null, condition, null, null);
            fail("Create should fail do to access condition.");
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getHttpStatusCode());
            assertEquals("The condition specified using HTTP conditional header(s) is not met.", ex.getMessage());
            assertEquals("ConditionNotMet", ex.getErrorCode());
        }
    }

    /**
     * Delete a block blob.
     *
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlockBlobDelete() throws StorageException, URISyntaxException, IOException {
        final CloudBlockBlob blob = this.container.getBlockBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("testBlob"));

        assertFalse(blob.exists());

        // create
        blob.uploadText("text");
        assertTrue(blob.exists());

        // delete
        blob.delete();
        assertFalse(blob.exists());

        // delete again, should fail as it doesn't exist
        try {
            blob.delete();
            fail("Delete should fail as blob does not exist.");
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            assertEquals("The specified blob does not exist.", ex.getMessage());
            assertEquals("BlobNotFound", ex.getErrorCode());
        }
    }

    /**
     * Test blob name validation.
     */
    @Test
    public void testCloudBlobNameValidation()
    {
        NameValidator.validateBlobName("alpha");
        NameValidator.validateBlobName("4lphanum3r1c");
        NameValidator.validateBlobName("CAPSLOCK");
        NameValidator.validateBlobName("white space");
        NameValidator.validateBlobName("0th3r(h@racter$");
        NameValidator.validateBlobName(new String(new char[253]).replace("\0", "a/a"));

        invalidBlobTestHelper("", "No empty strings.", "Invalid blob name. The name may not be null, empty, or whitespace only.");
        invalidBlobTestHelper(null, "No null strings.", "Invalid blob name. The name may not be null, empty, or whitespace only.");
        invalidBlobTestHelper(new String(new char[1025]).replace("\0", "n"), "Maximum 1024 characters.", "Invalid blob name length. The name must be between 1 and 1024 characters long.");
        invalidBlobTestHelper(new String(new char[254]).replace("\0", "a/a"), "Maximum 254 path segments.", "The count of URL path segments (strings between '/' characters) as part of the blob name cannot exceed 254.");
    }

    private void invalidBlobTestHelper(String blobName, String failMessage, String exceptionMessage)
    {
        try
        {
            NameValidator.validateBlobName(blobName);
            fail(failMessage);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test
    public void testBlobUriOnlyConstructors() throws URISyntaxException, StorageException, InvalidKeyException {
        URI blobURI = new URI(this.container.getUri().toString() + "/anonblob");
        CloudBlockBlob blob = new CloudBlockBlob(blobURI);
        assertEquals("anonblob", blob.getName());
        assertNotNull(blob.getServiceClient());
        assertEquals(StorageCredentialsAnonymous.class, blob.getServiceClient().getCredentials().getClass());

        blob = this.container.getBlockBlobReference("anonblob");

        String sas = blob.generateSharedAccessSignature(null, "dummyPolicy");
        blobURI = new URI(this.container.getUri().toString() + "/anonblob?" + sas);
        blob = new CloudBlockBlob(blobURI);
        assertEquals("anonblob", blob.getName());
        assertNotNull(blob.getServiceClient());
        assertEquals(StorageCredentialsSharedAccessSignature.class, blob.getServiceClient().getCredentials().getClass());
    }

    @Test
    @Category(SlowTests.class)
    public void testCopyBlockBlobSasToSasTest() throws InvalidKeyException, URISyntaxException, StorageException,
            IOException, InterruptedException {
        this.doCloudBlockBlobCopy(true, true);
    }

    @Test
    @Category(SlowTests.class)
    public void testCopyBlockBlobToSasTest() throws InvalidKeyException, URISyntaxException, StorageException,
            IOException, InterruptedException {
        this.doCloudBlockBlobCopy(false, true);
    }

    @Test
    @Category(SlowTests.class)
    public void testCopyBlockBlobSasTest() throws InvalidKeyException, URISyntaxException, StorageException,
            IOException, InterruptedException {
        this.doCloudBlockBlobCopy(true, false);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SlowTests.class })
    public void testCopyBlockBlobTest() throws InvalidKeyException, URISyntaxException, StorageException, IOException,
            InterruptedException {
        this.doCloudBlockBlobCopy(false, false);
    }

    @Test
    public void testCopyWithChineseChars() throws StorageException, IOException, URISyntaxException {
        String data = "sample data chinese chars 阿䶵";
        CloudBlockBlob copySource = container.getBlockBlobReference("sourcechinescharsblob阿䶵.txt");
        copySource.uploadText(data);

        assertEquals(this.container.getUri() + "/sourcechinescharsblob阿䶵.txt", copySource.getUri().toString());
        assertEquals(this.container.getUri() + "/sourcechinescharsblob%E9%98%BF%E4%B6%B5.txt",
                copySource.getUri().toASCIIString());

        CloudBlockBlob copyDestination = container.getBlockBlobReference("destchinesecharsblob阿䶵.txt");

        assertEquals(this.container.getUri() + "/destchinesecharsblob阿䶵.txt", copyDestination.getUri().toString());
        assertEquals(this.container.getUri() + "/destchinesecharsblob%E9%98%BF%E4%B6%B5.txt",
                copyDestination.getUri().toASCIIString());

        OperationContext ctx = new OperationContext();
        ctx.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {
            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection con = (HttpURLConnection) eventArg.getConnectionObject();

                // Test the copy destination request url
                assertEquals(CloudBlockBlobTests.this.container.getUri() + "/destchinesecharsblob%E9%98%BF%E4%B6%B5.txt",
                        con.getURL().toString());

                // Test the copy source request property
                assertEquals(CloudBlockBlobTests.this.container.getUri() + "/sourcechinescharsblob%E9%98%BF%E4%B6%B5.txt",
                        con.getRequestProperty("x-ms-copy-source"));
            }
        });

        copyDestination.startCopy(copySource.getUri(), null, null, null, ctx);
        copyDestination.startCopy(copySource, null, null, null, ctx);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SlowTests.class })
    public void testCopyBlockBlobWithMetadataOverride() throws URISyntaxException, StorageException, IOException,
            InterruptedException {
        Calendar calendar = Calendar.getInstance(Utility.UTC_ZONE);
        String data = "String data";
        CloudBlockBlob source = this.container.getBlockBlobReference("source");

        BlobTestHelper.setBlobProperties(source);

        source.uploadText(data, Constants.UTF8_CHARSET, null, null, null);

        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        CloudBlockBlob copy = this.container.getBlockBlobReference("copy");
        copy.getMetadata().put("Test2", "value2");
        String copyId = copy.startCopy(BlobTestHelper.defiddler(source));
        BlobTestHelper.waitForCopy(copy);

        assertEquals(CopyStatus.SUCCESS, copy.getCopyState().getStatus());
        assertEquals(source.getQualifiedUri().getPath(), copy.getCopyState().getSource().getPath());
        assertEquals(data.length(), copy.getCopyState().getTotalBytes().intValue());
        assertEquals(data.length(), copy.getCopyState().getBytesCopied().intValue());
        assertEquals(copyId, copy.getCopyState().getCopyId());
        assertTrue(copy.getCopyState().getCompletionTime().compareTo(new Date(calendar.get(Calendar.MINUTE) - 1)) > 0);

        String copyData = copy.downloadText(Constants.UTF8_CHARSET, null, null, null);
        assertEquals(data, copyData);

        copy.downloadAttributes();
        source.downloadAttributes();
        BlobProperties prop1 = copy.getProperties();
        BlobProperties prop2 = source.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentDisposition(), prop2.getContentDisposition());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value2", copy.getMetadata().get("Test2"));
        assertFalse(copy.getMetadata().containsKey("Test"));

        copy.delete();
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SlowTests.class })
    public void testCopyBlockBlobFromSnapshot() throws StorageException, IOException, URISyntaxException,
            InterruptedException {
        CloudBlockBlob source = this.container.getBlockBlobReference("source");
        String data = "String data";

        BlobTestHelper.setBlobProperties(source);

        source.uploadText(data, Constants.UTF8_CHARSET, null, null, null);

        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        CloudBlockBlob snapshot = (CloudBlockBlob) source.createSnapshot();

        //Modify source
        String newData = "Hello";
        source.getMetadata().put("Test", "newvalue");
        source.uploadMetadata();
        source.getProperties().setContentMD5(null);
        source.uploadText(newData, Constants.UTF8_CHARSET, null, null, null);

        assertEquals(newData, source.downloadText(Constants.UTF8_CHARSET, null, null, null));
        assertEquals(data, snapshot.downloadText(Constants.UTF8_CHARSET, null, null, null));

        source.downloadAttributes();
        snapshot.downloadAttributes();
        assertFalse(source.getMetadata().get("Test").equals(snapshot.getMetadata().get("Test")));

        CloudBlockBlob copy = this.container.getBlockBlobReference("copy");
        String copyId = copy.startCopy(BlobTestHelper.defiddler(snapshot));
        BlobTestHelper.waitForCopy(copy);

        copy.downloadAttributes();

        assertEquals(CopyStatus.SUCCESS, copy.getCopyState().getStatus());
        assertEquals(data, copy.downloadText(Constants.UTF8_CHARSET, null, null, null));
        assertEquals(copyId, copy.getProperties().getCopyState().getCopyId());

        copy.downloadAttributes();
        BlobProperties prop1 = copy.getProperties();
        BlobProperties prop2 = snapshot.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentDisposition(), prop2.getContentDisposition());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value", copy.getMetadata().get("Test"));

        copy.delete();
    }

    /**
     * Start copying a blob and then abort
     * 
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCopyFromBlobAbortTest() throws StorageException, URISyntaxException, IOException {
        final int length = 128;
        CloudBlockBlob originalBlob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(
                this.container, BlobType.BLOCK_BLOB, "originalBlob", length, null);
        CloudBlockBlob copyBlob = this.container.getBlockBlobReference(originalBlob.getName() + "copyed");
        copyBlob.startCopy(originalBlob);

        try {
            copyBlob.abortCopy(copyBlob.getProperties().getCopyState().getCopyId());
        }
        catch (StorageException e) {
            if (!e.getErrorCode().contains("NoPendingCopyOperation")) {
                throw e;
            }
        }
    }

    
    @Test
    @Category(SlowTests.class)
    public void testCopyFileSas()
            throws InvalidKeyException, URISyntaxException, StorageException, IOException, InterruptedException {
        // Create source on server.
        final CloudFileShare share = FileTestHelper.getRandomShareReference();
        try {
            share.create();
            final CloudFile source = share.getRootDirectoryReference().getFileReference("source");

            final String data = "String data";
            source.getMetadata().put("Test", "value");
            source.uploadText(data, Constants.UTF8_CHARSET, null, null, null);

            Calendar cal = Calendar.getInstance(Utility.UTC_ZONE);
            cal.add(Calendar.MINUTE, 5);

            // Source SAS must have read permissions
            SharedAccessFilePolicy policy = new SharedAccessFilePolicy();
            policy.setPermissions(EnumSet.of(SharedAccessFilePermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());

            String sasToken = source.generateSharedAccessSignature(policy, null, null);

            // Get destination reference
            final CloudBlockBlob destination = this.container.getBlockBlobReference("destination");

            // Start copy and wait for completion
            StorageCredentialsSharedAccessSignature credentials = new StorageCredentialsSharedAccessSignature(sasToken);
            String copyId = destination.startCopy(new CloudFile(credentials.transformUri(source.getUri())));
            BlobTestHelper.waitForCopy(destination);
            destination.downloadAttributes();
            assertNotNull(destination.getProperties().getEtag());

            // Check original file references for equality
            assertEquals(CopyStatus.SUCCESS, destination.getCopyState().getStatus());
            assertEquals(source.getServiceClient().getCredentials().transformUri(source.getUri()).getPath(),
                    destination.getCopyState().getSource().getPath());
            assertEquals(data.length(), destination.getCopyState().getTotalBytes().intValue());
            assertEquals(data.length(), destination.getCopyState().getBytesCopied().intValue());
            assertEquals(copyId, destination.getProperties().getCopyState().getCopyId());

            // Attempt to abort the completed copy operation.
            try {
                destination.abortCopy(destination.getCopyState().getCopyId());
                fail();
            }
            catch (StorageException ex) {
                assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
            }

            String copyData = destination.downloadText(Constants.UTF8_CHARSET, null, null, null);
            assertEquals(data, copyData);

            source.downloadAttributes();
            BlobProperties prop1 = destination.getProperties();
            FileProperties prop2 = source.getProperties();

            assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
            assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
            assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
            assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
            assertEquals(prop1.getContentType(), prop2.getContentType());

            assertEquals("value", destination.getMetadata().get("Test"));
            assertEquals(1, destination.getMetadata().size());
        }
        finally {
            share.deleteIfExists();
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SlowTests.class })
    public void testCopyFileWithMetadataOverride()
            throws URISyntaxException, StorageException, IOException, InterruptedException, InvalidKeyException {
        Calendar calendar = Calendar.getInstance(Utility.UTC_ZONE);
        String data = "String data";

        final CloudFileShare share = FileTestHelper.getRandomShareReference();
        try {
            share.create();
            final CloudFile source = share.getRootDirectoryReference().getFileReference("source");
            FileTestHelper.setFileProperties(source);

            // do this to make sure the set MD5 can be compared, otherwise when the dummy value
            // doesn't match the actual MD5 an exception would be thrown
            BlobRequestOptions options = new BlobRequestOptions();
            options.setDisableContentMD5Validation(true);

            source.getMetadata().put("Test", "value");
            source.uploadText(data);

            calendar.add(Calendar.MINUTE, 5);

            // Source SAS must have read permissions
            SharedAccessFilePolicy policy = new SharedAccessFilePolicy();
            policy.setPermissions(EnumSet.of(SharedAccessFilePermissions.READ));
            policy.setSharedAccessExpiryTime(calendar.getTime());

            String sasToken = source.generateSharedAccessSignature(policy, null, null);

            // Get source BlockBlob reference
            StorageCredentialsSharedAccessSignature credentials = new StorageCredentialsSharedAccessSignature(sasToken);
            CloudBlockBlob destination = this.container.getBlockBlobReference("copy");

            destination.getMetadata().put("Test2", "value2");
            String copyId = destination.startCopy(
                    FileTestHelper.defiddler(new CloudFile(credentials.transformUri(source.getUri()))));
            BlobTestHelper.waitForCopy(destination);
            destination.downloadAttributes();

            assertEquals(CopyStatus.SUCCESS, destination.getCopyState().getStatus());
            assertEquals(source.getServiceClient().getCredentials().transformUri(source.getUri()).getPath(),
                    destination.getCopyState().getSource().getPath());
            assertEquals(data.length(), destination.getCopyState().getTotalBytes().intValue());
            assertEquals(data.length(), destination.getCopyState().getBytesCopied().intValue());
            assertEquals(copyId, destination.getCopyState().getCopyId());
            assertTrue(0 < destination.getCopyState().getCompletionTime().compareTo(
                    new Date(calendar.get(Calendar.MINUTE) - 6)));

            String copyData = destination.downloadText(Constants.UTF8_CHARSET, null, options, null);
            assertEquals(data, copyData);

            source.downloadAttributes();
            BlobProperties prop1 = destination.getProperties();
            FileProperties prop2 = source.getProperties();

            assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
            assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
            assertEquals(prop1.getContentDisposition(),
                    prop2.getContentDisposition());
            assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
            assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
            assertEquals(prop1.getContentType(), prop2.getContentType());

            assertEquals("value2", destination.getMetadata().get("Test2"));
            assertFalse(destination.getMetadata().containsKey("Test"));
            assertEquals(1, destination.getMetadata().size());
        }
        finally {
            share.deleteIfExists();
        }
    }

    /**
     * Start copying a file and then abort
     *
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws InterruptedException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCopyFileAbort()
            throws StorageException, URISyntaxException, IOException, InvalidKeyException, InterruptedException {
        final int length = 128;
        final CloudFileShare share = FileTestHelper.getRandomShareReference();
        share.create();
        final CloudFile source = FileTestHelper.uploadNewFile(share, length, null);

        // Source SAS must have read permissions
        SharedAccessFilePolicy policy = new SharedAccessFilePolicy();
        policy.setPermissions(EnumSet.of(SharedAccessFilePermissions.READ));

        Calendar cal = Calendar.getInstance(Utility.UTC_ZONE);
        cal.add(Calendar.MINUTE, 5);
        policy.setSharedAccessExpiryTime(cal.getTime());
        String sasToken = source.generateSharedAccessSignature(policy, null, null);

        // Start copy and wait for completion
        final CloudBlockBlob destination = this.container.getBlockBlobReference(source.getName() + "copyed");
        StorageCredentialsSharedAccessSignature credentials = new StorageCredentialsSharedAccessSignature(sasToken);
        destination.startCopy(new CloudFile(credentials.transformUri(source.getUri())));

        try {
            destination.abortCopy(destination.getProperties().getCopyState().getCopyId());
            BlobTestHelper.waitForCopy(destination);
            fail();
        }
        catch (StorageException e) {
            if (!e.getErrorCode().contains("NoPendingCopyOperation")) {
                throw e;
            }
        }
        finally {
            share.deleteIfExists();
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testDeleteBlobIfExists() throws URISyntaxException, StorageException, IOException {
        final CloudBlockBlob blob1 = this.container.getBlockBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("testBlob"));

        assertFalse(blob1.exists());
        assertFalse(blob1.deleteIfExists());

        blob1.uploadText("test1");
        assertTrue(blob1.exists());

        assertTrue(blob1.deleteIfExists());
        assertFalse(blob1.deleteIfExists());

        // check if second condition works in delete if exists
        OperationContext ctx = new OperationContext();
        ctx.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                if (((HttpURLConnection) eventArg.getConnectionObject()).getRequestMethod().equals("DELETE")) {
                    try {
                        blob1.delete();
                        assertFalse(blob1.exists());
                    }
                    catch (StorageException e) {
                        fail("Delete should succeed.");
                    }
                }
            }
        });

        // The second delete of a blob will return a 404
        blob1.uploadText("test1");
        assertFalse(blob1.deleteIfExists(DeleteSnapshotsOption.NONE, null, null, ctx));
    }

    /**
     * Create a snapshot
     * 
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobSnapshotValidationTest() throws StorageException, URISyntaxException, IOException {
        final int length = 1024;
        CloudBlockBlob blockBlobRef = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container,
                BlobType.BLOCK_BLOB, "originalBlob", length, null);
        final CloudBlob blobSnapshot = blockBlobRef.createSnapshot();

        for (ListBlobItem blob : this.container.listBlobs(null, true, EnumSet.allOf(BlobListingDetails.class), null,
                null)) {
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream(length);
            ((CloudBlob) blob).download(outStream);
        }

        ByteArrayOutputStream outStream = new ByteArrayOutputStream(length);

        blobSnapshot.download(outStream);
        byte[] retrievedBuff = outStream.toByteArray();
        assertEquals(length, retrievedBuff.length);

        // Read operation should work fine.
        blobSnapshot.downloadAttributes();

        final CloudBlockBlob blobSnapshotUsingRootUri = this.container.getBlockBlobReference(blockBlobRef.getName(),
                blobSnapshot.getSnapshotID());
        outStream = new ByteArrayOutputStream(length);

        blobSnapshotUsingRootUri.download(outStream);
        retrievedBuff = outStream.toByteArray();
        assertEquals(length, retrievedBuff.length);
        assertEquals(blobSnapshot.getSnapshotID(), blobSnapshotUsingRootUri.getSnapshotID());

        // Expect an IllegalArgumentException from upload.
        try {
            final Random randGenerator = new Random();
            final byte[] buff = new byte[length];
            randGenerator.nextBytes(buff);
            blobSnapshot.upload(new ByteArrayInputStream(buff), -1);
            fail("Expect an IllegalArgumentException from upload");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
        }

        // Expect an IllegalArgumentException from uploadMetadata.
        try {
            blobSnapshot.uploadMetadata();
            fail("Expect an IllegalArgumentException from uploadMetadata");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
        }

        // Expect an IllegalArgumentException from uploadProperties.
        try {
            blobSnapshot.uploadProperties();
            fail("Expect an IllegalArgumentException from uploadProperties");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
        }

        // Expect an IllegalArgumentException from createSnapshot.
        try {
            blobSnapshot.createSnapshot();
            fail("Expect an IllegalArgumentException from createSnapshot");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
        }
    }

    /**
     * Create a blob and try to download a range of its contents
     * 
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobDownloadRangeValidationTest() throws StorageException, URISyntaxException, IOException {
        final int blockLength = 1024 * 1024;
        final int length = 5 * blockLength;

        final String blockBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudBlockBlob blockBlobRef = this.container.getBlockBlobReference(blockBlobName);

        ArrayList<BlockEntry> blockList = new ArrayList<BlockEntry>();
        for (int i = 1; i <= 5; i++) {
            String blockID = String.format("%08d", i);
            blockBlobRef
                    .uploadBlock(blockID, BlobTestHelper.getRandomDataStream(length), blockLength, null, null, null);
            blockList.add(new BlockEntry(blockID, BlockSearchMode.LATEST));
        }

        blockBlobRef.commitBlockList(blockList);

        //Download full blob
        blockBlobRef.download(new ByteArrayOutputStream());
        assertEquals(length, blockBlobRef.getProperties().getLength());

        //Download blob range.
        ByteArrayOutputStream downloadStream = new ByteArrayOutputStream();
        blockBlobRef.downloadRange(0, (long) 100, downloadStream);
        assertEquals(length, blockBlobRef.getProperties().getLength());

        //Download block list.
        blockBlobRef.downloadBlockList();
        assertEquals(length, blockBlobRef.getProperties().getLength());
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCommitBlockListContentMd5() throws URISyntaxException, StorageException, IOException {
        int length = 1024;
        byte[] buffer = BlobTestHelper.getRandomBuffer(length);
        Map<String, BlockEntry> blocks = BlobTestHelper.getBlockEntryList(3);
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("blob1");

        CloudBlockBlob blob = this.container.getBlockBlobReference(blobName);
        for (BlockEntry block : blocks.values()) {
            blob.uploadBlock(block.getId(), new ByteArrayInputStream(buffer), length);
        }

        OperationContext ctx = new OperationContext();
        ctx.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection conn = (HttpURLConnection)eventArg.getConnectionObject();
                assertNull(conn.getRequestProperty("Content-MD5"));
            }
        });

        blob.commitBlockList(blocks.values(), null, null, ctx);

        BlobRequestOptions opt = new BlobRequestOptions();
        opt.setUseTransactionalContentMD5(true);

        ctx = new OperationContext();
        ctx.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection conn = (HttpURLConnection)eventArg.getConnectionObject();
                assertNotNull(conn.getRequestProperty("Content-MD5"));
            }
        });

        blob.commitBlockList(blocks.values(), null, opt, ctx);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testDownloadBlockList() throws URISyntaxException, StorageException, IOException {
        int length = 1024;
        byte[] buffer = BlobTestHelper.getRandomBuffer(length);
        Map<String, BlockEntry> blocks = BlobTestHelper.getBlockEntryList(3);
        Map<String, BlockEntry> extraBlocks = BlobTestHelper.getBlockEntryList(2);
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("blob1");

        CloudBlockBlob blob = this.container.getBlockBlobReference(blobName);
        for (BlockEntry block : blocks.values()) {
            blob.uploadBlock(block.getId(), new ByteArrayInputStream(buffer), length);
        }
        blob.commitBlockList(blocks.values());

        for (BlockEntry block : extraBlocks.values()) {
            blob.uploadBlock(block.getId(), new ByteArrayInputStream(buffer), length);
        }

        CloudBlockBlob blob2 = this.container.getBlockBlobReference(blobName);
        blob2.downloadAttributes();
        assertEquals(1024 * blocks.size(), blob2.getProperties().getLength());

        List<BlockEntry> blockList = blob2.downloadBlockList();
        assertEquals(3, blockList.size());
        for (BlockEntry blockItem : blockList) {
            assertEquals(BlockSearchMode.COMMITTED, blockItem.getSearchMode());
            assertEquals(length, blockItem.getSize());
            assertFalse(blocks.remove(blockItem.getId()) == null);
        }
        assertEquals(0, blocks.size());

        blockList = blob2.downloadBlockList(BlockListingFilter.UNCOMMITTED, null, null, null);
        assertEquals(2, blockList.size());
        for (BlockEntry blockItem : blockList) {
            assertEquals(BlockSearchMode.UNCOMMITTED, blockItem.getSearchMode());
            assertEquals(length, blockItem.getSize());
            assertFalse(extraBlocks.remove(blockItem.getId()) == null);
        }
        assertEquals(0, extraBlocks.size());

        blockList = blob2.downloadBlockList(BlockListingFilter.ALL, null, null, null);
        assertEquals(5, blockList.size());
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlockBlobDownloadRangeTest() throws URISyntaxException, StorageException, IOException {
        byte[] buffer = BlobTestHelper.getRandomBuffer(2 * 1024);

        CloudBlockBlob blob = this.container.getBlockBlobReference("blob1");
        ByteArrayInputStream wholeBlob = new ByteArrayInputStream(buffer);
        blob.upload(wholeBlob, -1);

        ByteArrayOutputStream blobStream = new ByteArrayOutputStream();
        try {
            blob.downloadRange(0, new Long(0), blobStream, null, null, null);
        }
        catch (IndexOutOfBoundsException ex) {

        }

        blob.downloadRange(0, new Long(1024), blobStream);
        assertEquals(blobStream.size(), 1024);
        BlobTestHelper.assertStreamsAreEqualAtIndex(new ByteArrayInputStream(blobStream.toByteArray()), wholeBlob, 0,
                0, 1024, 2 * 1024);

        CloudBlockBlob blob2 = this.container.getBlockBlobReference("blob1");
        try {
            blob.downloadRange(1024, new Long(0), blobStream, null, null, null);
        }
        catch (IndexOutOfBoundsException ex) {

        }

        ByteArrayOutputStream blobStream2 = new ByteArrayOutputStream();
        blob2.downloadRange(1024, new Long(1024), blobStream2);
        BlobTestHelper.assertStreamsAreEqualAtIndex(new ByteArrayInputStream(blobStream2.toByteArray()), wholeBlob,
                0, 1024, 1024, 2 * 1024);
        blob2.downloadAttributes();
        BlobTestHelper.assertAreEqual(blob, blob2);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobUploadFromStreamTest() throws URISyntaxException, StorageException, IOException {
        final String blockBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudBlockBlob blockBlobRef = this.container.getBlockBlobReference(blockBlobName);

        int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);
        blockBlobRef.upload(srcStream, -1);
        ByteArrayOutputStream dstStream = new ByteArrayOutputStream();
        blockBlobRef.download(dstStream);
        BlobTestHelper.assertStreamsAreEqual(srcStream, new ByteArrayInputStream(dstStream.toByteArray()));

        length = 5 * 1024 * 1024;
        srcStream = BlobTestHelper.getRandomDataStream(length);
        blockBlobRef.upload(srcStream, length);
        dstStream = new ByteArrayOutputStream();
        blockBlobRef.download(dstStream);
        BlobTestHelper.assertStreamsAreEqual(srcStream, new ByteArrayInputStream(dstStream.toByteArray()));
    }

    @Test
    public void testBlobUploadFromStreamAccessConditionTest() throws URISyntaxException, StorageException, IOException {
        final String blockBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudBlockBlob blockBlobRef = this.container.getBlockBlobReference(blockBlobName);
        AccessCondition accessCondition = AccessCondition.generateIfNotModifiedSinceCondition(new Date());

        int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);
        blockBlobRef.upload(srcStream, -1, accessCondition, null, null);
        ByteArrayOutputStream dstStream = new ByteArrayOutputStream();
        blockBlobRef.download(dstStream);
        BlobTestHelper.assertStreamsAreEqual(srcStream, new ByteArrayInputStream(dstStream.toByteArray()));

        length = 5 * 1024 * 1024;
        srcStream = BlobTestHelper.getRandomDataStream(length);
        blockBlobRef.upload(srcStream, length);
        dstStream = new ByteArrayOutputStream();
        blockBlobRef.download(dstStream);
        BlobTestHelper.assertStreamsAreEqual(srcStream, new ByteArrayInputStream(dstStream.toByteArray()));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobUploadFromStreamRequestOptionsTest() throws URISyntaxException, StorageException, IOException {
        final String blockBlobName1 = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudBlockBlob blockBlobRef1 = this.container.getBlockBlobReference(blockBlobName1);

        final String blockBlobName2 = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudBlockBlob blockBlobRef2 = this.container.getBlockBlobReference(blockBlobName2);

        final int length = 2 * com.microsoft.azure.storage.Constants.MB;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);

        BlobRequestOptions options = new BlobRequestOptions();
        options.setSingleBlobPutThresholdInBytes(length / 2);
        options.setRetryPolicyFactory(RetryNoRetry.getInstance());
        OperationContext context = new OperationContext();
        blockBlobRef1.upload(srcStream, length, null /* accessCondition */, options, context);

        assertTrue(context.getRequestResults().size() >= 2);

        srcStream.reset();
        options.setSingleBlobPutThresholdInBytes(length);
        context = new OperationContext();
        blockBlobRef2.upload(srcStream, length, null /* accessCondition */, options, context);

        assertTrue(context.getRequestResults().size() <= 2);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testUploadDownloadBlobProperties() throws URISyntaxException, StorageException, IOException {
        final int length = 0;

        // do this to make sure the set MD5 can be compared without an exception being thrown
        BlobRequestOptions options = new BlobRequestOptions();
        options.setDisableContentMD5Validation(true);

        // with explicit upload/download of properties
        String blockBlobName1 = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        CloudBlockBlob blockBlobRef1 = this.container.getBlockBlobReference(blockBlobName1);

        blockBlobRef1.upload(BlobTestHelper.getRandomDataStream(length), length);

        // this is not set by upload (it is for page blob!), so set this manually
        blockBlobRef1.getProperties().setLength(length);

        BlobTestHelper.setBlobProperties(blockBlobRef1);
        BlobProperties props1 = blockBlobRef1.getProperties();
        blockBlobRef1.uploadProperties();

        blockBlobRef1.downloadAttributes(null, options, null);
        BlobProperties props2 = blockBlobRef1.getProperties();

        assertEquals(props1.getLength(), props2.getLength());
        BlobTestHelper.assertAreEqual(props1, props2);

        // by uploading/downloading the blob
        blockBlobName1 = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        blockBlobRef1 = this.container.getBlockBlobReference(blockBlobName1);

        BlobTestHelper.setBlobProperties(blockBlobRef1);
        props1 = blockBlobRef1.getProperties();

        blockBlobRef1.upload(BlobTestHelper.getRandomDataStream(length), length);

        blockBlobRef1.download(new ByteArrayOutputStream(), null, options, null);
        props2 = blockBlobRef1.getProperties();

        BlobTestHelper.assertAreEqual(props1, props2);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobUploadWithoutMD5Validation() throws URISyntaxException, StorageException, IOException {
        final String blockBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudBlockBlob blockBlobRef = this.container.getBlockBlobReference(blockBlobName);

        final int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);
        BlobRequestOptions options = new BlobRequestOptions();
        options.setDisableContentMD5Validation(false);
        options.setStoreBlobContentMD5(false);

        blockBlobRef.upload(srcStream, -1, null, options, null);
        blockBlobRef.downloadAttributes();
        blockBlobRef.getProperties().setContentMD5("MDAwMDAwMDA=");
        blockBlobRef.uploadProperties(null, options, null);

        try {
            blockBlobRef.download(new ByteArrayOutputStream(), null, options, null);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(306, ex.getHttpStatusCode());
            assertEquals("InvalidMd5", ex.getErrorCode());
        }

        options.setDisableContentMD5Validation(true);
        blockBlobRef.download(new ByteArrayOutputStream(), null, options, null);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlockBlobUploadContentMD5() throws URISyntaxException, StorageException, IOException {
        final String blockBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        CloudBlockBlob blockBlobRef = this.container.getBlockBlobReference(blockBlobName);

        int length = 16 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);

        final ArrayList<Boolean> callList = new ArrayList<Boolean>();
        OperationContext sendingRequestEventContext = new OperationContext();
        StorageEvent<SendingRequestEvent> event = new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                assertNotNull(((HttpURLConnection) eventArg.getConnectionObject())
                        .getRequestProperty(BlobConstants.BLOB_CONTENT_MD5_HEADER));
                callList.add(true);
            }
        };

        sendingRequestEventContext.getSendingRequestEventHandler().addListener(event);
        assertEquals(0, callList.size());

        // Upload with length less than single threshold. Make sure we calculate the contentMD5.
        blockBlobRef.upload(srcStream, length, null, null, sendingRequestEventContext);
        assertEquals(1, callList.size());

        sendingRequestEventContext.getSendingRequestEventHandler().removeListener(event);
        callList.clear();

        event = new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                callList.add(true);
            }
        };

        length = 33 * 1024 * 1024;
        srcStream = BlobTestHelper.getRandomDataStream(length);

        sendingRequestEventContext.getSendingRequestEventHandler().addListener(event);
        assertEquals(0, callList.size());

        // Upload with length greater than single threshold. This will do multiple block uploads.
        blockBlobRef.upload(srcStream, length, null, null, sendingRequestEventContext);
        assertTrue(callList.size() > 1);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobEmptyHeaderSigningTest() throws URISyntaxException, StorageException, IOException {
        final String blockBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudBlockBlob blockBlobRef = this.container.getBlockBlobReference(blockBlobName);

        final int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);

        OperationContext context = new OperationContext();
        context.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection connection = (HttpURLConnection) eventArg.getConnectionObject();
                connection.setRequestProperty("x-ms-foo", "");
            }
        });

        blockBlobRef.upload(srcStream, -1, null, null, context);
        blockBlobRef.download(new ByteArrayOutputStream(), null, null, context);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlockBlobDownloadToByteArray() throws URISyntaxException, StorageException, IOException {
        CloudBlockBlob blob = this.container.getBlockBlobReference("blob1");
        BlobTestHelper.doDownloadTest(blob, 1 * 512, 2 * 512, 0);
        BlobTestHelper.doDownloadTest(blob, 1 * 512, 2 * 512, 1 * 512);
        BlobTestHelper.doDownloadTest(blob, 2 * 512, 4 * 512, 1 * 512);
        BlobTestHelper.doDownloadTest(blob, 5 * 1024 * 1024, 5 * 1024 * 1024, 0);
        BlobTestHelper.doDownloadTest(blob, 5 * 1024 * 1024, 6 * 1024 * 1024, 512);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlockBlobDownloadRangeToByteArray() throws URISyntaxException, StorageException, IOException {
        CloudBlockBlob blob = this.container.getBlockBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("downloadrange"));

        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 8 * 1024 * 1024, 8 * 1024 * 1024, 1 * 1024 * 1024,
                new Long(1 * 1024 * 1024), new Long(5 * 1024 * 1024));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 8 * 1024 * 1024, 8 * 1024 * 1024, 2 * 1024 * 1024,
                new Long(2 * 1024 * 1024), new Long(6 * 1024 * 1024));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 8 * 1024 * 1024, 8 * 1024 * 1024, 1 * 1024 * 1024,
                new Long(4 * 1024 * 1024), new Long(4 * 1024 * 1024));

        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 0, new Long(1 * 512), new Long(1 * 512));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 1 * 512, new Long(0), null);
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 1 * 512, new Long(1 * 512), null);
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 1 * 512, new Long(0), new Long(1 * 512));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 2 * 512, new Long(1 * 512), new Long(
                1 * 512));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 2 * 512, new Long(1 * 512), new Long(
                2 * 512));

        // Edge cases
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 1023, new Long(1023), new Long(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 0, new Long(1023), new Long(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 0, new Long(0), new Long(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 0, new Long(512), new Long(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 512, new Long(1023), new Long(1));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlockBlobDownloadRangeToByteArrayNegativeTest() throws URISyntaxException, StorageException,
            IOException {
        CloudBlockBlob blob = this.container.getBlockBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("downloadrangenegative"));
        BlobTestHelper.doDownloadRangeToByteArrayNegativeTests(blob);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testCloudBlockBlobUploadFromStreamWithAccessCondition() throws URISyntaxException, StorageException,
            IOException {
        CloudBlockBlob blob1 = this.container.getBlockBlobReference("blob1");
        AccessCondition accessCondition = AccessCondition.generateIfNoneMatchCondition("\"*\"");

        final int length = 2 * 1024 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);

        blob1.upload(srcStream, length, accessCondition, null, null);
        blob1.downloadAttributes();

        CloudBlockBlob blob2 = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB,
                "accesscond", length, null);
        blob2.downloadAttributes();

        srcStream.reset();
        accessCondition = AccessCondition.generateIfNoneMatchCondition(blob1.getProperties().getEtag());
        try {
            blob1.upload(srcStream, length, accessCondition, null, null);
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getHttpStatusCode());
        }

        srcStream.reset();
        accessCondition = AccessCondition.generateIfMatchCondition(blob1.getProperties().getEtag());
        blob1.upload(srcStream, length, accessCondition, null, null);

        srcStream.reset();
        accessCondition = AccessCondition.generateIfMatchCondition(blob2.getProperties().getEtag());
        try {
            blob1.upload(srcStream, length, accessCondition, null, null);
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getHttpStatusCode());
        }

        srcStream.reset();
        accessCondition = AccessCondition.generateIfNoneMatchCondition(blob2.getProperties().getEtag());
        blob1.upload(srcStream, length, accessCondition, null, null);
    }

    /**
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SlowTests.class })
    public void testBlobNamePlusEncodingTest() throws StorageException, URISyntaxException, IOException,
            InterruptedException {
        final int length = 1 * 1024;

        final CloudBlockBlob originalBlob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container,
                BlobType.BLOCK_BLOB, "a+b.txt", length, null);
        final CloudBlockBlob copyBlob = this.container.getBlockBlobReference(originalBlob.getName() + "copyed");

        copyBlob.startCopy(originalBlob);
        BlobTestHelper.waitForCopy(copyBlob);
        copyBlob.downloadAttributes();
    }

    /**
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testSendingRequestEventBlob() throws StorageException, URISyntaxException, IOException {
        final int length = 128;

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

        //Put blob
        CloudBlob blob = BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, "bb", length,
                sendingRequestEventContext);

        assertEquals(1, callList.size());

        //Get blob
        blob.download(new ByteArrayOutputStream(), null, null, sendingRequestEventContext);
        assertEquals(2, callList.size());

        //uploadMetadata
        blob.uploadMetadata(null, null, sendingRequestEventContext);
        assertEquals(3, callList.size());

        //uploadMetadata
        blob.downloadAttributes(null, null, sendingRequestEventContext);
        assertEquals(4, callList.size());

    }

    /**
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     */
    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobInputStream() throws URISyntaxException, StorageException, IOException {
        final int blobLength = 16 * 1024;
        final Random randGenerator = new Random();
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudBlockBlob blobRef = this.container.getBlockBlobReference(blobName);

        final byte[] buff = new byte[blobLength];
        randGenerator.nextBytes(buff);
        buff[0] = -1;
        buff[1] = -128;
        final ByteArrayInputStream sourceStream = new ByteArrayInputStream(buff);

        final BlobRequestOptions options = new BlobRequestOptions();
        final OperationContext operationContext = new OperationContext();
        options.setStoreBlobContentMD5(true);
        options.setTimeoutIntervalInMs(90000);
        options.setRetryPolicyFactory(new RetryNoRetry());
        blobRef.uploadFullBlob(sourceStream, blobLength, null, options, operationContext);

        BlobInputStream blobStream = blobRef.openInputStream();

        for (int i = 0; i < blobLength; i++) {
            int data = blobStream.read();
            assertTrue(data >= 0);
            assertEquals(buff[i], (byte) data);
        }

        assertEquals(-1, blobStream.read());

        blobRef.delete();
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testUploadFromByteArray() throws Exception {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudBlockBlob blob = this.container.getBlockBlobReference(blobName);

        this.doUploadFromByteArrayTest(blob, 4 * 512, 0, 4 * 512);
        this.doUploadFromByteArrayTest(blob, 4 * 512, 0, 2 * 512);
        this.doUploadFromByteArrayTest(blob, 4 * 512, 1 * 512, 2 * 512);
        this.doUploadFromByteArrayTest(blob, 4 * 512, 2 * 512, 2 * 512);
        this.doUploadFromByteArrayTest(blob, 512, 0, 511);
    }

    private void doUploadFromByteArrayTest(CloudBlockBlob blob, int bufferSize, int bufferOffset, int count)
            throws Exception {
        byte[] buffer = BlobTestHelper.getRandomBuffer(bufferSize);
        byte[] downloadedBuffer = new byte[bufferSize];

        blob.uploadFromByteArray(buffer, bufferOffset, count);
        blob.downloadToByteArray(downloadedBuffer, 0);

        int i = 0;
        for (; i < count; i++) {
            assertEquals(buffer[i + bufferOffset], downloadedBuffer[i]);
        }

        for (; i < downloadedBuffer.length; i++) {
            assertEquals(0, downloadedBuffer[i]);
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testUploadDownloadFromFile() throws IOException, StorageException, URISyntaxException {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudBlockBlob blob = this.container.getBlockBlobReference(blobName);

        //this.doUploadDownloadFileTest(blob, 0);
        //this.doUploadDownloadFileTest(blob, 4096);
        //this.doUploadDownloadFileTest(blob, 4097);
        this.doUploadDownloadFileTest(blob, 5 * 1024 * 1024);
        //this.doUploadDownloadFileTest(blob, 11 * 1024 * 1024);
    }

    private void doUploadDownloadFileTest(CloudBlockBlob blob, int fileSize) throws IOException, StorageException {
        File sourceFile = File.createTempFile("sourceFile", ".tmp");
        File destinationFile = new File(sourceFile.getParentFile(), "destinationFile.tmp");

        try {

            byte[] buffer = BlobTestHelper.getRandomBuffer(fileSize);
            FileOutputStream fos = new FileOutputStream(sourceFile);
            fos.write(buffer);
            fos.close();
            blob.uploadFromFile(sourceFile.getAbsolutePath());

            blob.downloadToFile(destinationFile.getAbsolutePath());
            assertTrue("Destination file does not exist.", destinationFile.exists());
            assertEquals("Destination file does not match input file.", fileSize, destinationFile.length());
            FileInputStream fis = new FileInputStream(destinationFile);

            byte[] readBuffer = new byte[fileSize];
            fis.read(readBuffer);
            fis.close();

            for (int i = 0; i < fileSize; i++) {
                assertEquals("File contents do not match.", buffer[i], readBuffer[i]);
            }
        }
        finally {
            if (sourceFile.exists()) {
                sourceFile.delete();
            }

            if (destinationFile.exists()) {
                destinationFile.delete();
            }
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testUploadDownloadFromText() throws URISyntaxException, StorageException, IOException {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudBlockBlob blob = this.container.getBlockBlobReference(blobName);

        this.doUploadDownloadStringTest(blob, 0);
        this.doUploadDownloadStringTest(blob, 8000);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobMultiConditionHeaders() throws URISyntaxException, StorageException, IOException {
        final String blockBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudBlockBlob blockBlobRef = this.container.getBlockBlobReference(blockBlobName);

        final int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);
        OperationContext context = new OperationContext();
        blockBlobRef.upload(srcStream, -1, null, null, context);

        AccessCondition condition = AccessCondition.generateIfMatchCondition(context.getLastResult().getEtag());
        condition.setIfUnmodifiedSinceDate(context.getLastResult().getStartDate());

        StorageEvent<SendingRequestEvent> event = new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection connection = (HttpURLConnection) eventArg.getConnectionObject();
                assertNotNull(connection.getRequestProperty("If-Unmodified-Since"));
                assertNotNull(connection.getRequestProperty("If-Match"));
            }
        };

        context.getSendingRequestEventHandler().addListener(event);

        blockBlobRef.upload(srcStream, -1, condition, null, context);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobConditionalAccess() throws StorageException, IOException, URISyntaxException {
        CloudBlockBlob blob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB,
                "test", 128, null);
        blob.downloadAttributes();

        String currentETag = blob.getProperties().getEtag();
        Date currentModifiedTime = blob.getProperties().getLastModified();

        // ETag conditional tests
        blob.getMetadata().put("ETagConditionalName", "ETagConditionalValue");
        blob.uploadMetadata(AccessCondition.generateIfMatchCondition(currentETag), null, null);

        blob.downloadAttributes();
        String newETag = blob.getProperties().getEtag();
        assertFalse(newETag.equals(currentETag));

        blob.getMetadata().put("ETagConditionalName", "ETagConditionalValue2");

        try {
            blob.uploadMetadata(AccessCondition.generateIfNoneMatchCondition(newETag), null, null);
            fail("If none match on conditional test should throw");
        }
        catch (StorageException e) {
            assertEquals("ConditionNotMet", e.getErrorCode());
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, e.getHttpStatusCode());
            assertEquals("The condition specified using HTTP conditional header(s) is not met.", e.getMessage());
        }

        String invalidETag = "\"0x10101010\"";
        try {
            blob.uploadMetadata(AccessCondition.generateIfMatchCondition(invalidETag), null, null);
            fail("Invalid ETag on conditional test should throw");
        }
        catch (StorageException e) {
            assertEquals("ConditionNotMet", e.getErrorCode());
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, e.getHttpStatusCode());
            assertEquals("The condition specified using HTTP conditional header(s) is not met.", e.getMessage());
        }

        currentETag = blob.getProperties().getEtag();
        blob.uploadMetadata(AccessCondition.generateIfNoneMatchCondition(invalidETag), null, null);

        blob.downloadAttributes();
        newETag = blob.getProperties().getEtag();

        // LastModifiedTime tests
        currentModifiedTime = blob.getProperties().getLastModified();

        blob.getMetadata().put("DateConditionalName", "DateConditionalValue");

        try {
            blob.uploadMetadata(AccessCondition.generateIfModifiedSinceCondition(currentModifiedTime), null, null);
            fail("IfModifiedSince conditional on current modified time should throw");
        }
        catch (StorageException e) {
            assertEquals("ConditionNotMet", e.getErrorCode());
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, e.getHttpStatusCode());
            assertEquals("The condition specified using HTTP conditional header(s) is not met.", e.getMessage());
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentModifiedTime);
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - 5);
        Date pastTime = cal.getTime();
        blob.uploadMetadata(AccessCondition.generateIfModifiedSinceCondition(pastTime), null, null);

        cal = Calendar.getInstance();
        cal.setTime(currentModifiedTime);
        cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) - 5);
        pastTime = cal.getTime();
        blob.uploadMetadata(AccessCondition.generateIfModifiedSinceCondition(pastTime), null, null);

        cal = Calendar.getInstance();
        cal.setTime(currentModifiedTime);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 5);
        pastTime = cal.getTime();
        blob.uploadMetadata(AccessCondition.generateIfModifiedSinceCondition(pastTime), null, null);

        currentModifiedTime = blob.getProperties().getLastModified();

        cal = Calendar.getInstance();
        cal.setTime(currentModifiedTime);
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - 5);
        pastTime = cal.getTime();
        try {
            blob.uploadMetadata(AccessCondition.generateIfNotModifiedSinceCondition(pastTime), null, null);
            fail("IfNotModifiedSince conditional on past time should throw");
        }
        catch (StorageException e) {
            assertEquals("ConditionNotMet", e.getErrorCode());
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, e.getHttpStatusCode());
            assertEquals("The condition specified using HTTP conditional header(s) is not met.", e.getMessage());
        }

        cal = Calendar.getInstance();
        cal.setTime(currentModifiedTime);
        cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) - 5);
        pastTime = cal.getTime();
        try {
            blob.uploadMetadata(AccessCondition.generateIfNotModifiedSinceCondition(pastTime), null, null);
            fail("IfNotModifiedSince conditional on past time should throw");
        }
        catch (StorageException e) {
            assertEquals("ConditionNotMet", e.getErrorCode());
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, e.getHttpStatusCode());
            assertEquals("The condition specified using HTTP conditional header(s) is not met.", e.getMessage());
        }

        cal = Calendar.getInstance();
        cal.setTime(currentModifiedTime);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 5);
        pastTime = cal.getTime();
        try {
            blob.uploadMetadata(AccessCondition.generateIfNotModifiedSinceCondition(pastTime), null, null);
            fail("IfNotModifiedSince conditional on past time should throw");
        }
        catch (StorageException e) {
            assertEquals("ConditionNotMet", e.getErrorCode());
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, e.getHttpStatusCode());
            assertEquals("The condition specified using HTTP conditional header(s) is not met.", e.getMessage());
        }

        blob.getMetadata().put("DateConditionalName", "DateConditionalValue2");

        currentETag = blob.getProperties().getEtag();
        blob.uploadMetadata(AccessCondition.generateIfNotModifiedSinceCondition(currentModifiedTime), null, null);

        blob.downloadAttributes();
        newETag = blob.getProperties().getEtag();
        assertFalse("ETage should be modified on write metadata", newETag.equals(currentETag));
    }

    private void doUploadDownloadStringTest(CloudBlockBlob blob, int length) throws StorageException, IOException {
        String stringToUse = this.getRandomUNCString(length);
        blob.uploadText(stringToUse, Constants.UTF8_CHARSET, null, null, null);
        String newString = blob.downloadText(Constants.UTF8_CHARSET, null, null, null);
        assertEquals("Strings are not equal", stringToUse, newString);

        stringToUse = this.getRandomUNCString(length);
        blob.uploadText(stringToUse, "UTF-16", null, null, null);
        newString = blob.downloadText("UTF-16", null, null, null);
        assertEquals("Strings are not equal", stringToUse, newString);

        stringToUse = this.getRandomUNCString(length);
        blob.uploadText(stringToUse, "UTF-16BE", null, null, null);
        newString = blob.downloadText("UTF-16BE", null, null, null);
        assertEquals("Strings are not equal", stringToUse, newString);

        stringToUse = this.getRandomUNCString(length);
        blob.uploadText(stringToUse, "UTF-16LE", null, null, null);
        newString = blob.downloadText("UTF-16LE", null, null, null);
        assertEquals("Strings are not equal", stringToUse, newString);

        stringToUse = this.getRandomASCIIString(length);
        blob.uploadText(stringToUse, "US-ASCII", null, null, null);
        newString = blob.downloadText("US-ASCII", null, null, null);
        assertEquals("Strings are not equal", stringToUse, newString);
    }

    // Not a good test over all of Unicode, but good enough for our purposes
    private String getRandomUNCString(int length) {
        return this.getRandomString(length, 0xD7FF);
    }

    private String getRandomASCIIString(int length) {
        return this.getRandomString(length, 0x7F);
    }

    private String getRandomString(int length, int maxCodePoint) {
        int[] codePoints = new int[length];
        Random random = new Random(237);
        for (int i = 0; i < length; i++) {
            codePoints[i] = random.nextInt(maxCodePoint);
        }

        return new String(codePoints, 0, length);
    }

    private void doCloudBlockBlobCopy(boolean sourceIsSas, boolean destinationIsSas) throws URISyntaxException,
            StorageException, IOException, InvalidKeyException, InterruptedException {

        // Create source on server.
        CloudBlockBlob source = this.container.getBlockBlobReference("source");

        String data = "String data";
        source.uploadText(data, Constants.UTF8_CHARSET, null, null, null);

        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        // Get destination reference
        CloudBlockBlob destination = this.container.getBlockBlobReference("destination");
        destination.commitBlockList(new ArrayList<BlockEntry>());

        CloudBlockBlob copySource = source;
        CloudBlockBlob copyDestination = destination;

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.add(Calendar.SECOND, 300);

        if (sourceIsSas) {
            // Source SAS must have read permissions
            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE));
            policy.setSharedAccessExpiryTime(cal.getTime());

            BlobContainerPermissions perms = new BlobContainerPermissions();
            perms.getSharedAccessPolicies().put("read", policy);
            this.container.uploadPermissions(perms);
            Thread.sleep(30000);

            String sasToken = source.generateSharedAccessSignature(policy, null);

            // Get source BlockBlob reference
            StorageCredentialsSharedAccessSignature credentials = new StorageCredentialsSharedAccessSignature(sasToken);
            copySource = new CloudBlockBlob(credentials.transformUri(source.getUri()));
        }

        if (destinationIsSas) {
            // Destination SAS must have write permissions
            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE));
            policy.setSharedAccessExpiryTime(cal.getTime());

            BlobContainerPermissions perms = new BlobContainerPermissions();
            // Source container must be public if source is not SAS
            if (!sourceIsSas) {
                perms.setPublicAccess(BlobContainerPublicAccessType.BLOB);
            }
            perms.getSharedAccessPolicies().put("write", policy);
            this.container.uploadPermissions(perms);
            Thread.sleep(30000);

            String sasToken = destination.generateSharedAccessSignature(policy, null);

            // Get destination block blob reference
            StorageCredentialsSharedAccessSignature credentials = new StorageCredentialsSharedAccessSignature(sasToken);
            copyDestination = new CloudBlockBlob(credentials.transformUri(destination.getUri()));
        }

        Thread.sleep(30000);

        // Start copy and wait for completion
        String copyId = copyDestination.startCopy(copySource);
        BlobTestHelper.waitForCopy(copyDestination);
        Calendar calendar = Calendar.getInstance(Utility.UTC_ZONE);
        destination.downloadAttributes();

        // Check original blob references for equality
        assertEquals(CopyStatus.SUCCESS, destination.getCopyState().getStatus());
        assertEquals(source.getQualifiedUri().getPath(), destination.getCopyState().getSource().getPath());
        assertEquals(data.length(), destination.getCopyState().getTotalBytes().intValue());
        assertEquals(data.length(), destination.getCopyState().getBytesCopied().intValue());
        assertEquals(copyId, destination.getProperties().getCopyState().getCopyId());
        assertTrue(destination.getCopyState().getCompletionTime()
                .compareTo(new Date(calendar.get(Calendar.MINUTE) - 1)) > 0);

        if (!destinationIsSas) {
            try {
                copyDestination.abortCopy(destination.getCopyState().getCopyId());
            }
            catch (StorageException ex) {
                assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
            }
        }

        source.downloadAttributes();
        assertNotNull(destination.getProperties().getEtag());
        assertFalse(source.getProperties().getEtag().equals(destination.getProperties().getEtag()));
        assertTrue(destination.getProperties().getLastModified().compareTo(new Date(calendar.get(Calendar.MINUTE) - 1)) > 0);

        String copyData = destination.downloadText(Constants.UTF8_CHARSET, null, null, null);
        assertEquals(data, copyData);

        BlobProperties prop1 = destination.getProperties();
        BlobProperties prop2 = source.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value", destination.getMetadata().get("Test"));

        destination.delete();
        source.delete();
    }
}
