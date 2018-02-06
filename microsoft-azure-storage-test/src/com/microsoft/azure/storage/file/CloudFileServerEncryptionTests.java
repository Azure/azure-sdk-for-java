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
public class CloudFileServerEncryptionTests {

    private CloudFileShare share;
    private CloudFileDirectory dir;
    private CloudFile file;
    private boolean requestFound;

    @Before
    public void fileEncryptionTestMethodSetup() throws URISyntaxException, StorageException, IOException {
        this.share = FileTestHelper.getRandomShareReference();
        this.share.create();
        this.dir = this.share.getRootDirectoryReference().getDirectoryReference("dir");
        this.dir.create();
        this.file = this.share.getRootDirectoryReference().getFileReference("file");
        this.file.uploadText("text");
    }

    @After
    public void fileEncryptionTestMethodTearDown() throws StorageException {
        this.share.deleteIfExists();
    }

    @Test
    public void testFileAttributesEncryption() throws URISyntaxException, StorageException, IOException {
        this.file.downloadAttributes();
        assertTrue(this.file.getProperties().isServerEncrypted());

        CloudFile testFile = this.share.getRootDirectoryReference().getFileReference("file");
        testFile.downloadText();
        assertTrue(testFile.getProperties().isServerEncrypted());
    }
    
    @Test
    public void testDirectoryAttributesEncryption() throws URISyntaxException, StorageException, IOException {
        assertFalse(this.dir.getProperties().isServerEncrypted());

        CloudFileDirectory testDir = this.share.getRootDirectoryReference().getDirectoryReference("dir");
        testDir.downloadAttributes();
        assertTrue(testDir.getProperties().isServerEncrypted());
    }

    @Test
    public void testCloudFileUploadEncryption() throws URISyntaxException, StorageException, IOException {
        this.requestFound = false;

        OperationContext ctxt = new OperationContext();
        ctxt.getRequestCompletedEventHandler().addListener(new StorageEvent<RequestCompletedEvent>() {
            @Override
            public void eventOccurred(RequestCompletedEvent eventArg) {
                assertTrue(eventArg.getRequestResult().isRequestServiceEncrypted());
                CloudFileServerEncryptionTests.this.requestFound = true;
            }
        });

        this.file.uploadText("test", null, null, null, ctxt);
        assertTrue(this.requestFound);

        this.requestFound = false;
        this.file.uploadProperties(null, null, ctxt);
        assertTrue(this.requestFound);

        this.requestFound = false;
        this.file.uploadMetadata(null, null, ctxt);
        assertTrue(this.requestFound);
    }

    @Test
    public void testCloudFileDirectoryEncryption() throws URISyntaxException, StorageException, IOException {
        this.requestFound = false;

        OperationContext ctxt = new OperationContext();
        ctxt.getRequestCompletedEventHandler().addListener(new StorageEvent<RequestCompletedEvent>() {
            @Override
            public void eventOccurred(RequestCompletedEvent eventArg) {
                assertTrue(eventArg.getRequestResult().isRequestServiceEncrypted());
                CloudFileServerEncryptionTests.this.requestFound = true;
            }
        });

        this.dir.uploadMetadata(null, null, ctxt);
        assertTrue(this.requestFound);

        this.requestFound = false;
        CloudFileDirectory dir2 = this.share.getRootDirectoryReference().getDirectoryReference("dir2");
        dir2.create(null, ctxt);
        assertTrue(this.requestFound);
    }
}