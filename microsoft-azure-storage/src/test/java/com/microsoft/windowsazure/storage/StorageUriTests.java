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
package com.microsoft.windowsazure.storage;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.junit.Test;

import com.microsoft.windowsazure.storage.blob.CloudBlobClient;
import com.microsoft.windowsazure.storage.blob.CloudBlobContainer;
import com.microsoft.windowsazure.storage.blob.CloudBlobDirectory;
import com.microsoft.windowsazure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.storage.blob.CloudPageBlob;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.queue.CloudQueue;
import com.microsoft.windowsazure.storage.queue.CloudQueueClient;
import com.microsoft.windowsazure.storage.table.CloudTable;
import com.microsoft.windowsazure.storage.table.CloudTableClient;

public class StorageUriTests extends TestBase {

    private static final String ACCOUNT_NAME = "account";
    private static final String SECONDARY_SUFFIX = "-secondary";
    private static final String ENDPOINT_SUFFIX = ".core.windows.net";
    private static final String BLOB_SERVICE = ".blob";
    private static final String QUEUE_SERVICE = ".queue";
    private static final String TABLE_SERVICE = ".table";

    @Test
    public void testStorageUriWithTwoUris() throws URISyntaxException {
        URI primaryClientUri = new URI("http://" + ACCOUNT_NAME + BLOB_SERVICE + ENDPOINT_SUFFIX);
        URI primaryContainerUri = new URI(primaryClientUri + "container");
        URI secondaryClientUri = new URI("http://" + ACCOUNT_NAME + SECONDARY_SUFFIX + BLOB_SERVICE + ENDPOINT_SUFFIX);
        URI dummyClientUri = new URI("http://" + ACCOUNT_NAME + "-dummy" + BLOB_SERVICE + ENDPOINT_SUFFIX);

        StorageUri singleUri = new StorageUri(primaryClientUri);
        assertTrue(primaryClientUri.equals(singleUri.getPrimaryUri()));
        assertNull(singleUri.getSecondaryUri());

        StorageUri singleUri2 = new StorageUri(primaryClientUri);
        assertTrue(singleUri.equals(singleUri2));

        StorageUri singleUri3 = new StorageUri(secondaryClientUri);
        assertFalse(singleUri.equals(singleUri3));

        StorageUri multiUri = new StorageUri(primaryClientUri, secondaryClientUri);
        assertTrue(primaryClientUri.equals(multiUri.getPrimaryUri()));
        assertTrue(secondaryClientUri.equals(multiUri.getSecondaryUri()));
        assertFalse(multiUri.equals(singleUri));

        StorageUri multiUri2 = new StorageUri(primaryClientUri, secondaryClientUri);
        assertTrue(multiUri.equals(multiUri2));

        try {
            new StorageUri(primaryClientUri, primaryContainerUri);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.STORAGE_URI_MUST_MATCH, ex.getMessage());
        }

        StorageUri multiUri3 = new StorageUri(primaryClientUri, dummyClientUri);
        assertFalse(multiUri.equals(multiUri3));

        StorageUri multiUri4 = new StorageUri(dummyClientUri, secondaryClientUri);
        assertFalse(multiUri.equals(multiUri4));

