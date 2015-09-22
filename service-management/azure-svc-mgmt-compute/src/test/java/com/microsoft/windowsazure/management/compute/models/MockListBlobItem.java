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

import com.microsoft.azure.storage.blob.ListBlobItem;

import java.net.URI;

public class MockListBlobItem{
    public MockCloudPageBlob pageBlob;
    public ListBlobItem blobItem;
    public Boolean isMocked = false;
    
    public MockListBlobItem(MockCloudPageBlob pageBlob, ListBlobItem blobItem, Boolean isMocked) {
        this.pageBlob = pageBlob;
        this.blobItem = blobItem;
        this.isMocked = isMocked;
    }
    
    public URI getUri() {
        if (isMocked) {
            return pageBlob.getUri();
        } else {
            return blobItem.getUri();
        }
    }
}
