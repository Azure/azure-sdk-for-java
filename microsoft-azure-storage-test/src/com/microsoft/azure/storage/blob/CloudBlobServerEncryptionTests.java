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


import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RequestCompletedEvent;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category({ CloudTests.class, DevFabricTests.class, DevStoreTests.class })
//@Ignore
/* These test only works on accounts with server-side encryption enabled. */
public class CloudBlobServerEncryptionTests {

    private CloudBlobContainer container;
    private CloudBlockBlob blob;
    private boolean requestFound;

    @Before
    public void blobEncryptionTestMethodSetup() throws URISyntaxException, StorageException, IOException {
        this.container = BlobTestHelper.getRandomContainerReference();
        this.container.create();
        this.blob = this.container.getBlockBlobReference(BlobTestHelper.generateRandomBlobNameWithPrefix("testBlob"));
        this.blob.uploadText("test");
    }

    @After
    public void blobEncryptionTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }

    @Test
    public void testBlobAttributesEncryption() throws URISyntaxException, StorageException, IOException {
        this.blob.downloadAttributes();
        assertTrue(this.blob.getProperties().isServerEncrypted());

        CloudBlockBlob testBlob = this.container.getBlockBlobReference(this.blob.getName());
        testBlob.downloadText();
        assertTrue(testBlob.getProperties().isServerEncrypted());
    }

    @Test
    public void testListBlobsEncryption() throws URISyntaxException, StorageException, IOException {
        boolean blobFound = false;

        for (ListBlobItem b : this.container.listBlobs()) {
            CloudBlob blob = (CloudBlob) b;
            assertTrue(blob.getProperties().isServerEncrypted());

            blobFound = true;
        }

        assertTrue(blobFound);
    }

    @Test
    public void testBlobEncryption() throws URISyntaxException, StorageException, IOException {
        this.requestFound = false;
        
        OperationContext ctxt = new OperationContext();
        ctxt.getRequestCompletedEventHandler().addListener(new StorageEvent<RequestCompletedEvent>() {
            @Override
            public void eventOccurred(RequestCompletedEvent eventArg) {
                assertTrue(eventArg.getRequestResult().isRequestServiceEncrypted());
                CloudBlobServerEncryptionTests.this.requestFound = true;
            }
        });

        this.blob.uploadText("test", null, null, null, ctxt);
        assertTrue(this.requestFound);
    }
}