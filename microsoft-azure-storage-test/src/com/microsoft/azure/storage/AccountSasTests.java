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

package com.microsoft.azure.storage;


import com.microsoft.azure.storage.blob.BlobTestHelper;
import com.microsoft.azure.storage.blob.BlobType;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.FileTestHelper;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import com.microsoft.azure.storage.queue.MessageUpdateFields;
import com.microsoft.azure.storage.queue.QueueTestHelper;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.EntityProperty;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableTestHelper;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
import java.util.NoSuchElementException;
import java.util.TimeZone;

import static org.junit.Assert.*;

@Category({ TestRunners.DevFabricTests.class, TestRunners.DevStoreTests.class, TestRunners.CloudTests.class })
public class AccountSasTests {
    private static final String DOES_NOT_EXIST_ERROR_MESSAGE = "The specified resource does not exist.";
    private static final String ENUMERATION_ERROR_MESSAGE =
            "An error occurred while enumerating the result, check the original exception for details.";
    private static final String INVALID_PERMISSION_MESSAGE =
            "This request is not authorized to perform this operation using this permission.";
    private static final String INVALID_RESOURCE_TYPE_MESSAGE =
            "This request is not authorized to perform this operation using this resource type.";
    private static final String INVALID_SERVICE_MESSAGE =
            "This request is not authorized to perform this operation using this service.";
    private static final String QUERY_PARAM_MISSING_MESSAGE =
            "Server failed to authenticate the request. Make sure the value of Authorization header is formed correctly including the signature.";

    private static final int ADD_CODE =         0x1;
    private static final int CREATE_CODE =      0x2;
    private static final int DELETE_CODE =      0x4;
    private static final int LIST_CODE =        0x8;
    private static final int PROCESS_CODE =    0x10;
    private static final int READ_CODE =       0x20;
    private static final int UPDATE_CODE =     0x40;
    private static final int WRITE_CODE =      0x80;
    private static final int OBJECT_CODE =    0x100;
    private static final int CONTAINER_CODE = 0x200;
    private static final int SERVICE_CODE =   0x400;

    // 0x7ff
    private static final int FULL_PERMS_CODE =
            ADD_CODE | CREATE_CODE | DELETE_CODE | LIST_CODE | PROCESS_CODE | READ_CODE |
            UPDATE_CODE | WRITE_CODE | OBJECT_CODE | CONTAINER_CODE | SERVICE_CODE;
    private static final int EMPTY_PERMS_CODE = 0x0;

    private CloudBlobClient blobClient;
    private CloudBlobContainer blobContainer;

    private CloudFileClient fileClient;
    private CloudFileShare fileShare;

    private CloudQueueClient queueClient;
    private CloudQueue queueQueue;

    private CloudTableClient tableClient;
    private CloudTable tableTable;
    
    
    @Before
    public void accountSasTestMethodSetup() throws URISyntaxException, StorageException, IOException {
        this.blobClient = TestHelper.createCloudBlobClient();
        this.blobContainer = BlobTestHelper.getRandomContainerReference();

        this.fileClient = TestHelper.createCloudFileClient();
        this.fileShare = FileTestHelper.getRandomShareReference();

        this.queueClient = TestHelper.createCloudQueueClient();
        this.queueQueue = QueueTestHelper.getRandomQueueReference();

        this.tableClient = TestHelper.createCloudTableClient();
        this.tableTable = TableTestHelper.getRandomTableReference();
    }

