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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.windowsazure.storage.TestRunners.CloudTests;
import com.microsoft.windowsazure.storage.TestRunners.DevFabricTests;
import com.microsoft.windowsazure.storage.TestRunners.DevStoreTests;
import com.microsoft.windowsazure.storage.TestRunners.SlowTests;
import com.microsoft.windowsazure.storage.blob.BlobOutputStream;
import com.microsoft.windowsazure.storage.blob.BlobRequestOptions;
import com.microsoft.windowsazure.storage.blob.BlobTestBase;
import com.microsoft.windowsazure.storage.blob.CloudBlobClient;
import com.microsoft.windowsazure.storage.blob.CloudBlobContainer;
import com.microsoft.windowsazure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.Utility;
import com.microsoft.windowsazure.storage.queue.CloudQueue;
import com.microsoft.windowsazure.storage.queue.CloudQueueClient;
import com.microsoft.windowsazure.storage.table.CloudTable;
import com.microsoft.windowsazure.storage.table.CloudTableClient;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class GenericTests extends TestBase {

    @Test
    public void testUserAgentString() throws URISyntaxException, StorageException {
        // Test with a blob request
        CloudBlobClient blobClient = TestBase.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("container1");
        OperationContext sendingRequestEventContext = new OperationContext();
        sendingRequestEventContext.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                assertEquals(
                        Constants.HeaderConstants.USER_AGENT_PREFIX
                                + "/"
                                + Constants.HeaderConstants.USER_AGENT_VERSION
                                + " "
                                + String.format(Utility.LOCALE_US, "(JavaJRE %s; %s %s)",
                                        System.getProperty("java.version"),
                                        System.getProperty("os.name").replaceAll(" ", ""),
                                        System.getProperty("os.version")), ((HttpURLConnection) eventArg
                                .getConnectionObject()).getRequestProperty(Constants.HeaderConstants.USER_AGENT));
            }
        });
        container.exists(null, null, sendingRequestEventContext);

        // Test with a queue request
        CloudQueueClient queueClient = TestBase.createCloudQueueClient();
        CloudQueue queue = queueClient.getQueueReference("queue1");
        queue.exists(null, sendingRequestEventContext);

        // Test with a table request
        CloudTableClient tableClient = TestBase.createCloudTableClient();
        CloudTable table = tableClient.getTableReference("table1");
        table.exists(null, sendingRequestEventContext);
    }

    @Test
    public void testUserHeaders() throws URISyntaxException, StorageException {
        CloudBlobClient blobClient = TestBase.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("container1");
        OperationContext context = new OperationContext();

        // no user headers
        container.exists(null, null, context);

        // add user headers
        HashMap<String, String> userHeaders = new HashMap<String, String>();
        userHeaders.put("x-ms-foo", "bar");
        userHeaders.put("x-ms-hello", "value");
        context.setUserHeaders(userHeaders);
        StorageEvent<SendingRequestEvent> event = new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection connection = (HttpURLConnection) eventArg.getConnectionObject();
                assertNotNull(connection.getRequestProperty("x-ms-foo"));
                assertNotNull(connection.getRequestProperty("x-ms-hello"));
            }
        };

        context.getSendingRequestEventHandler().addListener(event);
        container.exists(null, null, context);

        // clear user headers
        userHeaders.clear();
        context.getSendingRequestEventHandler().removeListener(event);
        context.setUserHeaders(userHeaders);
        context.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection connection = (HttpURLConnection) eventArg.getConnectionObject();
                assertNull(connection.getRequestProperty("x-ms-foo"));
                assertNull(connection.getRequestProperty("x-ms-hello"));
            }
        });

        container.exists(null, null, context);
    }

    @Test
    public void testNullRetryPolicy() throws URISyntaxException, StorageException {
        CloudBlobClient blobClient = TestBase.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("container1");

        blobClient.setRetryPolicyFactory(null);
        container.exists();
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SecondaryTests.class })
    public void testMaximumExecutionTime() throws URISyntaxException, StorageException, IOException {
        OperationContext opContext = new OperationContext();
        setDelay(opContext, 2500);

        // set the maximum execution time
        BlobRequestOptions options = new BlobRequestOptions();
        options.setMaximumExecutionTimeInMs(2000);

        // set the location mode to secondary, secondary request should fail
        // so set the timeout low to save time failing (or fail with a timeout)
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        options.setTimeoutIntervalInMs(1000);

        CloudBlobClient blobClient = TestBase.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(generateRandomContainerName());

        try {
            // 1. download attributes will fail as the container does not exist
            // 2. the executor will attempt to retry as it is accessing secondary
            // 3. maximum execution time should prevent the retry from being made
            container.downloadAttributes(null, options, opContext);
            fail("Maximum execution time was reached but request did not fail.");
        }
        catch (StorageException e) {
            assertEquals(SR.MAXIMUM_EXCUTION_TIMEOUT_EXCEPTION, e.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SlowTests.class })
    public void testMaximumExecutionTimeBlobWrites() throws URISyntaxException, StorageException, IOException {
        byte[] buffer = BlobTestBase.getRandomBuffer(80 * 1024 * 1024);

        // set the maximum execution time
        BlobRequestOptions options = new BlobRequestOptions();
        options.setMaximumExecutionTimeInMs(5000);

        CloudBlobClient blobClient = TestBase.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(generateRandomContainerName());

        String blobName = "testBlob";
        final CloudBlockBlob blockBlobRef = container.getBlockBlobReference(blobName);
        blockBlobRef.setStreamWriteSizeInBytes(1 * 1024 * 1024);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        BlobOutputStream blobOutputStream = null;

        try {
            container.createIfNotExists();

            // make sure max timeout is thrown by Utility.writeToOutputStream() on upload
            try {
                blockBlobRef.upload(inputStream, buffer.length, null, options, null);
                fail("Maximum execution time was reached but request did not fail.");
            }
            catch (StorageException e) {
                assertEquals(SR.MAXIMUM_EXCUTION_TIMEOUT_EXCEPTION, e.getMessage());
            }
            catch (IOException e) {
                assertEquals(SR.MAXIMUM_EXCUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
            }
            assertFalse(blockBlobRef.exists());

            // make sure max timeout applies on a per service request basis if the user creates the stream
            // adds a delay so the first service request should fail
            OperationContext opContext = new OperationContext();
            setDelay(opContext, 6000);
            blobOutputStream = blockBlobRef.openOutputStream(null, options, opContext);
            try {
                blobOutputStream.write(inputStream, buffer.length);
                fail("Maximum execution time was reached but request did not fail.");
            }
            catch (StorageException e) {
                assertEquals(SR.MAXIMUM_EXCUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
            }
            catch (IOException e) {
                assertEquals(SR.MAXIMUM_EXCUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
            }
            finally {
                try {
                    blobOutputStream.close();
                }
                catch (IOException e) {
                    assertEquals(SR.MAXIMUM_EXCUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
                }
            }
            assertFalse(blockBlobRef.exists());

            // make sure max timeout applies on a per service request basis if the user creates the stream
            // adds a delay so the first service request should fail
            blobOutputStream = blockBlobRef.openOutputStream(null, options, opContext);
            try {
                blobOutputStream.write(buffer);
                fail("Maximum execution time was reached but request did not fail.");
            }
            catch (IOException e) {
                assertEquals(SR.MAXIMUM_EXCUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
            }
            finally {
                try {
                    blobOutputStream.close();
                }
                catch (IOException e) {
                    assertEquals(SR.MAXIMUM_EXCUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
                }
            }
            assertFalse(blockBlobRef.exists());

            // make sure max timeout applies on a per service request basis only if the user creates the stream
            // should succeed as even if all requests would exceed the timeout, each one won't
            blobOutputStream = blockBlobRef.openOutputStream(null, options, null);
            try {
                blobOutputStream.write(inputStream, buffer.length);
            }
            finally {
                blobOutputStream.close();
            }
            assertTrue(blockBlobRef.exists());
        }
        finally {
            inputStream.close();
            container.deleteIfExists();
        }
    }

    private static String generateRandomContainerName() {
        String containerName = "container" + UUID.randomUUID().toString();
        return containerName.replace("-", "");
    }

    private void setDelay(final OperationContext ctx, final int timeInMs) {

        ctx.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                try {
                    Thread.sleep(timeInMs);
                }
                catch (InterruptedException e) {
                    // do nothing
                }
            }
        });
    }
}
