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

package com.microsoft.windowsazure.management.compute.models;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.blob.CloudBlobContainer;
import com.microsoft.windowsazure.storage.blob.ListBlobItem;

public class MockCloudBlobContainer {
    public CloudBlobContainer blobContainer;
    public Boolean isMocked = false;
    public String containerName;
    public String blobName = null;
    
    public MockCloudBlobContainer(CloudBlobContainer blobContainer, boolean isMocked, String containerName) {
        this.blobContainer = blobContainer;
        this.isMocked = isMocked;
        this.containerName = containerName;
    }
    
    public boolean createIfNotExists() throws StorageException {
        if (isMocked) {
            return true;
        } else {
            return blobContainer.createIfNotExists();
        }
    }
    
    public String getName() {
        if (isMocked) {
            return containerName;
        } else {
            return blobContainer.getName();
        }
    }
    
    public MockCloudPageBlob getPageBlobReference(String blobName) throws URISyntaxException, StorageException{
        this.blobName = blobName;
        if (isMocked) {
            return new MockCloudPageBlob(null, true, blobName);
        } else {
            return new MockCloudPageBlob(blobContainer.getPageBlobReference(blobName), false, blobName);
        }
    }
    
    public Iterable<MockListBlobItem> listBlobs() throws StorageException {
        List<MockListBlobItem> items = new ArrayList<MockListBlobItem>();
        if (isMocked) {
            items.add(new MockListBlobItem(new MockCloudPageBlob(null, true, blobName), null, true));
        } else {
            Iterable<ListBlobItem> realItems = blobContainer.listBlobs();
            for (ListBlobItem c : realItems) {
                items.add(new MockListBlobItem(null, c, false));
            }
        }
        return items;
    }

    public long breakLease(Integer breakPeriodInSeconds) throws StorageException {
        if (isMocked) {
            return 0;
        } else {
            return blobContainer.breakLease(breakPeriodInSeconds);
        }
    }

    public void delete() throws StorageException {
        if (isMocked) {
            return;
        } else {
            blobContainer.delete();
        }
    }

    public Boolean exists() throws StorageException {
        if (isMocked) {
            return false;
        } else {
            return blobContainer.exists();
        }
    }

    public URI getUri() {
        try {
            return new URI("http://www.windows.net");
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
