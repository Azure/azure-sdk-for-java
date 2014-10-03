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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.blob.CloudBlobClient;
import com.microsoft.windowsazure.storage.blob.CloudBlobContainer;

public class MockCloudBlobClient {
    public CloudBlobClient blobClient;
    public Boolean isMocked = false;
    public String containerName = null;
    
    public MockCloudBlobClient(CloudBlobClient blobClient, Boolean isMocked) {
        this.blobClient = blobClient;
        this.isMocked = isMocked;
    }
    
    public MockCloudBlobContainer getContainerReference(String containerName) throws URISyntaxException, StorageException {
        this.containerName = containerName;
        if (isMocked) {
            return new MockCloudBlobContainer(null, true, containerName);
        } else {
            return new MockCloudBlobContainer(blobClient.getContainerReference(containerName), false, containerName);
        }
    }

    public Iterable<MockCloudBlobContainer> listContainers(String prefix) throws StorageException {
        List<MockCloudBlobContainer> containers = new ArrayList<MockCloudBlobContainer>();
        if (!isMocked) {
            Iterable<CloudBlobContainer> realContainers = blobClient.listContainers(prefix);
            for (CloudBlobContainer c : realContainers) {
                containers.add(new MockCloudBlobContainer(c, false, c.getName()));
            }
        } else {
            containers.add(new MockCloudBlobContainer(null, true, containerName));
        }
        return containers;
    }
}
