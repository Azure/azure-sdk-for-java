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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudPageBlob;

public class MockCloudPageBlob {
    public CloudPageBlob pageBlob;
    public Boolean isMocked = false;
    public String blobName;
    
    public MockCloudPageBlob(CloudPageBlob pageBlob, boolean isMocked, String blobName) {
        this.pageBlob = pageBlob;
        this.isMocked = isMocked;
        this.blobName = blobName;
    }
    
    public void upload(InputStream sourceStream, long length) throws StorageException, IOException{
        if (isMocked) {
            return;
        } else {
            pageBlob.upload(sourceStream, length);
        }
    }

    public URI getUri() {
        if (isMocked) {
            try {
                return new URI("http://www." + blobName + ".net");
            } catch (URISyntaxException e) {
                return null;
            } 
        } else {
            return pageBlob.getUri();
        }
    }

}