    @After
    public void accountSasTestMethodTearDown() throws StorageException, URISyntaxException {
        this.blobContainer.deleteIfExists();
        this.fileShare.deleteIfExists();
        this.queueQueue.deleteIfExists();
        this.tableTable.deleteIfExists();
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testInvalidIP() throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        // Arbitrary non-IP string
        String ip = "not an IP";
        try {
            new IPRange(ip);
            fail("Invalid IP address should throw");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(UnknownHostException.class, ex.getCause().getClass());
            assertEquals(String.format(SR.INVALID_IP_ADDRESS, ip), ex.getMessage());
        }

        // IPv6 Address
        ip = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        try {
            new IPRange(ip);
            fail("Invalid IP address should throw");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ClassCastException.class, ex.getCause().getClass());
            assertEquals(String.format(SR.INVALID_IP_ADDRESS, ip), ex.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testBlobServiceAccountSas() throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        // A file policy should not work on blobs or containers
        try {
            testBlobAccountSas(this.blobContainer, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }

        // A queue policy should not work on blobs or containers
        try {
            testBlobAccountSas(this.blobContainer, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }

        // A table policy should not work on blobs or containers
        try {
            testBlobAccountSas(this.blobContainer, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testBlobIPAccountSas() throws InvalidKeyException, StorageException, URISyntaxException, IOException {

        IPRange allIP = new IPRange("0.0.0.0", "255.255.255.255");
        IPRange noneIP = new IPRange("0.0.0.0");

        // Ensure access attempt from invalid IP fails
        IPRange sourceIP = null;
        try {
            testBlobAccountSas(this.blobContainer, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, noneIP, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
        }
        finally {
            this.blobContainer.deleteIfExists();
        }

        // Ensure access attempt from the single allowed IP succeeds
        this.blobContainer = BlobTestHelper.getRandomContainerReference();
        testBlobAccountSas(this.blobContainer, false,
                generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, sourceIP, null));

        // Ensure access attempt from one of many valid IPs succeeds
        this.blobContainer = BlobTestHelper.getRandomContainerReference();
        testBlobAccountSas(this.blobContainer, false,
            generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, allIP, null));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testBlobProtocolAccountSas()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {

        // Ensure attempt from http fails against HTTPS_ONLY
        try {
            testBlobAccountSas(this.blobContainer, false, generatePolicy(
                    AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, null, SharedAccessProtocols.HTTPS_ONLY));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.CANNOT_TRANSFORM_NON_HTTPS_URI_WITH_HTTPS_ONLY_CREDENTIALS, ex.getMessage());
        }
        finally {
            this.blobContainer.deleteIfExists();
        }

        // Ensure attempt from https succeeds against HTTPS_ONLY
        this.blobContainer = BlobTestHelper.getRandomContainerReference();
        testBlobAccountSas(this.blobContainer, true, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, null, SharedAccessProtocols.HTTPS_ONLY));

        // Ensure attempts from both https and http succeed against HTTPS_HTTP
        this.blobContainer = BlobTestHelper.getRandomContainerReference();
        testBlobAccountSas(this.blobContainer, true, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, null, SharedAccessProtocols.HTTPS_HTTP));

        this.blobContainer = BlobTestHelper.getRandomContainerReference();
        testBlobAccountSas(this.blobContainer, false, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, null, SharedAccessProtocols.HTTPS_HTTP));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testBlobAccountSasCombinations()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        // Test full and empty permissions
        testBlobAccountSas(false, AccountSasTests.FULL_PERMS_CODE);
        testBlobAccountSas(false, AccountSasTests.EMPTY_PERMS_CODE);

        // Test each individual permission
        testBlobAccountSas(false, AccountSasTests.ADD_CODE);
        testBlobAccountSas(false, AccountSasTests.CREATE_CODE);
        testBlobAccountSas(false, AccountSasTests.DELETE_CODE);
        testBlobAccountSas(false, AccountSasTests.LIST_CODE);
        testBlobAccountSas(false, AccountSasTests.READ_CODE);
        testBlobAccountSas(false, AccountSasTests.WRITE_CODE);
        testBlobAccountSas(false, AccountSasTests.OBJECT_CODE);
        testBlobAccountSas(false, AccountSasTests.CONTAINER_CODE);
        testBlobAccountSas(false, AccountSasTests.SERVICE_CODE);

        // Test an arbitrary combination of permissions.
        final int bits = AccountSasTests.OBJECT_CODE | AccountSasTests.SERVICE_CODE | AccountSasTests.READ_CODE |
                AccountSasTests.CREATE_CODE | AccountSasTests.DELETE_CODE;
        testBlobAccountSas(false, bits);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testFileServiceAccountSas() throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        // A blob policy should not work on files or shares
        try {
            testFileAccountSas(this.fileShare, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }

        // A queue policy should not work on files or shares
        try {
            testFileAccountSas(this.fileShare, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }

        // A table policy should not work on files or shares
        try {
            testFileAccountSas(this.fileShare, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testFileIPAccountSas() throws InvalidKeyException, StorageException, URISyntaxException, IOException {

        IPRange allIP = new IPRange("0.0.0.0", "255.255.255.255");
        IPRange noneIP = new IPRange("0.0.0.0");

        // Ensure access attempt from invalid IP fails
        IPRange sourceIP = null;
        try {
            testFileAccountSas(this.fileShare, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, noneIP, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
        }
        finally {
            this.fileShare.deleteIfExists();
        }

        // Ensure access attempt from the single allowed IP succeeds
        this.fileShare = FileTestHelper.getRandomShareReference();
        testFileAccountSas(this.fileShare, false,
                generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, sourceIP, null));

        // Ensure access attempt from one of many valid IPs succeeds
        this.fileShare = FileTestHelper.getRandomShareReference();
        testFileAccountSas(this.fileShare, false,
            generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, allIP, null));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testFileProtocolAccountSas()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {

        // Ensure attempt from http fails against HTTPS_ONLY
        try {
            testFileAccountSas(this.fileShare, false, generatePolicy(
                    AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, null, SharedAccessProtocols.HTTPS_ONLY));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.CANNOT_TRANSFORM_NON_HTTPS_URI_WITH_HTTPS_ONLY_CREDENTIALS, ex.getMessage());
        }
        finally {
            this.fileShare.deleteIfExists();
        }

        // Ensure attempt from https succeeds against HTTPS_ONLY
        this.fileShare = FileTestHelper.getRandomShareReference();
        testFileAccountSas(this.fileShare, true, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, null, SharedAccessProtocols.HTTPS_ONLY));

        // Ensure attempts from both https and http succeed against HTTPS_HTTP
        this.fileShare = FileTestHelper.getRandomShareReference();
        testFileAccountSas(this.fileShare, true, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, null, SharedAccessProtocols.HTTPS_HTTP));

        this.fileShare = FileTestHelper.getRandomShareReference();
        testFileAccountSas(this.fileShare, false, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, null, SharedAccessProtocols.HTTPS_HTTP));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testFileAccountSasCombinations()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        // Test full and empty permissions
        testFileAccountSas(false, AccountSasTests.FULL_PERMS_CODE);
        testFileAccountSas(false, AccountSasTests.EMPTY_PERMS_CODE);

        // Test each individual permission
        testFileAccountSas(false, AccountSasTests.CREATE_CODE);
        testFileAccountSas(false, AccountSasTests.DELETE_CODE);
        testFileAccountSas(false, AccountSasTests.LIST_CODE);
        testFileAccountSas(false, AccountSasTests.READ_CODE);
        testFileAccountSas(false, AccountSasTests.WRITE_CODE);
        testFileAccountSas(false, AccountSasTests.OBJECT_CODE);
        testFileAccountSas(false, AccountSasTests.CONTAINER_CODE);
        testFileAccountSas(false, AccountSasTests.SERVICE_CODE);

        // Test an arbitrary combination of permissions.
        final int bits = AccountSasTests.OBJECT_CODE | AccountSasTests.SERVICE_CODE | AccountSasTests.READ_CODE |
                AccountSasTests.CREATE_CODE | AccountSasTests.DELETE_CODE;
        testFileAccountSas(false, bits);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testQueueServiceAccountSas()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException, InterruptedException {

        // A blob policy should not work on queues
        try {
            testQueueAccountSas(this.queueQueue, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }

        // A file policy should not work on queues
        try {
            testQueueAccountSas(this.queueQueue, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }

        // A table policy should not work on queues
        try {
            testQueueAccountSas(this.queueQueue, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testQueueIPAccountSas()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException, InterruptedException {

        IPRange allIP = new IPRange("0.0.0.0", "255.255.255.255");
        IPRange noneIP = new IPRange("0.0.0.0");

        // Ensure access attempt from invalid IP fails
        IPRange sourceIP = null;
        try {
            testQueueAccountSas(this.queueQueue, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, noneIP, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
        }
        finally {
            this.queueQueue.deleteIfExists();
        }

        // Ensure access attempt from the single allowed IP succeeds
        this.queueQueue = QueueTestHelper.getRandomQueueReference();
        testQueueAccountSas(this.queueQueue, false,
                generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, sourceIP, null));

        // Ensure access attempt from one of many valid IPs succeeds
        this.queueQueue = QueueTestHelper.getRandomQueueReference();
        testQueueAccountSas(this.queueQueue, false,
            generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, allIP, null));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testQueueProtocolAccountSas()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException, InterruptedException {

        // Ensure attempt from http fails against HTTPS_ONLY
        try {
            testQueueAccountSas(this.queueQueue, false, generatePolicy(
                    AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, null, SharedAccessProtocols.HTTPS_ONLY));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.CANNOT_TRANSFORM_NON_HTTPS_URI_WITH_HTTPS_ONLY_CREDENTIALS, ex.getMessage());
        }
        finally {
            this.queueQueue.deleteIfExists();
        }

        // Ensure attempt from https succeeds against HTTPS_ONLY
        this.queueQueue = QueueTestHelper.getRandomQueueReference();
        testQueueAccountSas(this.queueQueue, true, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, null, SharedAccessProtocols.HTTPS_ONLY));

        // Ensure attempts from both https and http succeed against HTTPS_HTTP
        this.queueQueue = QueueTestHelper.getRandomQueueReference();
        testQueueAccountSas(this.queueQueue, true, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, null, SharedAccessProtocols.HTTPS_HTTP));

        this.queueQueue = QueueTestHelper.getRandomQueueReference();
        testQueueAccountSas(this.queueQueue, false, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, null, SharedAccessProtocols.HTTPS_HTTP));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testQueueAccountSasCombinations()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException, InterruptedException {
        // Test full and empty permissions
        testQueueAccountSas(false, AccountSasTests.FULL_PERMS_CODE);
        testQueueAccountSas(false, AccountSasTests.EMPTY_PERMS_CODE);

        // Test each individual permission
        testQueueAccountSas(false, AccountSasTests.ADD_CODE);
        testQueueAccountSas(false, AccountSasTests.CREATE_CODE);
        testQueueAccountSas(false, AccountSasTests.DELETE_CODE);
        testQueueAccountSas(false, AccountSasTests.LIST_CODE);
        testQueueAccountSas(false, AccountSasTests.PROCESS_CODE);
        testQueueAccountSas(false, AccountSasTests.READ_CODE);
        testQueueAccountSas(false, AccountSasTests.UPDATE_CODE);
        testQueueAccountSas(false, AccountSasTests.WRITE_CODE);
        testQueueAccountSas(false, AccountSasTests.OBJECT_CODE);
        testQueueAccountSas(false, AccountSasTests.CONTAINER_CODE);
        testQueueAccountSas(false, AccountSasTests.SERVICE_CODE);

        // Test an arbitrary combination of permissions.
        final int bits = AccountSasTests.OBJECT_CODE | AccountSasTests.SERVICE_CODE | AccountSasTests.READ_CODE |
                AccountSasTests.CREATE_CODE | AccountSasTests.DELETE_CODE;
        testQueueAccountSas(false, bits);
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testTableServiceAccountSas() throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        // A blob policy should not work on tables
        try {
            testTableAccountSas(this.tableTable, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.BLOB, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }

        // A file policy should not work on tables
        try {
            testTableAccountSas(this.tableTable, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.FILE, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }

        // A queue policy should not work on tables
        try {
            testTableAccountSas(this.tableTable, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.QUEUE, null, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(AccountSasTests.INVALID_SERVICE_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testTableIPAccountSas() throws InvalidKeyException, StorageException, URISyntaxException, IOException {

        IPRange allIP = new IPRange("0.0.0.0", "255.255.255.255");
        IPRange noneIP = new IPRange("0.0.0.0");

        // Ensure access attempt from invalid IP fails
        IPRange sourceIP = null;
        try {
            testTableAccountSas(this.tableTable, false,
                    generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, noneIP, null));
            fail();
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
        }
        finally {
            this.tableTable.deleteIfExists();
        }

        // Ensure access attempt from the single allowed IP succeeds
        this.tableTable = TableTestHelper.getRandomTableReference();
        testTableAccountSas(this.tableTable, false,
                generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, sourceIP, null));

        // Ensure access attempt from one of many valid IPs succeeds
        this.tableTable = TableTestHelper.getRandomTableReference();
        testTableAccountSas(this.tableTable, false,
            generatePolicy(AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, allIP, null));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testTableProtocolAccountSas()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {

        // Ensure attempt from http fails against HTTPS_ONLY
        try {
            testTableAccountSas(this.tableTable, false, generatePolicy(
                    AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, null, SharedAccessProtocols.HTTPS_ONLY));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.CANNOT_TRANSFORM_NON_HTTPS_URI_WITH_HTTPS_ONLY_CREDENTIALS, ex.getMessage());
        }
        finally {
            this.tableTable.deleteIfExists();
        }

        // Ensure attempt from https succeeds against HTTPS_ONLY
        this.tableTable = TableTestHelper.getRandomTableReference();
        testTableAccountSas(this.tableTable, true, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, null, SharedAccessProtocols.HTTPS_ONLY));

        // Ensure attempts from both https and http succeed against HTTPS_HTTP
        this.tableTable = TableTestHelper.getRandomTableReference();
        testTableAccountSas(this.tableTable, true, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, null, SharedAccessProtocols.HTTPS_HTTP));

        this.tableTable = TableTestHelper.getRandomTableReference();
        testTableAccountSas(this.tableTable, false, generatePolicy(
                AccountSasTests.FULL_PERMS_CODE, SharedAccessAccountService.TABLE, null, SharedAccessProtocols.HTTPS_HTTP));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testTableAccountSasCombinations()
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        // Test full and empty permissions
        testTableAccountSas(false, AccountSasTests.FULL_PERMS_CODE);
        testTableAccountSas(false, AccountSasTests.EMPTY_PERMS_CODE);

        // Test each individual permission
        testTableAccountSas(false, AccountSasTests.ADD_CODE);
        testTableAccountSas(false, AccountSasTests.CREATE_CODE);
        testTableAccountSas(false, AccountSasTests.DELETE_CODE);
        testTableAccountSas(false, AccountSasTests.LIST_CODE);
        testTableAccountSas(false, AccountSasTests.READ_CODE);
        testTableAccountSas(false, AccountSasTests.UPDATE_CODE);
        testTableAccountSas(false, AccountSasTests.WRITE_CODE);
        testTableAccountSas(false, AccountSasTests.OBJECT_CODE);
        testTableAccountSas(false, AccountSasTests.CONTAINER_CODE);
        testTableAccountSas(false, AccountSasTests.SERVICE_CODE);

        // Test an arbitrary combination of permissions.
        final int bits = AccountSasTests.OBJECT_CODE | AccountSasTests.SERVICE_CODE | AccountSasTests.READ_CODE |
                AccountSasTests.CREATE_CODE | AccountSasTests.DELETE_CODE;
        testTableAccountSas(false, bits);
    }
    


    private void testBlobAccountSas(final boolean useHttps, final int bits)
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        SharedAccessAccountPolicy policy = generatePolicy(bits, SharedAccessAccountService.BLOB, null, null);
        this.blobContainer = this.blobClient.getContainerReference("blobtest" + bits);

        try {
            testBlobAccountSas(this.blobContainer, useHttps, policy);
        }
        catch (StorageException ex) {
            if (bits < AccountSasTests.OBJECT_CODE ||
                    bits % AccountSasTests.OBJECT_CODE == AccountSasTests.EMPTY_PERMS_CODE) {
                // Expected failure if permissions or resource type is empty.
                assertEquals(AccountSasTests.QUERY_PARAM_MISSING_MESSAGE, ex.getMessage());
            }
            else {
                throw ex;
            }
        }
        finally {
            this.blobContainer.deleteIfExists();
        }
    }

    private void testBlobAccountSas(CloudBlobContainer container, boolean useHttps, SharedAccessAccountPolicy policy)
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {

        assertNotNull(container);
        assertNotNull(policy);
        assertFalse(container.exists());

        final CloudBlobClient sasClient = TestHelper.createCloudBlobClient(policy, useHttps);
        URI sasUri = sasClient.getContainerReference(container.getName()).getUri();
        sasUri = sasClient.getCredentials().transformUri(sasUri);
        final CloudBlobContainer sasContainer = new CloudBlobContainer(sasUri);

        // Test creating the container
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER) &&
                (policy.getPermissions().contains(SharedAccessAccountPermissions.CREATE) ||
                 policy.getPermissions().contains(SharedAccessAccountPermissions.WRITE))) {

            sasContainer.create();
        }
        else {
            try {
                sasContainer.create();
                fail();
            }
            catch (StorageException ex) {
                if (AccountSasTests.QUERY_PARAM_MISSING_MESSAGE.equals(ex.getMessage())) {
                    throw ex;
                }

                if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                }
                else {
                    assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                }
                container.create();
            }
        }

        assertTrue(container.exists());

        // Test listing the containers on the client
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.SERVICE) &&
                (policy.getPermissions().contains(SharedAccessAccountPermissions.LIST))) {

            assertEquals(sasContainer.getName(),
                    sasClient.listContainers(sasContainer.getName()).iterator().next().getName());
        }
        else {
            try {
                sasClient.listContainers(sasContainer.getName()).iterator().next();
                fail();
            }
            catch (NoSuchElementException ex) {
                assertEquals(AccountSasTests.ENUMERATION_ERROR_MESSAGE, ex.getMessage());
                assertEquals(sasContainer.getName(),
                        this.blobClient.listContainers(sasContainer.getName()).iterator().next().getName());
            }
        }

        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.OBJECT)) {

            // Test creating a new blob
            CloudAppendBlob blob = null;
            CloudAppendBlob sasBlob = null;
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.CREATE) ||
                    policy.getPermissions().contains(SharedAccessAccountPermissions.WRITE)) {

                sasBlob = (CloudAppendBlob) BlobTestHelper.uploadNewBlob(
                        sasContainer, BlobType.APPEND_BLOB, null, 0, null);
                blob = container.getAppendBlobReference(sasBlob.getName());
            }
            else {
                try {
                    sasBlob = (CloudAppendBlob) BlobTestHelper.uploadNewBlob(
                            sasContainer, BlobType.APPEND_BLOB, null, 0, null);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    blob = (CloudAppendBlob) BlobTestHelper.uploadNewBlob(
                            container, BlobType.APPEND_BLOB, null, 0, null);
                    sasBlob = (CloudAppendBlob) BlobTestHelper.getBlobReference(
                            BlobType.APPEND_BLOB, sasContainer, blob.getName());
                }
            }

