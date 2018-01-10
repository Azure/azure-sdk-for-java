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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.IPRange;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResponseReceivedEvent;
import com.microsoft.azure.storage.SecondaryTests;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.SharedAccessAccountPermissions;
import com.microsoft.azure.storage.SharedAccessAccountPolicy;
import com.microsoft.azure.storage.SharedAccessAccountResourceType;
import com.microsoft.azure.storage.SharedAccessAccountService;
import com.microsoft.azure.storage.SharedAccessProtocols;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.TestHelper;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.BlobTestHelper;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.SR;

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
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.LIST, SharedAccessFilePermissions.DELETE), 300);

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

        // Test deleting a directory using a SAS token. The directory must be empty for this request to succeed.
        file.delete();
        sasDir.delete();
    }

    @Test
    @Category({ SecondaryTests.class })
    public void testIpAcl()
            throws StorageException, URISyntaxException, InvalidKeyException, InterruptedException, UnknownHostException {
        
        // Generate policies
        SharedAccessFilePolicy policy = createSharedAccessPolicy(
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.LIST), 300);
        IPRange range1 = new IPRange("0.0.0.0", "255.255.255.255");
        IPRange range2 = new IPRange("0.0.0.0");
        
        // Ensure access attempt from invalid IP fails
        IPRange sourceIP = null;
        try {
            String shareSasNone = this.share.generateSharedAccessSignature(policy, null, range2, null);
            CloudFileShare noneShare =
                    new CloudFileShare(PathUtility.addToQuery(this.share.getUri(), shareSasNone));
    
            assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                    noneShare.getServiceClient().getCredentials().getClass().toString());
    
            CloudFile noneFile = noneShare.getRootDirectoryReference().getFileReference(this.file.getName());
            noneFile.download(new ByteArrayOutputStream());
            fail();
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
        }
        
        // Ensure access attempt from the single allowed IP succeeds
        String shareSasOne = this.share.generateSharedAccessSignature(policy, null, sourceIP, null);
        CloudFileShare oneShare =
                new CloudFileShare(PathUtility.addToQuery(this.share.getUri(), shareSasOne));

        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                oneShare.getServiceClient().getCredentials().getClass().toString());

        CloudFile oneFile = oneShare.getRootDirectoryReference().getFileReference(this.file.getName());
        oneFile.download(new ByteArrayOutputStream());

        // Ensure access attempt from one of many valid IPs succeeds
        String shareSasAll = this.share.generateSharedAccessSignature(policy, null, range1, null);
        CloudFileShare allShare =
                new CloudFileShare(PathUtility.addToQuery(this.share.getUri(), shareSasAll));

        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                allShare.getServiceClient().getCredentials().getClass().toString());

        CloudFile allFile = allShare.getRootDirectoryReference().getFileReference(this.file.getName());
        allFile.download(new ByteArrayOutputStream());
    }

    @Test
    @Category({ SecondaryTests.class })
    public void testProtocolRestrictions()
            throws StorageException, URISyntaxException, InvalidKeyException, InterruptedException {
        
        // Generate policy
        SharedAccessFilePolicy policy = createSharedAccessPolicy(
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.LIST), 300);
        
        // Generate share SAS and URI
        String shareSasHttps =
                this.share.generateSharedAccessSignature(policy, null, null, SharedAccessProtocols.HTTPS_ONLY);
        String shareSasHttp =
                this.share.generateSharedAccessSignature(policy, null, null, SharedAccessProtocols.HTTPS_HTTP);
        final URI uri = this.share.getUri();
        
        // Ensure attempt from http fails against HTTPS_ONLY
        try {
            CloudFileShare httpShare =
                    new CloudFileShare(PathUtility.addToQuery(TestHelper.securePortUri(uri, false, 'f'), shareSasHttps));
            assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                    httpShare.getServiceClient().getCredentials().getClass().toString());
            CloudFile httpFile = httpShare.getRootDirectoryReference().getFileReference(this.file.getName());
            httpFile.download(new ByteArrayOutputStream());
            
            fail();
        }
        catch (StorageException ex) {
            assertEquals(Constants.HeaderConstants.HTTP_UNUSED_306, ex.getHttpStatusCode());
            assertEquals(SR.CANNOT_TRANSFORM_NON_HTTPS_URI_WITH_HTTPS_ONLY_CREDENTIALS, ex.getMessage());
        }

        // Ensure attempt from https succeeds against HTTPS_ONLY
        CloudFileShare httpsShare =
                new CloudFileShare(PathUtility.addToQuery(TestHelper.securePortUri(uri, true, 'f'), shareSasHttps));
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                httpsShare.getServiceClient().getCredentials().getClass().toString());
        CloudFile httpsFile = httpsShare.getRootDirectoryReference().getFileReference(this.file.getName());
        httpsFile.download(new ByteArrayOutputStream());
        
        // Ensure attempt from both http and https succeed against HTTPS_HTTP
        CloudFileShare httpShare =
                new CloudFileShare(PathUtility.addToQuery(TestHelper.securePortUri(uri, false, 'f'), shareSasHttp));
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                httpShare.getServiceClient().getCredentials().getClass().toString());
        CloudFile httpFile = httpShare.getRootDirectoryReference().getFileReference(this.file.getName());
        httpFile.download(new ByteArrayOutputStream());
        
        httpsShare =
                new CloudFileShare(PathUtility.addToQuery(TestHelper.securePortUri(uri, true, 'f'), shareSasHttp));
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
                httpsShare.getServiceClient().getCredentials().getClass().toString());
        httpsFile = httpsShare.getRootDirectoryReference().getFileReference(this.file.getName());
        httpsFile.download(new ByteArrayOutputStream());
    }

    @Test
    @Category({ SecondaryTests.class, SlowTests.class })
    public void testShareSAS() throws IllegalArgumentException, StorageException, URISyntaxException,
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
        testAccess(sasToken,
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE), this.share, file);

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
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
        }
    }

    @Test
    @Category(SlowTests.class)
    public void testShareSASCombinations()
            throws StorageException, URISyntaxException, IOException, InvalidKeyException, InterruptedException {

        EnumSet<SharedAccessFilePermissions> permissionSet = null;
        Map<Integer, CloudFileShare> shares = new HashMap<Integer, CloudFileShare>();

        try {
            for (int bits = 0x1; bits < 0x40; bits++) {
                shares.put(bits, FileTestHelper.getRandomShareReference());
                shares.get(bits).createIfNotExists();

                permissionSet = getPermissions(bits);
                FileSharePermissions perms = new FileSharePermissions();

                perms.getSharedAccessPolicies().put("readwrite" + bits, createSharedAccessPolicy(permissionSet, 300));
                shares.get(bits).uploadPermissions(perms);
            }

            Thread.sleep(30000);

            for (int bits = 0x1; bits < 0x20; bits++) {
                permissionSet = getPermissions(bits);
                String sasToken = shares.get(bits).generateSharedAccessSignature(null, "readwrite" + bits);

                CloudFile testFile = FileTestHelper.uploadNewFile(shares.get(bits), 64, null);
                testAccess(sasToken, permissionSet, shares.get(bits), testFile);
            }
        }
        finally {
            for (int bits = 0x1; bits < shares.size(); bits++) {
                shares.get(bits).deleteIfExists();
            }
        }
    }

    @Test
    public void testFileSASCombinations() throws URISyntaxException, StorageException, InvalidKeyException, IOException {
        EnumSet<SharedAccessFilePermissions> permissionSet = null;

        for (int bits = 0x1; bits < 0x10; bits++) {
            permissionSet = getPermissions(bits);
            SharedAccessFilePolicy policy = createSharedAccessPolicy(permissionSet, 300);

            CloudFile testFile = FileTestHelper.uploadNewFile(this.share, 512, null);
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
    
    @Test
    public void testFileCopyFromBlobWithSasAndSnapshot()
            throws URISyntaxException, StorageException, InterruptedException, IOException, InvalidKeyException {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        CloudBlobContainer container = TestHelper.createCloudBlobClient().getContainerReference(BlobTestHelper.generateRandomContainerName());
        container.createIfNotExists();
        CloudBlockBlob source = container.getBlockBlobReference(blobName);
        String data = "String data";
        source.uploadText(data, Constants.UTF8_CHARSET, null, null, null);

        byte[] buffer = BlobTestHelper.getRandomBuffer(512);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        source.upload(stream, buffer.length);
        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        SharedAccessFilePolicy policy = createSharedAccessPolicy(
                EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.WRITE,
                      SharedAccessFilePermissions.LIST, SharedAccessFilePermissions.DELETE), 5000);

        CloudFile copy = this.share.getRootDirectoryReference().getFileReference("copy");
        String sasToken = copy.generateSharedAccessSignature(policy, null);
        CloudFile copySas = new CloudFile(new URI(copy.getUri().toString() + "?" + sasToken));
        
        // Generate account SAS for the source
        // Cannot generate a SAS directly on a snapshot and the SAS for the destination is only for the destination
        SharedAccessAccountPolicy accountPolicy = new SharedAccessAccountPolicy();
        accountPolicy.setPermissions(EnumSet.of(SharedAccessAccountPermissions.READ, SharedAccessAccountPermissions.WRITE));
        accountPolicy.setServices(EnumSet.of(SharedAccessAccountService.BLOB));
        accountPolicy.setResourceTypes(EnumSet.of(SharedAccessAccountResourceType.OBJECT, SharedAccessAccountResourceType.CONTAINER));
        accountPolicy.setSharedAccessExpiryTime(policy.getSharedAccessExpiryTime());
        final CloudBlobClient sasClient = TestHelper.createCloudBlobClient(accountPolicy, false);

        CloudBlockBlob snapshot = (CloudBlockBlob) source.createSnapshot();
        CloudBlockBlob sasBlob = (CloudBlockBlob) sasClient.getContainerReference(container.getName())
                .getBlobReferenceFromServer(snapshot.getName(), snapshot.getSnapshotID(), null, null, null);
        sasBlob.exists();

        String copyId = copySas.startCopy(BlobTestHelper.defiddler(sasBlob));
        FileTestHelper.waitForCopy(copySas);
        
        copySas.downloadAttributes();
        FileProperties prop1 = copySas.getProperties();
        BlobProperties prop2 = sasBlob.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentDisposition(),
                prop2.getContentDisposition());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value", copySas.getMetadata().get("Test"));
        assertEquals(copyId, copySas.getCopyState().getCopyId());

        snapshot.delete();
        source.delete();
        copySas.delete();
        container.delete();
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
            throws StorageException, URISyntaxException, IOException {
        
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
                            HttpURLConnection.HTTP_FORBIDDEN, ((StorageException) ex.getCause()).getHttpStatusCode());
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
                assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
            }
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
                            HttpURLConnection.HTTP_FORBIDDEN, ((StorageException) ex.getCause()).getHttpStatusCode());
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
                assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
            }
        }

        if (share != null) {
            final StorageUri uri = PathUtility.appendPathToUri(
                share.getStorageUri(), FileTestHelper.generateRandomFileName());
            final CloudFile upFile = new CloudFile(credentials.transformUri(uri));
            
            if(permissions.contains(SharedAccessFilePermissions.WRITE)) {
                upFile.upload(FileTestHelper.getRandomDataStream(512), 512, null, null, null);
            }
            else {
                if (permissions.contains(SharedAccessFilePermissions.CREATE)) {
                    upFile.create(0);
                }
                
                try {
                    upFile.upload(FileTestHelper.getRandomDataStream(512), 512, null, null, null);
                    fail();
                }
                catch (StorageException ex) {
                    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
                }
            }
        }

        if (permissions.contains(SharedAccessFilePermissions.WRITE) && 
                permissions.contains(SharedAccessFilePermissions.READ)) {
            file.downloadAttributes();
            file.uploadMetadata();
        }
        else {
            try {
                file.downloadAttributes();
                file.uploadMetadata();
                fail();
            }
            catch (StorageException ex) {
                assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
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
                assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
            }
        }
    }

    private EnumSet<SharedAccessFilePermissions> getPermissions(int bits) {
        EnumSet<SharedAccessFilePermissions> permissionSet = EnumSet.noneOf(SharedAccessFilePermissions.class);
        if ((bits & 0x1) == 0x1) {
            permissionSet.add(SharedAccessFilePermissions.READ);
        }

        if ((bits & 0x2) == 0x2) {
            permissionSet.add(SharedAccessFilePermissions.CREATE);
        }

        if ((bits & 0x4) == 0x4) {
            permissionSet.add(SharedAccessFilePermissions.WRITE);
        }

        if ((bits & 0x8) == 0x8) {
            permissionSet.add(SharedAccessFilePermissions.DELETE);
        }

        if ((bits & 0x10) == 0x10) {
            permissionSet.add(SharedAccessFilePermissions.LIST);
        }

        return permissionSet;
    }
}