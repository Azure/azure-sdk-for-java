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
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResponseReceivedEvent;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.core.SR;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * Blob Output Stream Tests
 */
@Category(CloudTests.class)
public class BlobOutputStreamTests {

    protected CloudBlobContainer container;

    @Before
    public void blobOutputStreamTestMethodSetup() throws URISyntaxException, StorageException {
        this.container = BlobTestHelper.getRandomContainerReference();
        this.container.create();
    }

    @After
    public void blobOutputStreamTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }

    @Test
    public void testEmpty() throws URISyntaxException, StorageException, IOException {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");

        CloudBlockBlob blockBlob = this.container.getBlockBlobReference(blobName);
        BlobOutputStream str = blockBlob.openOutputStream();
        str.close();
        
        CloudBlockBlob blockBlob2 = this.container.getBlockBlobReference(blobName);
        blockBlob2.downloadAttributes();
        assertEquals(0, blockBlob2.getProperties().getLength());
        
        ArrayList<BlockEntry> blocks = blockBlob2.downloadBlockList(BlockListingFilter.ALL, null, null, null);
        assertEquals(0, blocks.size());
    }

    @Test
    public void testClose() throws URISyntaxException, StorageException, IOException {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");

        CloudBlockBlob blockBlob = this.container.getBlockBlobReference(blobName);
        BlobOutputStream str = blockBlob.openOutputStream();
        str.close();
        
        try {
            str.close();
            fail("Can't close twice.");
        } catch(IOException e) {
            assertEquals(SR.STREAM_CLOSED, e.getMessage());
        }
        
        str = blockBlob.openOutputStream();
        str.write(8);
        ArrayList<BlockEntry> blocks = blockBlob.downloadBlockList(BlockListingFilter.ALL, null, null, null);
        assertEquals(0, blocks.size());
        
        str.close();
        blocks = blockBlob.downloadBlockList(BlockListingFilter.COMMITTED, null, null, null);
        assertEquals(1, blocks.size());
    }

    @Test
    public void testWithAccessCondition() throws URISyntaxException, StorageException, IOException {
        int blobLengthToUse = 8 * 512;
        byte[] buffer = BlobTestHelper.getRandomBuffer(blobLengthToUse);
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        AccessCondition accessCondition = AccessCondition.generateIfNotModifiedSinceCondition(new Date()); 
        
        CloudBlockBlob blockBlob = this.container.getBlockBlobReference(blobName);
        BlobOutputStream blobOutputStream = blockBlob.openOutputStream(accessCondition, null, null);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        blobOutputStream.write(inputStream, 512);

        inputStream = new ByteArrayInputStream(buffer, 512, 3 * 512);
        blobOutputStream.write(inputStream, 3 * 512);

        blobOutputStream.close();

        byte[] result = new byte[blobLengthToUse];
        blockBlob.downloadToByteArray(result, 0);

        int i = 0;
        for (; i < 4 * 512; i++) {
            assertEquals(buffer[i], result[i]);
        }

        for (; i < 8 * 512; i++) {
            assertEquals(0, result[i]);
        }
    }

    @Test
    public void testWriteStream() throws URISyntaxException, StorageException, IOException {
        int blobLengthToUse = 8 * 512;
        byte[] buffer = BlobTestHelper.getRandomBuffer(blobLengthToUse);
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");

        CloudBlockBlob blockBlob = this.container.getBlockBlobReference(blobName);
        BlobOutputStream blobOutputStream = blockBlob.openOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        blobOutputStream.write(inputStream, 512);

        inputStream = new ByteArrayInputStream(buffer, 512, 3 * 512);
        blobOutputStream.write(inputStream, 3 * 512);

        blobOutputStream.close();

        byte[] result = new byte[blobLengthToUse];
        blockBlob.downloadToByteArray(result, 0);

        int i = 0;
        for (; i < 4 * 512; i++) {
            assertEquals(buffer[i], result[i]);
        }

        for (; i < 8 * 512; i++) {
            assertEquals(0, result[i]);
        }
    }

    @Test
    public void testFlush() throws Exception {
        CloudBlockBlob blockBlob = this.container.getBlockBlobReference(
                BlobTestHelper.generateRandomBlobNameWithPrefix("flush"));
        
        OperationContext ctx = new OperationContext();
        ctx.getResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {
            
            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
                try {
                    HttpURLConnection con = (HttpURLConnection) eventArg.getConnectionObject();
                    if ("511".equals(con.getRequestProperty(Constants.HeaderConstants.CONTENT_LENGTH))) {
                        Thread.sleep(3000);   
                    }
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        });
        
        BlobOutputStream blobOutputStream = blockBlob.openOutputStream(null, null, ctx);
        
        ExecutorService threadExecutor = Executors.newFixedThreadPool(1);
        
        byte[] buffer = BlobTestHelper.getRandomBuffer(511);
        blobOutputStream.write(buffer);
        
        Future<Void> future = threadExecutor.submit(new FlushTask(blobOutputStream));
        Thread.sleep(1000);
        
        buffer = BlobTestHelper.getRandomBuffer(513);
        blobOutputStream.write(buffer);
        
        // Writes complete when the upload is dispatched (not when the upload completes and flush must
        // wait for upload1 to complete. So, flush should finish last and writes should finish in order.
        while(!future.isDone()) {
            Thread.sleep(500);
        }
        
        // After flush we should see the first upload
        ArrayList<BlockEntry> blocks = blockBlob.downloadBlockList(BlockListingFilter.UNCOMMITTED, null, null, null);
        assertEquals(1, blocks.size());
        assertEquals(511, blocks.get(0).getSize());
        
        // After close we should see the second upload
        blobOutputStream.close();
        blocks = blockBlob.downloadBlockList(BlockListingFilter.COMMITTED, null, null, null);
        assertEquals(2, blocks.size());
        assertEquals(513, blocks.get(1).getSize());
    }

    @Test
    public void testWritesDoubleConcurrency() throws URISyntaxException, StorageException, IOException,
            InterruptedException {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("concurrency");
        CloudBlockBlob blockBlob = this.container.getBlockBlobReference(blobName);

        // setup the blob output stream with a concurrency of 5
        BlobRequestOptions options = new BlobRequestOptions();
        options.setConcurrentRequestCount(5);
        BlobOutputStream blobOutputStream = blockBlob.openOutputStream(null, options, null);

        // set up the execution completion service
        ExecutorService threadExecutor = Executors.newFixedThreadPool(5);
        ExecutorCompletionService<Void> completion = new ExecutorCompletionService<Void>(threadExecutor);
        
        int tasks = 10;
        int writes = 10;
        int length = 512;
        
        // submit tasks to write and flush many blocks
        for (int i = 0; i < tasks; i++) {
            completion.submit(new WriteTask(blobOutputStream, length, writes, 4 /*flush period*/));
        }

        // wait for all tasks to complete
        for (int i = 0; i < tasks; i++) {
            completion.take();
        }

        // shut down the thread executor for this method
        threadExecutor.shutdown();

        // check that blocks were committed
        ArrayList<BlockEntry> blocks = blockBlob.downloadBlockList(BlockListingFilter.UNCOMMITTED, null, null, null);
        assertTrue(blocks.size() != 0);
        
        // close the stream and check that the blob is the expected length
        blobOutputStream.close();
        blockBlob.downloadAttributes();
        assertTrue(blockBlob.getProperties().getLength() == length*writes*tasks);
    }

    @Test
    public void testWritesNoConcurrency() throws URISyntaxException, StorageException, IOException {
        int writes = 10;
        
        this.smallPutThresholdHelper(Constants.MB, writes, null);
        this.writeFlushHelper(512, writes, null, 1);
        this.writeFlushHelper(512, writes, null, 4);
        this.writeFlushHelper(512, writes, null, writes+1);
    }
    
    @Test
    public void testWritesConcurrency() throws URISyntaxException, StorageException, IOException {
        int writes = 10;
        
        BlobRequestOptions options = new BlobRequestOptions();
        options.setConcurrentRequestCount(5); 
        
        this.smallPutThresholdHelper(Constants.MB, writes, options);
        this.writeFlushHelper(512, writes, options, 1);
        this.writeFlushHelper(512, writes, options, 4);
        this.writeFlushHelper(512, writes, options, writes+1);
    }
    
    private void smallPutThresholdHelper(int length, int writes, BlobRequestOptions options) 
            throws URISyntaxException, StorageException, IOException {    
        byte[] buffer = BlobTestHelper.getRandomBuffer(length*writes);
        
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("concurrency");
        CloudBlockBlob blockBlob = this.container.getBlockBlobReference(blobName);
        blockBlob.setStreamWriteSizeInBytes(length);
              
        BlobOutputStream blobOutputStream = blockBlob.openOutputStream(null, options, null);      
        for (int i = 0; i < writes; i ++) {
            blobOutputStream.write(buffer, i*length, length);
        }
        
        blobOutputStream.flush();
        ArrayList<BlockEntry> blocks = blockBlob.downloadBlockList(BlockListingFilter.UNCOMMITTED, null, null, null);
        assertEquals(writes, blocks.size());
        
        blobOutputStream.close();
        blocks = blockBlob.downloadBlockList(BlockListingFilter.COMMITTED, null, null, null);
        assertEquals(writes, blocks.size());
        
        byte[] outBuffer = new byte[writes*length];
        blockBlob.downloadToByteArray(outBuffer, 0);
        for (int i = 0; i < length*writes; i ++) {
            assertEquals(buffer[i], outBuffer[i]);
        }
    }
    
    private void writeFlushHelper(int length, int writes, BlobRequestOptions options, int flushPeriod)
            throws URISyntaxException, StorageException, IOException {
        byte[] buffer = BlobTestHelper.getRandomBuffer(length*writes);
        
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("concurrency");
        CloudBlockBlob blockBlob = this.container.getBlockBlobReference(blobName);
        
        ArrayList<BlockEntry> blocks;
        BlobOutputStream blobOutputStream = blockBlob.openOutputStream(null, options, null);      
        for (int i = 0; i < writes; i ++) {
            blobOutputStream.write(buffer, i*length, length);
            
            if ((i+1)%flushPeriod == 0) {
                blobOutputStream.flush();
                blocks = blockBlob.downloadBlockList(BlockListingFilter.UNCOMMITTED, null, null, null);
                assertEquals((int)Math.ceil((i+1)/flushPeriod), blocks.size());   
            }
        }
        
        blobOutputStream.close();
        blocks = blockBlob.downloadBlockList(BlockListingFilter.COMMITTED, null, null, null);
        
        int flushRequired = writes-flushPeriod < 0 || writes%flushPeriod == 0 ? 0 : 1;
        double expected = Math.ceil(((double)writes+flushRequired)/flushPeriod);
        assertEquals((long)expected, blocks.size());
        
        byte[] outBuffer = new byte[writes*length];
        blockBlob.downloadToByteArray(outBuffer, 0);
        for (int i = 0; i < length*writes; i ++) {
            assertEquals(buffer[i], outBuffer[i]);
        }
    }
    
    private static class FlushTask implements Callable<Void> {
        final BlobOutputStream stream;
        
        public FlushTask(BlobOutputStream stream) {
            this.stream = stream;
        }
        
        @Override
        public Void call() {
            try {
                stream.flush();
            } catch (IOException e) {
                fail("The flush should succeed.");
            }
            return null;
        }
    }
    
    private class WriteTask implements Callable<Void> {
        final int length;
        final int writes;
        final int flushPeriod;
        final BlobOutputStream blobOutputStream;
        
        public WriteTask(BlobOutputStream blobOutputStream, int length, int writes, int flushPeriod) {
            this.length = length;
            this.writes = writes;
            this.flushPeriod = flushPeriod;
            this.blobOutputStream = blobOutputStream;
        }
        
        @Override
        public Void call() {
            try {
                byte[] buffer = BlobTestHelper.getRandomBuffer(this.length*this.writes);
                for (int i = 0; i < writes; i ++) {
                    this.blobOutputStream.write(buffer, i*this.length, this.length);
                    
                    if ((i+1)%flushPeriod == 0) {
                        this.blobOutputStream.flush();
                    }
                }
            } catch (Exception e) {
                fail("flushHelper should succeed.");
            }
            return null;
        }
    }
}
