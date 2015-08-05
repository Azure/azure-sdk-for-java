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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResponseReceivedEvent;
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
    public void blobSasTestMethodSetup() throws URISyntaxException, StorageException, IOException {
        this.container = BlobTestHelper.getRandomContainerReference();
        this.container.create();

        this.blob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, "test", 100,
                null);
    }

    @After
    public void blobSasTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }
    
    @Test
    public void testApiVersion() throws InvalidKeyException, StorageException, URISyntaxException {
        SharedAccessBlobPolicy sp1 = createSharedAccessPolicy(
                EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE,
                        SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.DELETE), 300);
    	String sas = this.blob.generateSharedAccessSignature(sp1, null);
    	
    	// should not be appended before signing
    	assertEquals(-1, sas.indexOf(Constants.QueryConstants.API_VERSION));
    	
        OperationContext ctx = new OperationContext();
        ctx.getResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {

            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
            	// should be appended after signing
            	HttpURLConnection conn = (HttpURLConnection) eventArg.getConnectionObject();
            	assertTrue(conn.getURL().toString().indexOf(Constants.QueryConstants.API_VERSION) != -1);
            }
        });

        CloudBlockBlob sasBlob = new CloudBlockBlob(new URI(this.blob.getUri().toString() + "?" + sas));
        sasBlob.uploadMetadata(null, null, ctx);	    	
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
        this.container.uploadPermissions(perms);
        Thread.sleep(30000);

        String containerReadListSas = this.container.generateSharedAccessSignature(sp2, null);
        CloudBlobContainer readListContainer = new CloudBlobContainer(PathUtility.addToQuery(this.container.getUri(),
                containerReadListSas));

        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), readListContainer.getServiceClient()
                .getCredentials().getClass().toString());

        CloudBlockBlob blobFromSasContainer = readListContainer.getBlockBlobReference(this.blob.getName());
        blobFromSasContainer.download(new ByteArrayOutputStream());

        // do not give the client and check that the new container's client has the correct perms
        CloudBlobContainer containerFromUri = new CloudBlobContainer(PathUtility.addToQuery(
                readListContainer.getStorageUri(), this.container.generateSharedAccessSignature(null, "readlist")));
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), containerFromUri.getServiceClient()
                .getCredentials().getClass().toString());
        
        // create credentials from sas
        StorageCredentials creds = new StorageCredentialsSharedAccessSignature(
                this.container.generateSharedAccessSignature(null, "readlist"));
        CloudBlobClient bClient = new CloudBlobClient(this.container.getServiceClient().getStorageUri(), creds);

        CloudBlobContainer containerFromClient = bClient.getContainerReference(this.container.getName());
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), containerFromClient.getServiceClient()
                .getCredentials().getClass().toString());
        assertEquals(bClient, containerFromClient.getServiceClient());
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
        this.container.uploadPermissions(perms);
        Thread.sleep(30000);

        String sasToken = this.container.generateSharedAccessSignature(policy, null);

        CloudBlockBlob blob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB,
                "blockblob", 64, null);
        testAccess(sasToken, EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE), null,
                this.container, blob);

        //Change the policy to only read and update SAS.
        SharedAccessBlobPolicy policy2 = createSharedAccessPolicy(EnumSet.of(SharedAccessBlobPermissions.READ), 300);
        perms = new BlobContainerPermissions();

        perms.getSharedAccessPolicies().put("read", policy2);
        this.container.uploadPermissions(perms);
        Thread.sleep(30000);

        // Extra check to make sure that we have actually updated the SAS token.
        String sasToken2 = this.container.generateSharedAccessSignature(policy2, null);
        CloudBlobContainer sasContainer = new CloudBlobContainer(PathUtility.addToQuery(this.container.getUri(),
                sasToken2));

        try {
            BlobTestHelper.uploadNewBlob(sasContainer, BlobType.BLOCK_BLOB, "blockblob", 64, null);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
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
            this.container.uploadPermissions(perms);
            Thread.sleep(30000);

            String sasToken = this.container.generateSharedAccessSignature(policy, null);

            CloudBlockBlob testBlockBlob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container,
                    BlobType.BLOCK_BLOB, "blockblob", 64, null);
            testAccess(sasToken, permissions, null, this.container, testBlockBlob);

            CloudPageBlob testPageBlob = (CloudPageBlob) BlobTestHelper.uploadNewBlob(this.container,
                    BlobType.PAGE_BLOB, "pageblob", 512, null);

            testAccess(sasToken, permissions, null, this.container, testPageBlob);
        }
    }

    @Test
    @Category(SlowTests.class)
    public void testContainerPublicAccess() throws StorageException, IOException, URISyntaxException,
            InterruptedException {
        CloudBlockBlob testBlockBlob = (CloudBlockBlob) BlobTestHelper.uploadNewBlob(this.container,
                BlobType.BLOCK_BLOB, "blockblob", 64, null);
        CloudPageBlob testPageBlob = (CloudPageBlob) BlobTestHelper.uploadNewBlob(this.container, BlobType.PAGE_BLOB,
                "pageblob", 512, null);

        BlobContainerPermissions permissions = new BlobContainerPermissions();

        permissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
        this.container.uploadPermissions(permissions);
        Thread.sleep(35000);

        testAccess(null, EnumSet.of(SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.READ), null,
                this.container, testBlockBlob);
        testAccess(null, EnumSet.of(SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.READ), null,
                this.container, testPageBlob);

        permissions.setPublicAccess(BlobContainerPublicAccessType.BLOB);
        this.container.uploadPermissions(permissions);
        Thread.sleep(30000);
        testAccess(null, EnumSet.of(SharedAccessBlobPermissions.READ), null, this.container, testBlockBlob);
        testAccess(null, EnumSet.of(SharedAccessBlobPermissions.READ), null, this.container, testPageBlob);
    }

    @Test
    public void testBlockBlobSASCombinations() throws URISyntaxException, StorageException, InvalidKeyException,
            IOException {
        for (int i = 1; i < 8; i++) {
            final EnumSet<SharedAccessBlobPermissions> permissions = EnumSet.noneOf(SharedAccessBlobPermissions.class);
            addPermissions(permissions, i);
            testBlobAccess(this.container, BlobType.BLOCK_BLOB, permissions, null);
        }
    }

    @Test
    public void testPageBlobSASCombinations() throws InvalidKeyException, StorageException, IOException,
            URISyntaxException {
        for (int i = 1; i < 8; i++) {
            final EnumSet<SharedAccessBlobPermissions> permissions = EnumSet.noneOf(SharedAccessBlobPermissions.class);
            addPermissions(permissions, i);
            testBlobAccess(this.container, BlobType.PAGE_BLOB, permissions, null);
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
        this.container.uploadPermissions(perms);
        Thread.sleep(30000);

        CloudBlockBlob sasBlob = new CloudBlockBlob(new URI(this.blob.getUri().toString() + "?"
                + this.blob.generateSharedAccessSignature(null, "readperm")));
        sasBlob.download(new ByteArrayOutputStream());

        // do not give the client and check that the new blob's client has the correct perms
        CloudBlob blobFromUri = new CloudBlockBlob(PathUtility.addToQuery(this.blob.getStorageUri(),
                this.blob.generateSharedAccessSignature(null, "readperm")));
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), blobFromUri.getServiceClient()
                .getCredentials().getClass().toString());

        // create credentials from sas
        StorageCredentials creds = new StorageCredentialsSharedAccessSignature(
                this.blob.generateSharedAccessSignature(null, "readperm"));
        CloudBlobClient bClient = new CloudBlobClient(sasBlob.getServiceClient().getStorageUri(), creds);

        CloudBlockBlob blobFromClient = bClient.getContainerReference(this.blob.getContainer().getName())
                .getBlockBlobReference(this.blob.getName());
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), blobFromClient.getServiceClient()
                .getCredentials().getClass().toString());
        assertEquals(bClient, blobFromClient.getServiceClient());
    }

    @Test
    @Category(SlowTests.class)
    public void testBlobSaSWithSharedAccessBlobHeaders() throws InvalidKeyException, IllegalArgumentException,
            StorageException, URISyntaxException, InterruptedException {
        SharedAccessBlobPolicy sp = createSharedAccessPolicy(EnumSet.of(SharedAccessBlobPermissions.READ,
                SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.LIST), 300);
        BlobContainerPermissions perms = new BlobContainerPermissions();

        perms.getSharedAccessPolicies().put("readperm", sp);
        this.container.uploadPermissions(perms);
        Thread.sleep(30000);

        SharedAccessBlobHeaders headers = new SharedAccessBlobHeaders();
        headers.setCacheControl("no-cache");
        headers.setContentDisposition("attachment; filename=\"fname.ext\"");
        headers.setContentEncoding("gzip");
        headers.setContentLanguage("da");
        headers.setContentType("text/html; charset=utf-8");

        CloudBlockBlob sasBlob = new CloudBlockBlob(new URI(this.blob.getUri().toString() + "?"
                + this.blob.generateSharedAccessSignature(null, headers, "readperm")));
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
                    fail();
                }
                catch (NoSuchElementException ex) {
                    assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                            ((StorageException) ex.getCause()).getHttpStatusCode());
                }
            }
        }

        if (permissions.contains(SharedAccessBlobPermissions.READ)) {
            blob.downloadAttributes();

            // Test headers
            if (headers != null) {
                if (headers.getCacheControl() != null) {
                    assertEquals(headers.getCacheControl(), blob.getProperties().getCacheControl());
                }

                if (headers.getContentDisposition() != null) {
                    assertEquals(headers.getContentDisposition(), blob.getProperties().getContentDisposition());
                }

                if (headers.getContentEncoding() != null) {
                    assertEquals(headers.getContentEncoding(), blob.getProperties().getContentEncoding());
                }

                if (headers.getContentLanguage() != null) {
                    assertEquals(headers.getContentLanguage(), blob.getProperties().getContentLanguage());
                }

                if (headers.getContentType() != null) {
                    assertEquals(headers.getContentType(), blob.getProperties().getContentType());
                }
            }
        }
        else {
            try {
                blob.downloadAttributes();
                fail();
            }
            catch (StorageException ex) {
                assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            }
        }

        if (permissions.contains(SharedAccessBlobPermissions.WRITE)) {
            blob.uploadMetadata();
        }
        else {
            try {
                blob.uploadMetadata();
                fail();
            }
            catch (StorageException ex) {
                assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            }
        }

        if (permissions.contains(SharedAccessBlobPermissions.DELETE)) {
            blob.delete();
        }
        else {
            try {
                blob.delete();
                fail();
            }
            catch (StorageException ex) {
                assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
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
