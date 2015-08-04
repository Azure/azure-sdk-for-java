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
import com.microsoft.azure.storage.TestHelper;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;
import com.microsoft.azure.storage.core.PathUtility;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class FileSasTests {

    protected static CloudFileClient fileClient = null;
    protected CloudFileShare share;
    protected CloudFile file;

    @Before
    public void fileSASTestMethodSetup() throws URISyntaxException, StorageException, IOException {
        if (fileClient == null) {
            fileClient = TestHelper.createCloudFileClient();
        }
        this.share = FileTestHelper.getRandomShareReference();
        this.share.create();

        this.file = (CloudFile) FileTestHelper.uploadNewFile(this.share, 100, null);
    }

    @After
    public void fileSASTestMethodTearDown() throws StorageException {
        this.share.deleteIfExists();
    }
    
    @Test
    public void testApiVersion() throws InvalidKeyException, StorageException, URISyntaxException {
        SharedAccessFilePolicy policy = createSharedAccessPolicy(
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE,
                        SharedAccessFilePermissions.LIST, SharedAccessFilePermissions.DELETE), 300);
        String sas = this.file.generateSharedAccessSignature(policy, null);
        
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

        CloudFile sasFile = new CloudFile(new URI(this.file.getUri().toString() + "?" + sas));
        sasFile.uploadMetadata(null, null, ctx);
    }
    
    @Test
    public void testDirectorySas() throws InvalidKeyException, IllegalArgumentException, StorageException,
            URISyntaxException, InterruptedException {
        CloudFileDirectory dir = this.share.getRootDirectoryReference().getDirectoryReference("dirFile");
        CloudFile file = dir.getFileReference("dirFile");

        dir.create();
        file.create(512);

        SharedAccessFilePolicy policy = createSharedAccessPolicy(
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.LIST), 300);

        // Test directory SAS with a file SAS token from an identically named file
        String sas = file.generateSharedAccessSignature(policy, null);
        CloudFileDirectory sasDir = new CloudFileDirectory(new URI(dir.getUri().toString() + "?" + sas));
        try {
            sasDir.downloadAttributes();
            fail("This should result in an authentication error.");
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
        }

        // Test directory SAS with a share SAS token
        sas = this.share.generateSharedAccessSignature(policy, null);
        sasDir = new CloudFileDirectory(new URI(dir.getUri().toString() + "?" + sas));
        sasDir.downloadAttributes();
    }

    @Test
    @Category({ SecondaryTests.class, SlowTests.class })
    public void testShareSAS()throws IllegalArgumentException, StorageException, URISyntaxException,
            InvalidKeyException, InterruptedException {
        SharedAccessFilePolicy policy1 = createSharedAccessPolicy(
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE,
                SharedAccessFilePermissions.LIST, SharedAccessFilePermissions.DELETE), 300);
        SharedAccessFilePolicy policy2 = createSharedAccessPolicy(
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.LIST), 300);
        FileSharePermissions permissions = new FileSharePermissions();

        permissions.getSharedAccessPolicies().put("full", policy1);
        permissions.getSharedAccessPolicies().put("readlist", policy2);
        this.share.uploadPermissions(permissions);
        Thread.sleep(30000);

        String shareReadListSas = this.share.generateSharedAccessSignature(policy2, null);
        CloudFileShare readListShare =
                new CloudFileShare(PathUtility.addToQuery(this.share.getUri(), shareReadListSas));

        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                readListShare.getServiceClient().getCredentials().getClass().toString());

        CloudFile fileFromSasShare = readListShare.getRootDirectoryReference().getFileReference(this.file.getName());
        fileFromSasShare.download(new ByteArrayOutputStream());

        // do not give the client and check that the new share's client has the correct perms
        CloudFileShare shareFromUri = new CloudFileShare(PathUtility.addToQuery(
                readListShare.getStorageUri(), this.share.generateSharedAccessSignature(null, "readlist")));
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                shareFromUri.getServiceClient().getCredentials().getClass().toString());

        // create credentials from sas
        StorageCredentials creds = new StorageCredentialsSharedAccessSignature(
                this.share.generateSharedAccessSignature(null, "readlist"));
        CloudFileClient client = new CloudFileClient(this.share.getServiceClient().getStorageUri(), creds);

        CloudFileShare shareFromClient = client.getShareReference(readListShare.getName());
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                shareFromClient.getServiceClient().getCredentials().getClass().toString());
        assertEquals(client, shareFromClient.getServiceClient());
    }

    @Test
    @Category(SlowTests.class)
    public void testShareUpdateSAS()
            throws InvalidKeyException, StorageException, IOException, URISyntaxException, InterruptedException {
        // Create a policy with read/write access and get SAS.
        SharedAccessFilePolicy policy = createSharedAccessPolicy(
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE), 300);
        FileSharePermissions permissions = new FileSharePermissions();

        permissions.getSharedAccessPolicies().put("readwrite", policy);
        this.share.uploadPermissions(permissions);
        Thread.sleep(30000);

        String sasToken = this.share.generateSharedAccessSignature(policy, null);

        CloudFile file = FileTestHelper.uploadNewFile(this.share, 64, null);
        testAccess(sasToken, EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE),
                this.share, file);

        //Change the policy to only read and update SAS.
        SharedAccessFilePolicy policy2 = createSharedAccessPolicy(EnumSet.of(SharedAccessFilePermissions.READ), 300);
        permissions = new FileSharePermissions();

        permissions.getSharedAccessPolicies().put("read", policy2);
        this.share.uploadPermissions(permissions);
        Thread.sleep(30000);

        // Extra check to make sure that we have actually updated the SAS token.
        String sasToken2 = this.share.generateSharedAccessSignature(policy2, null);
        CloudFileShare sasShare = new CloudFileShare(PathUtility.addToQuery(this.share.getUri(), sasToken2));

        try {
            FileTestHelper.uploadNewFile(sasShare, 64, null);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
        }
    }

    @Test
    @Category(SlowTests.class)
    public void testShareSASCombinations()
            throws StorageException, URISyntaxException, IOException, InvalidKeyException, InterruptedException {
        for (int bits = 1; bits < 16; bits++) {
            final EnumSet<SharedAccessFilePermissions> permissionSet = EnumSet.noneOf(SharedAccessFilePermissions.class);

            if ((bits & 0x1) == 0x1) {
                permissionSet.add(SharedAccessFilePermissions.READ);
            }

            if ((bits & 0x2) == 0x2) {
                permissionSet.add(SharedAccessFilePermissions.WRITE);
            }

            if ((bits & 0x4) == 0x4) {
                permissionSet.add(SharedAccessFilePermissions.DELETE);
            }

            if ((bits & 0x8) == 0x8) {
                permissionSet.add(SharedAccessFilePermissions.LIST);
            }

            SharedAccessFilePolicy policy = createSharedAccessPolicy(permissionSet, 300);

            FileSharePermissions permissions = new FileSharePermissions();

            permissions.getSharedAccessPolicies().put("readwrite" + bits, policy);
            this.share.uploadPermissions(permissions);
            Thread.sleep(30000);

            String sasToken = this.share.generateSharedAccessSignature(policy, null);

            CloudFile testFile = FileTestHelper.uploadNewFile(this.share, 64, null);
            testAccess(sasToken, permissionSet, this.share, testFile);
        }
    }

    @Test
    public void testFileSASCombinations() throws URISyntaxException, StorageException, InvalidKeyException, IOException {
        for (int bits = 1; bits < 8; bits++) {
            EnumSet<SharedAccessFilePermissions> permissionSet = EnumSet.noneOf(SharedAccessFilePermissions.class);
            
            if ((bits & 0x1) == 0x1) {
                permissionSet.add(SharedAccessFilePermissions.READ);
            }
            
            if ((bits & 0x2) == 0x2) {
                permissionSet.add(SharedAccessFilePermissions.WRITE);
            }
            
            if ((bits & 0x4) == 0x4) {
                permissionSet.add(SharedAccessFilePermissions.DELETE);
            }
            
            CloudFile testFile = FileTestHelper.uploadNewFile(this.share, 512, null);
            SharedAccessFilePolicy policy = createSharedAccessPolicy(permissionSet, 300);
            String sasToken = testFile.generateSharedAccessSignature(policy, null, null);

            testAccess(sasToken, permissionSet, null, testFile);
        }
    }
    
    @Test
    public void testFileSAS() throws InvalidKeyException, IllegalArgumentException, StorageException,
            URISyntaxException, InterruptedException {
        SharedAccessFilePolicy policy = createSharedAccessPolicy(
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.LIST), 300);
        FileSharePermissions perms = new FileSharePermissions();

        perms.getSharedAccessPolicies().put("readperm", policy);
        this.share.uploadPermissions(perms);
        Thread.sleep(30000);

        CloudFile sasFile = new CloudFile(
                new URI(this.file.getUri().toString() + "?" + this.file.generateSharedAccessSignature(null, "readperm")));
        sasFile.download(new ByteArrayOutputStream());

        // do not give the client and check that the new file's client has the correct permissions
        CloudFile fileFromUri = new CloudFile(PathUtility.addToQuery(this.file.getStorageUri(),
                this.file.generateSharedAccessSignature(null, "readperm")));
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                fileFromUri.getServiceClient().getCredentials().getClass().toString());
        
        // create credentials from sas
        StorageCredentials creds = new StorageCredentialsSharedAccessSignature(
                this.file.generateSharedAccessSignature(policy, null, null));
        CloudFileClient client = new CloudFileClient(sasFile.getServiceClient().getStorageUri(), creds);

        CloudFile fileFromClient = client.getShareReference(this.file.getShare().getName()).getRootDirectoryReference()
                .getFileReference(this.file.getName());
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                fileFromClient.getServiceClient().getCredentials().getClass().toString());
        assertEquals(client, fileFromClient.getServiceClient());
    }

    @Test
    public void testFileSASWithSharedAccessFileHeaders() throws InvalidKeyException, IllegalArgumentException,
            StorageException, URISyntaxException, InterruptedException {
        SharedAccessFilePolicy policy = createSharedAccessPolicy(EnumSet.of(SharedAccessFilePermissions.READ,
                SharedAccessFilePermissions.WRITE, SharedAccessFilePermissions.LIST), 300);
        FileSharePermissions perms = new FileSharePermissions();

        perms.getSharedAccessPolicies().put("rwperm", policy);
        this.share.uploadPermissions(perms);
        Thread.sleep(30000);

        SharedAccessFileHeaders headers = new SharedAccessFileHeaders();
        headers.setCacheControl("no-cache");
        headers.setContentDisposition("attachment; filename=\"fname.ext\"");
        headers.setContentEncoding("gzip");
        headers.setContentLanguage("da");
        headers.setContentType("text/html; charset=utf-8");

        CloudFile sasFile = new CloudFile(
                new URI(this.file.getUri().toString() + "?" + this.file.generateSharedAccessSignature(null, headers, "rwperm")));
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

        sasFile.download(new ByteArrayOutputStream(), null, null, context);
    }

    private final static SharedAccessFilePolicy createSharedAccessPolicy(EnumSet<SharedAccessFilePermissions> sap,
            int expireTimeInSeconds) {

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, expireTimeInSeconds);
        SharedAccessFilePolicy policy = new SharedAccessFilePolicy();
        policy.setPermissions(sap);
        policy.setSharedAccessExpiryTime(calendar.getTime());
        return policy;
    }

    @SuppressWarnings("unused")
    private static void testAccess(
            String sasToken, EnumSet<SharedAccessFilePermissions> permissions, CloudFileShare share, CloudFile file)
            throws StorageException, URISyntaxException {
        StorageCredentials credentials = new StorageCredentialsSharedAccessSignature(sasToken);

        if (share != null) {
            share = new CloudFileShare(credentials.transformUri(share.getUri()));
            file = share.getRootDirectoryReference().getFileReference(file.getName());
        }
        else {
            file = new CloudFile(credentials.transformUri(file.getUri()));
        }

        if (share != null) {
            if (permissions.contains(SharedAccessFilePermissions.LIST)) {
                for (ListFileItem listedFile : share.getRootDirectoryReference().listFilesAndDirectories());
            }
            else {
                try {
                    for (ListFileItem listedFile : share.getRootDirectoryReference().listFilesAndDirectories());
                    fail();
                }
                catch (NoSuchElementException ex) {
                    assertEquals(
                            HttpURLConnection.HTTP_NOT_FOUND, ((StorageException) ex.getCause()).getHttpStatusCode());
                }
            }
        }

        if (permissions.contains(SharedAccessFilePermissions.READ)) {
            file.downloadAttributes();
        }
        else {
            try {
                file.downloadAttributes();
                fail();
            }
            catch (StorageException ex) {
                assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            }
        }

        if (permissions.contains(SharedAccessFilePermissions.WRITE)) {
            file.uploadMetadata();
        }
        else {
            try {
                file.uploadMetadata();
                fail();
            }
            catch (StorageException ex) {
                assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            }
        }

        if (permissions.contains(SharedAccessFilePermissions.DELETE)) {
            file.delete();
        }
        else {
            try {
                file.delete();
                fail();
            }
            catch (StorageException ex) {
                assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            }
        }
    }
}