            assertTrue(blob.exists());

            // Test uploading data to the blob
            final int length = 512;
            ByteArrayInputStream sourceStream = BlobTestHelper.getRandomDataStream(length);
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.ADD) ||
                    policy.getPermissions().contains(SharedAccessAccountPermissions.WRITE)) {

                sasBlob.appendBlock(sourceStream, length);
            }
            else {
                try {
                    sasBlob.appendBlock(sourceStream, length);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    sourceStream = BlobTestHelper.getRandomDataStream(length);
                    blob.appendBlock(sourceStream, length);
                }
            }

            // Test downloading data from the blob
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.READ)) {
                sasBlob.download(outStream);
            }
            else {
                try {
                    sasBlob.download(outStream);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    blob.download(outStream);
                }
            }

            TestHelper.assertStreamsAreEqual(sourceStream, new ByteArrayInputStream(outStream.toByteArray()));

            if (policy.getPermissions().contains(SharedAccessAccountPermissions.DELETE)) {
                sasBlob.delete();
            }
            else {
                try {
                    sasBlob.delete();
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    blob.delete();
                }
            }

            assertFalse(blob.exists());
        }
        else {
            try {
                BlobTestHelper.uploadNewBlob(sasContainer, BlobType.APPEND_BLOB, null, 0, null);
                fail();
            }
            catch (StorageException ex) {
                assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                BlobTestHelper.uploadNewBlob(container, BlobType.APPEND_BLOB, null, 0, null);
            }
        }

        // Test deleting the container
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER) &&
                 policy.getPermissions().contains(SharedAccessAccountPermissions.DELETE)) {

            sasContainer.delete();
        }
        else {
            try {
                sasContainer.delete();
                fail();
            }
            catch (StorageException ex) {
                if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                }
                else {
                    assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                }
                container.delete();
            }
        }
    }

    private void testFileAccountSas(final boolean useHttps, final int bits)
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        SharedAccessAccountPolicy policy = generatePolicy(bits, SharedAccessAccountService.FILE, null, null);
        this.fileShare = this.fileClient.getShareReference("filetest" + bits);

        try {
            testFileAccountSas(this.fileShare, useHttps, policy);
        }
        catch (StorageException ex) {
            if (bits < AccountSasTests.OBJECT_CODE ||
                    bits % AccountSasTests.OBJECT_CODE == AccountSasTests.EMPTY_PERMS_CODE) {
                // Expected failure if permissions or resource type is empty.
                assertEquals(AccountSasTests.QUERY_PARAM_MISSING_MESSAGE, ex.getMessage());
            }
            else {
                throw ex;
            }
        }
        finally {
            this.fileShare.deleteIfExists();
        }
    }

    private void testFileAccountSas(CloudFileShare share, boolean useHttps, SharedAccessAccountPolicy policy)
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {

        assertNotNull(share);
        assertNotNull(policy);
        assertFalse(share.exists());

        final CloudFileClient sasClient = TestHelper.createCloudFileClient(policy, useHttps);
        URI sasUri = sasClient.getShareReference(share.getName()).getUri();
        sasUri = sasClient.getCredentials().transformUri(sasUri);
        final CloudFileShare sasShare = new CloudFileShare(sasUri);

        // Test creating the share
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER) &&
                (policy.getPermissions().contains(SharedAccessAccountPermissions.CREATE) ||
                 policy.getPermissions().contains(SharedAccessAccountPermissions.WRITE))) {

            sasShare.create();
        }
        else {
            try {
                sasShare.create();
                fail();
            }
            catch (StorageException ex) {
                if (AccountSasTests.QUERY_PARAM_MISSING_MESSAGE.equals(ex.getMessage())) {
                    throw ex;
                }

                if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                }
                else {
                    assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                }
                share.create();
            }
        }

        assertTrue(share.exists());

        // Test listing the shares on the client
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.SERVICE) &&
                (policy.getPermissions().contains(SharedAccessAccountPermissions.LIST))) {

            assertEquals(sasShare.getName(), sasClient.listShares(sasShare.getName()).iterator().next().getName());
        }
        else {
            try {
                sasClient.listShares(sasShare.getName()).iterator().next();
                fail();
            }
            catch (NoSuchElementException ex) {
                assertEquals(AccountSasTests.ENUMERATION_ERROR_MESSAGE, ex.getMessage());
                assertEquals(sasShare.getName(),
                        this.fileClient.listShares(sasShare.getName()).iterator().next().getName());
            }
        }

        final int length = 512;
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.OBJECT)) {

            // Test creating a new file
            CloudFile file = null;
            CloudFile sasFile = null;

            sasFile = sasShare.getRootDirectoryReference().getFileReference(FileTestHelper.generateRandomFileName());
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.CREATE) ||
                    policy.getPermissions().contains(SharedAccessAccountPermissions.WRITE)) {

                sasFile.create(length);
                file = share.getRootDirectoryReference().getFileReference(sasFile.getName());
            }
            else {
                try {
                    sasFile.create(length);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    file = FileTestHelper.uploadNewFile(share, FileTestHelper.getRandomDataStream(0), length, null);
                    sasFile = sasShare.getRootDirectoryReference().getFileReference(file.getName());
                }
            }

            assertTrue(file.exists());

            //Test writing data to  a file
            final ByteArrayInputStream sourcestream = FileTestHelper.getRandomDataStream(length);
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.WRITE)) {
                sasFile.upload(sourcestream, length);
            }
            else {
                try {
                    sasFile.upload(sourcestream, length);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    file.upload(sourcestream, length);
                }
            }

            // Test downloading data from the file
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.READ)) {
                sasFile.download(outStream);
            }
            else {
                try {
                    sasFile.download(outStream);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    file.download(outStream);
                }
            }

            TestHelper.assertStreamsAreEqual(sourcestream, new ByteArrayInputStream(outStream.toByteArray()));

            if (policy.getPermissions().contains(SharedAccessAccountPermissions.DELETE)) {
                sasFile.delete();
            }
            else {
                try {
                    sasFile.delete();
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    file.delete();
                }
            }

            assertFalse(file.exists());
        }
        else {
            try {
                FileTestHelper.uploadNewFile(sasShare, FileTestHelper.getRandomDataStream(0), length, null);
                fail();
            }
            catch (StorageException ex) {
                assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                FileTestHelper.uploadNewFile(share, FileTestHelper.getRandomDataStream(0), length, null);
            }
        }

        // Test deleting the share
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER) &&
                 policy.getPermissions().contains(SharedAccessAccountPermissions.DELETE)) {

            sasShare.delete();
        }
        else {
            try {
                sasShare.delete();
                fail();
            }
            catch (StorageException ex) {
                if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                }
                else {
                    assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                }
                share.delete();
            }
        }
    }

    private void testQueueAccountSas(final boolean useHttps, final int bits)
            throws InvalidKeyException, StorageException, URISyntaxException, IOException, InterruptedException {
        SharedAccessAccountPolicy policy = generatePolicy(bits, SharedAccessAccountService.QUEUE, null, null);
        this.queueQueue = this.queueClient.getQueueReference("queuetest" + bits);

        try {
            testQueueAccountSas(this.queueQueue, useHttps, policy);
        }
        catch (StorageException ex) {
            if (bits < AccountSasTests.OBJECT_CODE ||
                    bits % AccountSasTests.OBJECT_CODE == AccountSasTests.EMPTY_PERMS_CODE) {
                // Expected failure if permissions or resource type is empty.
                assertEquals(AccountSasTests.QUERY_PARAM_MISSING_MESSAGE, ex.getMessage());
            }
            else {
                throw ex;
            }
        }
        finally {
            this.queueQueue.deleteIfExists();
        }
    }

    private void testQueueAccountSas(CloudQueue queue, boolean useHttps, SharedAccessAccountPolicy policy)
            throws InvalidKeyException, StorageException, URISyntaxException, IOException, InterruptedException {

        assertNotNull(policy);
        assertNotNull(queue);
        assertFalse(queue.exists());

        final CloudQueueClient sasClient = TestHelper.createCloudQueueClient(policy, useHttps);
        URI  sasUri = sasClient.getQueueReference(queue.getName()).getUri();
        sasUri = sasClient.getCredentials().transformUri(sasUri);
        final CloudQueue sasQueue = new CloudQueue(sasUri);

        final String key = "testkey";
        final String value = "testvalue";

        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
            // Test creating the queue
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.CREATE) ||
                    policy.getPermissions().contains(SharedAccessAccountPermissions.WRITE)) {

                sasQueue.create();
            }
            else {
                try {
                    sasQueue.create();
                    fail();
                }
                catch (StorageException ex) {
                    if (AccountSasTests.QUERY_PARAM_MISSING_MESSAGE.equals(ex.getMessage())) {
                        throw ex;
                    }

                    if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
                        assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    }
                    else {
                        assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                    }
                    queue.create();
                }
            }

            // Test queue metadata
            queue.setMetadata(new HashMap<String, String>());
            queue.getMetadata().put(key, Constants.ID);
            queue.uploadMetadata();

            sasQueue.setMetadata(new HashMap<String, String>());
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.WRITE)) {
                sasQueue.getMetadata().put(key, value);
                sasQueue.uploadMetadata();
            }
            else {
                try {
                    sasQueue.getMetadata().put(key, value);
                    sasQueue.uploadMetadata();
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);

                    queue.getMetadata().put(key, value);
                    queue.uploadMetadata();
                }
            }

            queue.downloadAttributes();
            assertEquals(value, queue.getMetadata().get(key));
        }
        else {
            try {
                sasQueue.create();
                fail();
            }
            catch (StorageException ex) {
                if (AccountSasTests.QUERY_PARAM_MISSING_MESSAGE.equals(ex.getMessage())) {
                    throw ex;
                }

                if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                }
                else {
                    assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                }
                queue.create();
            }
        }

        assertTrue(queue.exists());

        // Test listing the queues on the client
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.SERVICE) &&
                (policy.getPermissions().contains(SharedAccessAccountPermissions.LIST))) {

            assertEquals(sasQueue.getName(), sasClient.listQueues(sasQueue.getName()).iterator().next().getName());
        }
        else {
            try {
                sasClient.listQueues(sasQueue.getName()).iterator().next();
                fail();
            }
            catch (NoSuchElementException ex) {
                assertEquals(AccountSasTests.ENUMERATION_ERROR_MESSAGE, ex.getMessage());
                assertEquals(sasQueue.getName(),
                        this.queueClient.listQueues(sasQueue.getName()).iterator().next().getName());
            }
        }

        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.OBJECT)) {
            // Test inserting a message into a queue
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.ADD)) {
                sasQueue.addMessage(new CloudQueueMessage(value));
            }
            else {
                try {
                    sasQueue.addMessage(new CloudQueueMessage(value));
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    queue.addMessage(new CloudQueueMessage(value));
                }
            }

            // Test peeking at a message in the queue
            CloudQueueMessage message = null;
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.READ)) {
                message = sasQueue.peekMessage();
            }
            else {
                try {
                    sasQueue.peekMessage();
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    message = queue.peekMessage();
                }
            }

            assertEquals(value, message.getMessageContentAsString());

            // Test getting a message from the queue
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.PROCESS_MESSAGES)) {
                message = sasQueue.retrieveMessage();
            }
            else {
                try {
                    sasQueue.retrieveMessage();
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    message = queue.retrieveMessage();
                }
            }

            // Test updating a message in the queue
            message.setMessageContent(key);
            OperationContext oc = new OperationContext();
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.UPDATE)) {
                sasQueue.updateMessage(message, 1, EnumSet.of(MessageUpdateFields.CONTENT), null, oc);
            }
            else {
                try {
                    sasQueue.updateMessage(message, 1, EnumSet.of(MessageUpdateFields.CONTENT), null, oc);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    queue.updateMessage(message, 1, EnumSet.of(MessageUpdateFields.CONTENT), null, oc);
                }
            }

            assertEquals(oc.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
            Thread.sleep(1000);
            message = queue.peekMessage();
            assertEquals(key, message.getMessageContentAsString());

            // Test clearing all messages from the queue.
            queue.addMessage(new CloudQueueMessage(key));
            queue.addMessage(new CloudQueueMessage(value));
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.DELETE)) {
                sasQueue.clear();
            }
            else {
                try {
                    sasQueue.clear();
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    queue.clear();
                }
            }

            assertEquals(null, queue.peekMessage());
        }
        else {
            try {
                sasQueue.peekMessage();
                fail();
            }
            catch (StorageException ex) {
                assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                queue.peekMessage();
            }
        }

        // Test deleting the queue
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER) &&
                 policy.getPermissions().contains(SharedAccessAccountPermissions.DELETE)) {

            sasQueue.delete();
        }
        else {
            try {
                sasQueue.delete();
                fail();
            }
            catch (StorageException ex) {
                if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                }
                else {
                    assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                }
                queue.delete();
            }
        }
    }

    private void testTableAccountSas(final boolean useHttps, final int bits)
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        SharedAccessAccountPolicy policy = generatePolicy(bits, SharedAccessAccountService.TABLE, null, null);
        this.tableTable = this.tableClient.getTableReference("tabletest" + bits);

        try {
            testTableAccountSas(this.tableTable, useHttps, policy);
        }
        catch (StorageException ex) {
            if (bits < AccountSasTests.OBJECT_CODE ||
                    bits % AccountSasTests.OBJECT_CODE == AccountSasTests.EMPTY_PERMS_CODE) {
                // Expected failure if permissions or resource type is empty.
                assertEquals(AccountSasTests.QUERY_PARAM_MISSING_MESSAGE, ex.getMessage());
            }
            else {
                throw ex;
            }
        }
        finally {
            this.fileShare.deleteIfExists();
        }
    }

    private void testTableAccountSas(CloudTable table, boolean useHttps, SharedAccessAccountPolicy policy)
            throws InvalidKeyException, StorageException, URISyntaxException, IOException {
        assertNotNull(policy);
        assertNotNull(table);
        assertFalse(table.exists());

        final CloudTableClient sasClient = TestHelper.createCloudTableClient(policy, useHttps);
        URI sasUri = sasClient.getTableReference(table.getName()).getUri();
        sasUri = sasClient.getCredentials().transformUri(sasUri);
        final CloudTable sasTable = new CloudTable(sasUri);

        final String key = "testkey";
        final String value = "testvalue";
        final String value2 = "testvalue2";
        final String partition1 = "testpartition1";
        final String partition2 = "testpartition2";
        final String row1 = "testrow1";
        final String row2 = "testrow2";

        // Test creating the table
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER) &&
                (policy.getPermissions().contains(SharedAccessAccountPermissions.CREATE) ||
                policy.getPermissions().contains(SharedAccessAccountPermissions.WRITE))) {

            sasTable.create();
        }
        else {
            try {
                sasTable.create();
                fail();
            }
            catch (StorageException ex) {
                if (AccountSasTests.QUERY_PARAM_MISSING_MESSAGE.equals(ex.getMessage())) {
                    throw ex;
                }

                if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                }
                else {
                    assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                }
                table.create();
            }
        }

        assertTrue(table.exists());

        // Test listing the tables on the client
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER) &&
                policy.getPermissions().contains(SharedAccessAccountPermissions.LIST)) {

            assertEquals(sasTable.getName(), sasClient.listTables(sasTable.getName()).iterator().next());
        }
        else {
            try {
                sasClient.listTables(sasTable.getName()).iterator().next();
                fail();
            }
            catch (NoSuchElementException ex) {
                assertEquals(AccountSasTests.ENUMERATION_ERROR_MESSAGE, ex.getMessage());
                assertEquals(sasTable.getName(),
                        this.tableClient.listTables(sasTable.getName()).iterator().next());
            }
        }

        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.OBJECT)) {
            // Test inserting an entity into a table
            final HashMap<String, EntityProperty> columns = new HashMap<String, EntityProperty>();
            columns.put(key, new EntityProperty(value));
            final DynamicTableEntity entity = new DynamicTableEntity(partition1, row1, columns);
            TableOperation op = TableOperation.insert(entity);

            if (policy.getPermissions().contains(SharedAccessAccountPermissions.ADD)) {
                sasTable.execute(op);
            }
            else {
                try {
                    sasTable.execute(op);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    table.execute(op);
                }
            }

            op = TableOperation.retrieve(partition1, row1, DynamicTableEntity.class);
            DynamicTableEntity result = (DynamicTableEntity) table.execute(op).getResultAsType();
            assertEquals(value, result.getProperties().get(key).getValueAsString());

            // Test merging an entity in a table
            entity.getProperties().put(key, new EntityProperty(value2));
            op = TableOperation.merge(entity);
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.UPDATE)) {
                sasTable.execute(op);
            }
            else {
                try {
                    sasTable.execute(op);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    table.execute(op);
                }
            }

            // Test retrieving an entity from a table
            op = TableOperation.retrieve(partition1, row1, DynamicTableEntity.class);
            result = null;
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.READ)) {
                result = (DynamicTableEntity) sasTable.execute(op).getResultAsType();
            }
            else {
                try {
                    sasTable.execute(op);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    result = (DynamicTableEntity) table.execute(op).getResultAsType();
                }
            }

            assertEquals(value2, result.getProperties().get(key).getValueAsString());

            // Test insert or merge on two entities
            entity.getProperties().put(key, new EntityProperty(value));
            op = TableOperation.insertOrMerge(entity);

            final HashMap<String, EntityProperty> columns2 = new HashMap<String, EntityProperty>();
            columns2.put(key, new EntityProperty(value2));
            final DynamicTableEntity entity2 = new DynamicTableEntity(partition2, row2, columns2);
            TableOperation op2 = TableOperation.insertOrMerge(entity2);

            if (policy.getPermissions().contains(SharedAccessAccountPermissions.ADD) &&
                    policy.getPermissions().contains(SharedAccessAccountPermissions.UPDATE)) {
                sasTable.execute(op);
                sasTable.execute(op2);
            }
            else {
                try {
                    sasTable.execute(op);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    try {
                        sasTable.execute(op2);
                        fail();
                    }
                    catch (StorageException exeption) {
                        assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, exeption);
                        table.execute(op);
                        table.execute(op2);
                    }
                }
            }

            TableOperation retrieveOp = TableOperation.retrieve(partition2, row2, DynamicTableEntity.class);
            result = (DynamicTableEntity) table.execute(retrieveOp).getResultAsType();
            assertEquals(value2, result.getProperties().get(key).getValueAsString());

            retrieveOp = TableOperation.retrieve(partition1, row1, DynamicTableEntity.class);
            result = (DynamicTableEntity) table.execute(retrieveOp).getResultAsType();
            assertEquals(value, result.getProperties().get(key).getValueAsString());

            // Test deleting an entity from a table.
            op = TableOperation.delete(entity);
            if (policy.getPermissions().contains(SharedAccessAccountPermissions.DELETE)) {
                sasTable.execute(op);
            }
            else {
                try {
                    sasTable.execute(op);
                    fail();
                }
                catch (StorageException ex) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                    table.execute(op);
                }
            }

            try {
                table.execute(op);
            }
            catch (StorageException ex) {
                assertMessagesMatch(AccountSasTests.DOES_NOT_EXIST_ERROR_MESSAGE, ex);
            }
        }
        else {
            TableOperation op = TableOperation.retrieve(partition1, row1, DynamicTableEntity.class);
            try {
                sasTable.execute(op);
                fail();
            }
            catch (StorageException ex) {
                assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                table.execute(op);
            }
        }

        // Test deleting the share
        if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER) &&
                 policy.getPermissions().contains(SharedAccessAccountPermissions.DELETE)) {

            sasTable.delete();
        }
        else {
            try {
                sasTable.delete();
                fail();
            }
            catch (StorageException ex) {
                if (policy.getResourceTypes().contains(SharedAccessAccountResourceType.CONTAINER)) {
                    assertMessagesMatch(AccountSasTests.INVALID_PERMISSION_MESSAGE, ex);
                }
                else {
                    assertMessagesMatch(AccountSasTests.INVALID_RESOURCE_TYPE_MESSAGE, ex);
                }
                table.delete();
            }
        }
    }

    private static void assertMessagesMatch(String expectedMessage, StorageException ex) {
        final String message = ex.getExtendedErrorInformation().getErrorMessage().split("\n")[0];
        assertEquals(expectedMessage, message);
    }

    private static SharedAccessAccountPolicy generatePolicy(
            final int bits, final SharedAccessAccountService service, IPRange ipRange, SharedAccessProtocols protocols) {

        EnumSet<SharedAccessAccountService> services = EnumSet.noneOf(SharedAccessAccountService.class);
        EnumSet<SharedAccessAccountPermissions> permissions = EnumSet.noneOf(SharedAccessAccountPermissions.class);
        EnumSet<SharedAccessAccountResourceType> resourceTypes = EnumSet.noneOf(SharedAccessAccountResourceType.class);

        services.add(service);

        if ((bits & AccountSasTests.ADD_CODE) == AccountSasTests.ADD_CODE) {
            permissions.add(SharedAccessAccountPermissions.ADD);
        }

        if ((bits & AccountSasTests.CREATE_CODE) == AccountSasTests.CREATE_CODE) {
            permissions.add(SharedAccessAccountPermissions.CREATE);
        }

        if ((bits & AccountSasTests.DELETE_CODE) ==  AccountSasTests.DELETE_CODE) {
            permissions.add(SharedAccessAccountPermissions.DELETE);
        }

        if ((bits & AccountSasTests.LIST_CODE) ==  AccountSasTests.LIST_CODE) {
            permissions.add(SharedAccessAccountPermissions.LIST);
        }

        if ((bits & AccountSasTests.PROCESS_CODE) == AccountSasTests.PROCESS_CODE) {
            permissions.add(SharedAccessAccountPermissions.PROCESS_MESSAGES);
        }

        if ((bits & AccountSasTests.READ_CODE) == AccountSasTests.READ_CODE) {
            permissions.add(SharedAccessAccountPermissions.READ);
        }

        if ((bits & AccountSasTests.UPDATE_CODE) == AccountSasTests.UPDATE_CODE) {
            permissions.add(SharedAccessAccountPermissions.UPDATE);
        }

        if ((bits & AccountSasTests.WRITE_CODE) == AccountSasTests.WRITE_CODE) {
            permissions.add(SharedAccessAccountPermissions.WRITE);
        }

        if ((bits & AccountSasTests.OBJECT_CODE) == AccountSasTests.OBJECT_CODE) {
            resourceTypes.add(SharedAccessAccountResourceType.OBJECT);
        }

        if ((bits & AccountSasTests.CONTAINER_CODE) == AccountSasTests.CONTAINER_CODE) {
            resourceTypes.add(SharedAccessAccountResourceType.CONTAINER);
        }

        if ((bits & AccountSasTests.SERVICE_CODE) == AccountSasTests.SERVICE_CODE) {
            resourceTypes.add(SharedAccessAccountResourceType.SERVICE);
        }

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.add(Calendar.SECOND, 300);

        SharedAccessAccountPolicy policy = new SharedAccessAccountPolicy();
        policy.setServices(services);
        policy.setPermissions(permissions);
        policy.setResourceTypes(resourceTypes);
        policy.setRange(ipRange);
        policy.setProtocols(protocols);
        policy.setSharedAccessExpiryTime(cal.getTime());

        return policy;
    }
}