        StorageUri multiUri5 = new StorageUri(secondaryClientUri, primaryClientUri);
        assertFalse(multiUri.equals(multiUri5));
    }

    @Test
    public void testCloudStorageAccountWithStorageUri() throws URISyntaxException, InvalidKeyException {
        StorageUri blobEndpoint = new StorageUri(new URI("http://" + ACCOUNT_NAME + BLOB_SERVICE + ENDPOINT_SUFFIX),
                new URI("http://" + ACCOUNT_NAME + SECONDARY_SUFFIX + BLOB_SERVICE + ENDPOINT_SUFFIX));

        StorageUri queueEndpoint = new StorageUri(new URI("http://" + ACCOUNT_NAME + QUEUE_SERVICE + ENDPOINT_SUFFIX),
                new URI("http://" + ACCOUNT_NAME + SECONDARY_SUFFIX + QUEUE_SERVICE + ENDPOINT_SUFFIX));

        StorageUri tableEndpoint = new StorageUri(new URI("http://" + ACCOUNT_NAME + TABLE_SERVICE + ENDPOINT_SUFFIX),
                new URI("http://" + ACCOUNT_NAME + SECONDARY_SUFFIX + TABLE_SERVICE + ENDPOINT_SUFFIX));

        CloudStorageAccount account = CloudStorageAccount.parse(String.format(
                "DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=dummyKey", ACCOUNT_NAME));
        assertTrue(blobEndpoint.equals(account.getBlobStorageUri()));
        assertTrue(queueEndpoint.equals(account.getQueueStorageUri()));
        assertTrue(tableEndpoint.equals(account.getTableStorageUri()));

        assertTrue(blobEndpoint.equals(account.createCloudBlobClient().getStorageUri()));
        assertTrue(queueEndpoint.equals(account.createCloudQueueClient().getStorageUri()));
        assertTrue(tableEndpoint.equals(account.createCloudTableClient().getStorageUri()));

        assertTrue(blobEndpoint.getPrimaryUri().equals(account.getBlobEndpoint()));
        assertTrue(queueEndpoint.getPrimaryUri().equals(account.getQueueEndpoint()));
        assertTrue(tableEndpoint.getPrimaryUri().equals(account.getTableEndpoint()));
    }

    @Test
    public void testBlobTypesWithStorageUri() throws StorageException, URISyntaxException {
        CloudBlobClient blobClient = TestBase.createCloudBlobClient();
        StorageUri endpoint = new StorageUri(new URI("http://" + ACCOUNT_NAME + BLOB_SERVICE + ENDPOINT_SUFFIX), new URI(
                "http://" + ACCOUNT_NAME + SECONDARY_SUFFIX + BLOB_SERVICE + ENDPOINT_SUFFIX));

        CloudBlobClient client = new CloudBlobClient(endpoint, blobClient.getCredentials());
        assertTrue(endpoint.equals(client.getStorageUri()));
        assertTrue(endpoint.getPrimaryUri().equals(client.getEndpoint()));

        StorageUri containerUri = new StorageUri(new URI(endpoint.getPrimaryUri() + "/container"), new URI(
                endpoint.getSecondaryUri() + "/container"));

        CloudBlobContainer container = client.getContainerReference("container");
        assertTrue(containerUri.equals(container.getStorageUri()));
        assertTrue(containerUri.getPrimaryUri().equals(container.getUri()));
        assertTrue(endpoint.equals(container.getServiceClient().getStorageUri()));

        container = new CloudBlobContainer(containerUri, client);
        assertTrue(containerUri.equals(container.getStorageUri()));
        assertTrue(containerUri.getPrimaryUri().equals(container.getUri()));
        assertTrue(endpoint.equals(container.getServiceClient().getStorageUri()));

        StorageUri directoryUri = new StorageUri(new URI(containerUri.getPrimaryUri() + "/directory/"), new URI(
                containerUri.getSecondaryUri() + "/directory/"));

        StorageUri subdirectoryUri = new StorageUri(new URI(directoryUri.getPrimaryUri() + "subdirectory/"), new URI(
                directoryUri.getSecondaryUri() + "subdirectory/"));

        CloudBlobDirectory directory = container.getDirectoryReference("directory");
        assertTrue(directoryUri.equals(directory.getStorageUri()));
        assertTrue(directoryUri.getPrimaryUri().equals(directory.getUri()));
        assertNull(directory.getParent());
        assertTrue(containerUri.equals(directory.getContainer().getStorageUri()));
        assertTrue(endpoint.equals(directory.getServiceClient().getStorageUri()));

        CloudBlobDirectory subdirectory = directory.getSubDirectoryReference("subdirectory");
        assertTrue(subdirectoryUri.equals(subdirectory.getStorageUri()));
        assertTrue(subdirectoryUri.getPrimaryUri().equals(subdirectory.getUri()));
        assertTrue(directoryUri.equals(subdirectory.getParent().getStorageUri()));
        assertTrue(containerUri.equals(subdirectory.getContainer().getStorageUri()));
        assertTrue(endpoint.equals(subdirectory.getServiceClient().getStorageUri()));

        StorageUri blobUri = new StorageUri(new URI(subdirectoryUri.getPrimaryUri() + "blob"), new URI(
                subdirectoryUri.getSecondaryUri() + "blob"));

        CloudBlockBlob blockBlob = subdirectory.getBlockBlobReference("blob");
        assertTrue(blobUri.equals(blockBlob.getStorageUri()));
        assertTrue(blobUri.getPrimaryUri().equals(blockBlob.getUri()));
        assertTrue(subdirectoryUri.equals(blockBlob.getParent().getStorageUri()));
        assertTrue(containerUri.equals(blockBlob.getContainer().getStorageUri()));
        assertTrue(endpoint.equals(blockBlob.getServiceClient().getStorageUri()));

        blockBlob = new CloudBlockBlob(blobUri, null, client);
        assertTrue(blobUri.equals(blockBlob.getStorageUri()));
        assertTrue(blobUri.getPrimaryUri().equals(blockBlob.getUri()));
        assertTrue(subdirectoryUri.equals(blockBlob.getParent().getStorageUri()));
        assertTrue(containerUri.equals(blockBlob.getContainer().getStorageUri()));
        assertTrue(endpoint.equals(blockBlob.getServiceClient().getStorageUri()));

        CloudPageBlob pageBlob = subdirectory.getPageBlobReference("blob");
        assertTrue(blobUri.equals(pageBlob.getStorageUri()));
        assertTrue(blobUri.getPrimaryUri().equals(pageBlob.getUri()));
        assertTrue(subdirectoryUri.equals(pageBlob.getParent().getStorageUri()));
        assertTrue(containerUri.equals(pageBlob.getContainer().getStorageUri()));
        assertTrue(endpoint.equals(pageBlob.getServiceClient().getStorageUri()));

        pageBlob = new CloudPageBlob(blobUri, null, client);
        assertTrue(blobUri.equals(pageBlob.getStorageUri()));
        assertTrue(blobUri.getPrimaryUri().equals(pageBlob.getUri()));
        assertTrue(subdirectoryUri.equals(pageBlob.getParent().getStorageUri()));
        assertTrue(containerUri.equals(pageBlob.getContainer().getStorageUri()));
        assertTrue(endpoint.equals(pageBlob.getServiceClient().getStorageUri()));
    }

    @Test
    public void testQueueTypesWithStorageUri() throws URISyntaxException, StorageException {
        CloudQueueClient queueClient = TestBase.createCloudQueueClient();
        StorageUri endpoint = new StorageUri(new URI("http://" + ACCOUNT_NAME + QUEUE_SERVICE + ENDPOINT_SUFFIX),
                new URI("http://" + ACCOUNT_NAME + SECONDARY_SUFFIX + QUEUE_SERVICE + ENDPOINT_SUFFIX));

        CloudQueueClient client = new CloudQueueClient(endpoint, queueClient.getCredentials());
        assertTrue(endpoint.equals(client.getStorageUri()));
        assertTrue(endpoint.getPrimaryUri().equals(client.getEndpoint()));

        StorageUri queueUri = new StorageUri(new URI(endpoint.getPrimaryUri() + "/queue"), new URI(
                endpoint.getSecondaryUri() + "/queue"));

        CloudQueue queue = client.getQueueReference("queue");
        assertTrue(queueUri.equals(queue.getStorageUri()));
        assertTrue(queueUri.getPrimaryUri().equals(queue.getUri()));
        assertTrue(endpoint.equals(queue.getServiceClient().getStorageUri()));

        queue = new CloudQueue(queueUri, client);
        assertTrue(queueUri.equals(queue.getStorageUri()));
        assertTrue(queueUri.getPrimaryUri().equals(queue.getUri()));
        assertTrue(endpoint.equals(queue.getServiceClient().getStorageUri()));
    }

    @Test
    public void testTableTypesWithStorageUri() throws URISyntaxException, StorageException {
        CloudTableClient tableClient = TestBase.createCloudTableClient();
        StorageUri endpoint = new StorageUri(new URI("http://" + ACCOUNT_NAME + TABLE_SERVICE + ENDPOINT_SUFFIX),
                new URI("http://" + ACCOUNT_NAME + SECONDARY_SUFFIX + TABLE_SERVICE + ENDPOINT_SUFFIX));

        CloudTableClient client = new CloudTableClient(endpoint, tableClient.getCredentials());
        assertTrue(endpoint.equals(client.getStorageUri()));
        assertTrue(endpoint.getPrimaryUri().equals(client.getEndpoint()));

        StorageUri tableUri = new StorageUri(new URI(endpoint.getPrimaryUri() + "/table"), new URI(
                endpoint.getSecondaryUri() + "/table"));

        CloudTable table = client.getTableReference("table");
        assertTrue(tableUri.equals(table.getStorageUri()));
        assertTrue(tableUri.getPrimaryUri().equals(table.getUri()));
        assertTrue(endpoint.equals(table.getServiceClient().getStorageUri()));

        table = new CloudTable(tableUri, client);
        assertTrue(tableUri.equals(table.getStorageUri()));
        assertTrue(tableUri.getPrimaryUri().equals(table.getUri()));
        assertTrue(endpoint.equals(table.getServiceClient().getStorageUri()));
    }
}
