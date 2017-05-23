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

import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.BlobTestHelper;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.FileRequestOptions;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.QueueRequestOptions;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableRequestOptions;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.Assert.*;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class MaximumExecutionTimeTests {

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SecondaryTests.class })
    public void testBlobMaximumExecutionTime() throws URISyntaxException, StorageException {
        OperationContext opContext = new OperationContext();
        setDelay(opContext, 2500);

        // set the maximum execution time
        BlobRequestOptions options = new BlobRequestOptions();
        options.setMaximumExecutionTimeInMs(2000);

        // set the location mode to secondary, secondary request should fail
        // so set the timeout low to save time failing (or fail with a timeout)
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        options.setTimeoutIntervalInMs(1000);

        CloudBlobClient blobClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(generateRandomName("container"));

        try {
            // 1. download attributes will fail as the container does not exist
            // 2. the executor will attempt to retry as it is accessing secondary
            // 3. maximum execution time should prevent the retry from being made
            container.downloadAttributes(null, options, opContext);
            fail("Maximum execution time was reached but request did not fail.");
        }
        catch (StorageException e) {
            assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SecondaryTests.class })
    public void testFileMaximumExecutionTime() throws URISyntaxException, StorageException {
        OperationContext opContext = new OperationContext();
        setDelay(opContext, 2500);
        
        opContext.getResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {

            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
                // Set status code to 500 to force a retry
                eventArg.getRequestResult().setStatusCode(500);
            }
        });

        // set the maximum execution time
        FileRequestOptions options = new FileRequestOptions();
        options.setMaximumExecutionTimeInMs(2000);
        options.setTimeoutIntervalInMs(1000);

        CloudFileClient fileClient = TestHelper.createCloudFileClient();
        CloudFileShare share = fileClient.getShareReference(generateRandomName("share"));

        try {
            // 1. download attributes will fail as the share does not exist
            // 2. the executor will attempt to retry as we set the status code to 500
            // 3. maximum execution time should prevent the retry from being made
            share.downloadAttributes(null, options, opContext);
            fail("Maximum execution time was reached but request did not fail.");
        }
        catch (StorageException e) {
            assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SecondaryTests.class })
    public void testQueueMaximumExecutionTime() throws URISyntaxException, StorageException {
        OperationContext opContext = new OperationContext();
        setDelay(opContext, 2500);

        // set the maximum execution time
        QueueRequestOptions options = new QueueRequestOptions();
        options.setMaximumExecutionTimeInMs(2000);

        // set the location mode to secondary, secondary request should fail
        // so set the timeout low to save time failing (or fail with a timeout)
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        options.setTimeoutIntervalInMs(1000);

        CloudQueueClient queueClient = TestHelper.createCloudQueueClient();
        CloudQueue queue = queueClient.getQueueReference(generateRandomName("queue"));

        try {
            // 1. download attributes will fail as the queue does not exist
            // 2. the executor will attempt to retry as it is accessing secondary
            // 3. maximum execution time should prevent the retry from being made
            queue.downloadAttributes(options, opContext);
            fail("Maximum execution time was reached but request did not fail.");
        }
        catch (StorageException e) {
            assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SecondaryTests.class })
    public void testTableMaximumExecutionTime() throws URISyntaxException, StorageException {
        OperationContext opContext = new OperationContext();
        setDelay(opContext, 2500);
        
        opContext.getResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {

            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
                // Set status code to 500 to force a retry
                eventArg.getRequestResult().setStatusCode(500);
            }
        });

        // set the maximum execution time
        TableRequestOptions options = new TableRequestOptions();
        options.setMaximumExecutionTimeInMs(2000);
        options.setTimeoutIntervalInMs(1000);

        CloudTableClient tableClient = TestHelper.createCloudTableClient();
        CloudTable table = tableClient.getTableReference(generateRandomName("share"));

        try {
            // 1. insert entity will fail as the table does not exist
            // 2. the executor will attempt to retry as we set the status code to 500
            // 3. maximum execution time should prevent the retry from being made
            DynamicTableEntity ent = new DynamicTableEntity("partition", "row");
            TableOperation insert = TableOperation.insert(ent);
            table.execute(insert, options, opContext);
            fail("Maximum execution time was reached but request did not fail.");
        }
        catch (StorageException e) {
            assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getMessage());
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SlowTests.class })
    public void testMaximumExecutionTimeBlobWrites() throws URISyntaxException, StorageException, IOException {
        byte[] buffer = BlobTestHelper.getRandomBuffer(80 * 1024 * 1024);

        // set the maximum execution time
        BlobRequestOptions options = new BlobRequestOptions();
        options.setMaximumExecutionTimeInMs(5000);

        // set a lower put blob threshold so that we perform multiple put block requests that timeout
        options.setSingleBlobPutThresholdInBytes(32 * Constants.MB);

        CloudBlobClient blobClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(generateRandomName("container"));

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
                assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getMessage());
            }
            catch (IOException e) {
                assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
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
                assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
            }
            catch (IOException e) {
                assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
            }
            finally {
                try {
                    blobOutputStream.close();
                }
                catch (IOException e) {
                    assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
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
                assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
            }
            finally {
                try {
                    blobOutputStream.close();
                }
                catch (IOException e) {
                    assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
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

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, SlowTests.class })
    public void testMaximumExecutionTimeBlobByteArray() throws URISyntaxException, StorageException, IOException {
        int length = 10 * 1024 * 1024;
        byte[] uploadBuffer = BlobTestHelper.getRandomBuffer(length);
        byte[] downloadBuffer = new byte[length];

        // set a delay in sending request
        OperationContext opContext = new OperationContext();
        setDelay(opContext, 2500);

        // set the maximum execution time
        BlobRequestOptions options = new BlobRequestOptions();
        options.setMaximumExecutionTimeInMs(2000);

        CloudBlobClient blobClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(generateRandomName("container"));

        String blobName = "testBlob";
        final CloudBlockBlob blockBlobRef = container.getBlockBlobReference(blobName);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(uploadBuffer);

        try {
            container.createIfNotExists();

            blockBlobRef.upload(inputStream, length);
            assertTrue(blockBlobRef.exists());

            try {
                blockBlobRef.downloadToByteArray(downloadBuffer, 0, null, options, opContext);
                fail("Maximum execution time was reached but request did not fail.");
            }
            catch (StorageException e) {
                assertEquals(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, e.getCause().getMessage());
            }
        }
        finally {
            inputStream.close();
            container.deleteIfExists();
        }
    }

    private static String generateRandomName(String prefix) {
        String name = prefix + UUID.randomUUID().toString();
        return name.replace("-", "");
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