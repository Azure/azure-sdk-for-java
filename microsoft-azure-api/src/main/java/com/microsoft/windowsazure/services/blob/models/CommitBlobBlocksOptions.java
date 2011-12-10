/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.models;

import java.util.HashMap;

public class CommitBlobBlocksOptions extends BlobServiceOptions {
    private String blobContentType;
    private String blobContentEncoding;
    private String blobContentLanguage;
    private String blobContentMD5;
    private String blobCacheControl;
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private String leaseId;
    private AccessCondition accessCondition;

    @Override
    public CommitBlobBlocksOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getBlobContentType() {
        return blobContentType;
    }

    public CommitBlobBlocksOptions setBlobContentType(String blobContentType) {
        this.blobContentType = blobContentType;
        return this;
    }

    public String getBlobContentEncoding() {
        return blobContentEncoding;
    }

    public CommitBlobBlocksOptions setBlobContentEncoding(String blobContentEncoding) {
        this.blobContentEncoding = blobContentEncoding;
        return this;
    }

    public String getBlobContentLanguage() {
        return blobContentLanguage;
    }

    public CommitBlobBlocksOptions setBlobContentLanguage(String blobContentLanguage) {
        this.blobContentLanguage = blobContentLanguage;
        return this;
    }

    public String getBlobContentMD5() {
        return blobContentMD5;
    }

    public CommitBlobBlocksOptions setBlobContentMD5(String blobContentMD5) {
        this.blobContentMD5 = blobContentMD5;
        return this;
    }

    public String getBlobCacheControl() {
        return blobCacheControl;
    }

    public CommitBlobBlocksOptions setBlobCacheControl(String blobCacheControl) {
        this.blobCacheControl = blobCacheControl;
        return this;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public CommitBlobBlocksOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public CommitBlobBlocksOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public CommitBlobBlocksOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public CommitBlobBlocksOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
