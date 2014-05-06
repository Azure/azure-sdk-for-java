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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.LocationMode;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.SecondaryTests;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;
import com.microsoft.azure.storage.core.PathUtility;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class SasTests {

    protected CloudBlobContainer container;
    protected CloudBlockBlob blob;

    @Before
    public void leaseTestMethodSetup() throws URISyntaxException, StorageException, IOException {
        container = BlobTestHelper.getRandomContainerReference();
        container.create();

        blob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(container, BlobType.BLOCK_BLOB, "test", 100, null);
    }

    @After
    public void leaseTestMethodTearDown() throws StorageException {
        container.deleteIfExists();
    }

    @Test
    @Category({ SecondaryTests.class, SlowTests.class })
    public void testContainerSaS() throws IllegalArgumentException, StorageException, URISyntaxException,
            InvalidKeyException, InterruptedException {
        SharedAccessBlobPolicy sp1 = createSharedAccessPolicy(
                EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE,
                        SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.DELETE), 300);
        SharedAccessBlobPolicy sp2 = createSharedAccessPolicy(
                EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.LIST), 300);
        BlobContainerPermissions perms = new BlobContainerPermissions();

        perms.getSharedAccessPolicies().put("full", sp1);
        perms.getSharedAccessPolicies().put("readlist", sp2);
        container.uploadPermissions(perms);
        Thread.sleep(30000);

        String containerReadListSas = container.generateSharedAccessSignature(sp2, null);
        CloudBlobContainer readListContainer = new CloudBlobContainer(PathUtility.addToQuery(container.getUri(),
                containerReadListSas));

        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), readListContainer.getServiceClient()
                .getCredentials().getClass().toString());

        CloudBlockBlob blobFromSasContainer = readListContainer.getBlockBlobReference(blob.getName());
        blobFromSasContainer.download(new ByteArrayOutputStream());

        // do not give the client and check that the new container's client has the correct perms
        CloudBlobContainer containerFromUri = new CloudBlobContainer(PathUtility.addToQuery(
                readListContainer.getStorageUri(), container.generateSharedAccessSignature(null, "readlist")));
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), containerFromUri.getServiceClient()
                .getCredentials().getClass().toString());

        // pass in a client which will have different permissions and check the sas permissions are used
        // and that the properties set in the old service client are passed to the new client
        CloudBlobClient bClient = container.getServiceClient();

        // set some arbitrary settings to make sure they are passed on
        bClient.getDefaultRequestOptions().setConcurrentRequestCount(5);
        bClient.setDirectoryDelimiter("%");
        bClient.getDefaultRequestOptions().setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        bClient.getDefaultRequestOptions().setSingleBlobPutThresholdInBytes(5 * Constants.MB);
        bClient.getDefaultRequestOptions().setTimeoutIntervalInMs(1000);
        bClient.getDefaultRequestOptions().setRetryPolicyFactory(new RetryNoRetry());

        containerFromUri = new CloudBlobContainer(PathUtility.addToQuery(readListContainer.getStorageUri(),
                container.generateSharedAccessSignature(null, "readlist")), container.getServiceClient());
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), containerFromUri.getServiceClient()
                .getCredentials().getClass().toString());

        assertEquals(bClient.getDefaultRequestOptions().getConcurrentRequestCount(), containerFromUri
                .getServiceClient().getDefaultRequestOptions().getConcurrentRequestCount());
        assertEquals(bClient.getDirectoryDelimiter(), containerFromUri.getServiceClient().getDirectoryDelimiter());
        assertEquals(bClient.getDefaultRequestOptions().getLocationMode(), containerFromUri.getServiceClient()
                .getDefaultRequestOptions().getLocationMode());
        assertEquals(bClient.getDefaultRequestOptions().getSingleBlobPutThresholdInBytes(), containerFromUri
                .getServiceClient().getDefaultRequestOptions().getSingleBlobPutThresholdInBytes());
        assertEquals(bClient.getDefaultRequestOptions().getTimeoutIntervalInMs(), containerFromUri.getServiceClient()
                .getDefaultRequestOptions().getTimeoutIntervalInMs());
        assertEquals(bClient.getDefaultRequestOptions().getRetryPolicyFactory().getClass(), containerFromUri
                .getServiceClient().getDefaultRequestOptions().getRetryPolicyFactory().getClass());
    }

    @Test
    @Category(SlowTests.class)
    public void testContainerUpdateSAS() throws InvalidKeyException, StorageException, IOException, URISyntaxException,
            InterruptedException {
        //Create a policy with read/write access and get SAS.
        SharedAccessBlobPolicy policy = createSharedAccessPolicy(
                EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE), 300);
        BlobContainerPermissions perms = new BlobContainerPermissions();

        perms.getSharedAccessPolicies().put("readwrite", policy);
        container.uploadPermissions(perms);
        Thread.sleep(30000);

        String sasToken = container.generateSharedAccessSignature(policy, null);

        CloudBlockBlob blob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(container, BlobType.BLOCK_BLOB,
                "blockblob", 64, null);
        testAccess(sasToken, EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE), null,
                container, blob);

        //Change the policy to only read and update SAS.
        SharedAccessBlobPolicy policy2 = createSharedAccessPolicy(EnumSet.of(SharedAccessBlobPermissions.READ), 300);
        perms = new BlobContainerPermissions();

        perms.getSharedAccessPolicies().put("read", policy2);
        container.uploadPermissions(perms);
        Thread.sleep(30000);

        // Extra check to make sure that we have actually updated the SAS token.
        String sasToken2 = container.generateSharedAccessSignature(policy2, null);
        CloudBlobContainer sasContainer = new CloudBlobContainer(PathUtility.addToQuery(container.getUri(), sasToken2));

        try {
            BlobTestHelper.uploadNewBlob(sasContainer, BlobType.BLOCK_BLOB, "blockblob", 64, null);
            Assert.fail();
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
        }
    }

    @Test
    @Category(SlowTests.class)
    public void testContainerSASCombinations() throws StorageException, URISyntaxException, IOException,
            InvalidKeyException, InterruptedException {
        for (int i = 1; i < 16; i++) {
            final EnumSet<SharedAccessBlobPermissions> permissions = EnumSet.noneOf(SharedAccessBlobPermissions.class);

            if ((i & 0x1) == 0x1) {
                permissions.add(SharedAccessBlobPermissions.READ);
            }

            if ((i & 0x2) == 0x2) {
                permissions.add(SharedAccessBlobPermissions.WRITE);
            }

            if ((i & 0x4) == 0x4) {
                permissions.add(SharedAccessBlobPermissions.DELETE);
            }

            if ((i & 0x8) == 0x8) {
                permissions.add(SharedAccessBlobPermissions.LIST);
            }

            SharedAccessBlobPolicy policy = createSharedAccessPolicy(permissions, 300);

            BlobContainerPermissions perms = new BlobContainerPermissions();

            perms.getSharedAccessPolicies().put("readwrite" + i, policy);
            container.uploadPermissions(perms);
            Thread.sleep(30000);

            String sasToken = container.generateSharedAccessSignature(policy, null);

            CloudBlockBlob testBlockBlob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(container,
                    BlobType.BLOCK_BLOB, "blockblob", 64, null);
            testAccess(sasToken, permissions, null, container, testBlockBlob);

            CloudPageBlob testPageBlob = (CloudPageBlob) BlobTestHelper.uploadNewBlob(container, BlobType.PAGE_BLOB,
                    "pageblob", 512, null);

            testAccess(sasToken, permissions, null, container, testPageBlob);
        }
    }

    @Test
    @Category(SlowTests.class)
    public void testContainerPublicAccess() throws StorageException, IOException, URISyntaxException,
            InterruptedException {
        CloudBlockBlob testBlockBlob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(container, BlobType.BLOCK_BLOB,
                "blockblob", 64, null);
        CloudPageBlob testPageBlob = (CloudPageBlob) BlobTestHelper.uploadNewBlob(container, BlobType.PAGE_BLOB,
                "pageblob", 512, null);

        BlobContainerPermissions permissions = new BlobContainerPermissions();

        permissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
        container.uploadPermissions(permissions);
        Thread.sleep(35000);

        testAccess(null, EnumSet.of(SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.READ), null,
                container, testBlockBlob);
        testAccess(null, EnumSet.of(SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.READ), null,
                container, testPageBlob);

        permissions.setPublicAccess(BlobContainerPublicAccessType.BLOB);
        container.uploadPermissions(permissions);
        Thread.sleep(30000);
        testAccess(null, EnumSet.of(SharedAccessBlobPermissions.READ), null, container, testBlockBlob);
        testAccess(null, EnumSet.of(SharedAccessBlobPermissions.READ), null, container, testPageBlob);
    }

    @Test
    public void testBlockBlobSASCombinations() throws URISyntaxException, StorageException, InvalidKeyException,
            IOException {
        for (int i = 1; i < 8; i++) {
            final EnumSet<SharedAccessBlobPermissions> permissions = EnumSet.noneOf(SharedAccessBlobPermissions.class);
            addPermissions(permissions, i);
            testBlobAccess(container, BlobType.BLOCK_BLOB, permissions, null);
        }
    }

    @Test
    public void testPageBlobSASCombinations() throws InvalidKeyException, StorageException, IOException,
            URISyntaxException {
        for (int i = 1; i < 8; i++) {
            final EnumSet<SharedAccessBlobPermissions> permissions = EnumSet.noneOf(SharedAccessBlobPermissions.class);
            addPermissions(permissions, i);
            testBlobAccess(container, BlobType.PAGE_BLOB, permissions, null);
        }
    }

    @Test
    @Category(SlowTests.class)
    public void testBlobSaS() throws InvalidKeyException, IllegalArgumentException, StorageException,
            URISyntaxException, InterruptedException {
        SharedAccessBlobPolicy sp = createSharedAccessPolicy(
                EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.LIST), 300);
        BlobContainerPermissions perms = new BlobContainerPermissions();

        perms.getSharedAccessPolicies().put("readperm", sp);
        container.uploadPermissions(perms);
        Thread.sleep(30000);

        CloudBlockBlob sasBlob = new CloudBlockBlob(new URI(blob.getUri().toString() + "?"
                + blob.generateSharedAccessSignature(null, "readperm")));
        sasBlob.download(new ByteArrayOutputStream());

        // do not give the client and check that the new blob's client has the correct perms
        CloudBlob blobFromUri = new CloudBlockBlob(PathUtility.addToQuery(blob.getStorageUri(),
                blob.generateSharedAccessSignature(null, "readperm")), null);
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), blobFromUri.getServiceClient()
                .getCredentials().getClass().toString());

        // pass in a client which will have different permissions and check the sas permissions are used
        // and that the properties set in the old service client are passed to the new client
        CloudBlobClient bClient = sasBlob.getServiceClient();

        // set some arbitrary settings to make sure they are passed on
        bClient.getDefaultRequestOptions().setConcurrentRequestCount(5);
        bClient.setDirectoryDelimiter("%");
        bClient.getDefaultRequestOptions().setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        bClient.getDefaultRequestOptions().setSingleBlobPutThresholdInBytes(5 * Constants.MB);
        bClient.getDefaultRequestOptions().setTimeoutIntervalInMs(1000);
        bClient.getDefaultRequestOptions().setRetryPolicyFactory(new RetryNoRetry());

        blobFromUri = new CloudBlockBlob(PathUtility.addToQuery(blob.getStorageUri(),
                blob.generateSharedAccessSignature(null, "readperm")), bClient);
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), blobFromUri.getServiceClient()
                .getCredentials().getClass().toString());

        assertEquals(bClient.getDefaultRequestOptions().getConcurrentRequestCount(), blobFromUri.getServiceClient()
                .getDefaultRequestOptions().getConcurrentRequestCount());
        assertEquals(bClient.getDirectoryDelimiter(), blobFromUri.getServiceClient().getDirectoryDelimiter());
        assertEquals(bClient.getDefaultRequestOptions().getLocationMode(), blobFromUri.getServiceClient()
                .getDefaultRequestOptions().getLocationMode());
        assertEquals(bClient.getDefaultRequestOptions().getSingleBlobPutThresholdInBytes(), blobFromUri
                .getServiceClient().getDefaultRequestOptions().getSingleBlobPutThresholdInBytes());
        assertEquals(bClient.getDefaultRequestOptions().getTimeoutIntervalInMs(), blobFromUri.getServiceClient()
                .getDefaultRequestOptions().getTimeoutIntervalInMs());
        assertEquals(bClient.getDefaultRequestOptions().getRetryPolicyFactory().getClass(), blobFromUri
                .getServiceClient().getDefaultRequestOptions().getRetryPolicyFactory().getClass());
    }

    @Test
    @Category(SlowTests.class)
    public void testBlobSaSWithSharedAccessBlobHeaders() throws InvalidKeyException, IllegalArgumentException,
            StorageException, URISyntaxException, InterruptedException {
        SharedAccessBlobPolicy sp = createSharedAccessPolicy(EnumSet.of(SharedAccessBlobPermissions.READ,
                SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.LIST), 300);
        BlobContainerPermissions perms = new BlobContainerPermissions();

        perms.getSharedAccessPolicies().put("readperm", sp);
        container.uploadPermissions(perms);
        Thread.sleep(30000);

        SharedAccessBlobHeaders headers = new SharedAccessBlobHeaders();
        headers.setCacheControl("no-cache");
        headers.setContentDisposition("attachment; filename=\"fname.ext\"");
        headers.setContentEncoding("gzip");
        headers.setContentLanguage("da");
        headers.setContentType("text/html; charset=utf-8");

        CloudBlockBlob sasBlob = new CloudBlockBlob(new URI(blob.getUri().toString() + "?"
                + blob.generateSharedAccessSignature(null, headers, "readperm")));
        OperationContext context = new OperationContext();

        context.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection connection = (HttpURLConnection) eventArg.getConnectionObject();
                assertEquals("no-cache", connection.getHeaderField(Constants.HeaderConstants.CACHE_CONTROL));
                assertEquals("attachment; filename=\"fname.ext\"",
                        connection.getHeaderField(Constants.HeaderConstants.CONTENT_DISPOSITION));
                assertEquals("gzip", connection.getHeaderField(Constants.HeaderConstants.CONTENT_ENCODING));
                assertEquals("da", connection.getHeaderField(Constants.HeaderConstants.CONTENT_LANGUAGE));
                assertEquals("text/html; charset=utf-8",
                        connection.getHeaderField(Constants.HeaderConstants.CONTENT_TYPE));
            }
        });

        sasBlob.download(new ByteArrayOutputStream(), null, null, context);
    }

    private final static SharedAccessBlobPolicy createSharedAccessPolicy(EnumSet<SharedAccessBlobPermissions> sap,
            int expireTimeInSeconds) {

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.add(Calendar.SECOND, expireTimeInSeconds);
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        policy.setPermissions(sap);
        policy.setSharedAccessExpiryTime(cal.getTime());
        return policy;

    }

    @SuppressWarnings("unused")
    private static void testAccess(String sasToken, EnumSet<SharedAccessBlobPermissions> permissions,
            SharedAccessBlobHeaders headers, CloudBlobContainer container, CloudBlob blob) throws StorageException,
            URISyntaxException {
        StorageCredentials credentials = new StorageCredentialsSharedAccessSignature(sasToken);

        if (container != null) {
            container = new CloudBlobContainer(credentials.transformUri(container.getUri()));
            if (blob.getProperties().getBlobType() == BlobType.BLOCK_BLOB) {
                blob = container.getBlockBlobReference(blob.getName());
            }
            else {
                blob = container.getPageBlobReference(blob.getName());
            }
        }
        else {
            if (blob.getProperties().getBlobType() == BlobType.BLOCK_BLOB) {
                blob = new CloudBlockBlob(credentials.transformUri(blob.getUri()));
            }
            else {
                blob = new CloudPageBlob(credentials.transformUri(blob.getUri()));
            }
        }

        if (container != null) {
            if (permissions.contains(SharedAccessBlobPermissions.LIST)) {
                for (ListBlobItem listedBlob : container.listBlobs());
            }
            else {
                try {
                    for (ListBlobItem listedBlob : container.listBlobs());
                    Assert.fail();
                }
                catch (NoSuchElementException ex) {
                    Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                            ((StorageException) ex.getCause()).getHttpStatusCode());
                }
            }
        }

        if (permissions.contains(SharedAccessBlobPermissions.READ)) {
            blob.downloadAttributes();

            // Test headers
            if (headers != null) {
                if (headers.getCacheControl() != null) {
                    Assert.assertEquals(headers.getCacheControl(), blob.getProperties().getCacheControl());
                }

                if (headers.getContentDisposition() != null) {
                    Assert.assertEquals(headers.getContentDisposition(), blob.getProperties().getContentDisposition());
                }

                if (headers.getContentEncoding() != null) {
                    Assert.assertEquals(headers.getContentEncoding(), blob.getProperties().getContentEncoding());
                }

                if (headers.getContentLanguage() != null) {
                    Assert.assertEquals(headers.getContentLanguage(), blob.getProperties().getContentLanguage());
                }

                if (headers.getContentType() != null) {
                    Assert.assertEquals(headers.getContentType(), blob.getProperties().getContentType());
                }
            }
        }
        else {
            try {
                blob.downloadAttributes();
                Assert.fail();
            }
            catch (StorageException ex) {
                Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            }
        }

        if (permissions.contains(SharedAccessBlobPermissions.WRITE)) {
            blob.uploadMetadata();
        }
        else {
            try {
                blob.uploadMetadata();
                Assert.fail();
            }
            catch (StorageException ex) {
                Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            }
        }

        if (permissions.contains(SharedAccessBlobPermissions.DELETE)) {
            blob.delete();
        }
        else {
            try {
                blob.delete();
                Assert.fail();
            }
            catch (StorageException ex) {
                Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            }
        }
    }

    private static void testBlobAccess(CloudBlobContainer container, BlobType type,
            EnumSet<SharedAccessBlobPermissions> permissions, SharedAccessBlobHeaders headers) throws StorageException,
            IOException, URISyntaxException, InvalidKeyException {
        CloudBlob blob = BlobTestHelper.uploadNewBlob(container, type, "blob", 512, null);

        SharedAccessBlobPolicy policy = createSharedAccessPolicy(permissions, 300);

        String sasToken = blob.generateSharedAccessSignature(policy, headers, null);
        testAccess(sasToken, permissions, headers, null, blob);
    }

    private static void addPermissions(EnumSet<SharedAccessBlobPermissions> permissions, int i) {
        if ((i & 0x1) == 0x1) {
            permissions.add(SharedAccessBlobPermissions.READ);
        }

        if ((i & 0x2) == 0x2) {
            permissions.add(SharedAccessBlobPermissions.WRITE);
        }

        if ((i & 0x4) == 0x4) {
            permissions.add(SharedAccessBlobPermissions.DELETE);
        }
    }
}
