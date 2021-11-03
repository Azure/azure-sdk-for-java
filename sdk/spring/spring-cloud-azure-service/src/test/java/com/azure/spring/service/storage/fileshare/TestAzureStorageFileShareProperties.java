// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.fileshare;

import com.azure.spring.service.storage.TestAzureStorageProperties;
import com.azure.storage.file.share.ShareServiceVersion;

/**
 * Properties for Azure Storage File Share service.
 */
public class TestAzureStorageFileShareProperties extends TestAzureStorageProperties implements StorageFileShareProperties {

    private ShareServiceVersion serviceVersion;

    private String shareName;
    private String fileName;

    @Override
    public ShareServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(ShareServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public String getShareName() {
        return shareName;
    }

    public void setShareName(String shareName) {
        this.shareName = shareName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
