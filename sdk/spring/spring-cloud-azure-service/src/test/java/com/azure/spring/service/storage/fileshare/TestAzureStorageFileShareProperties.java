// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.fileshare;

import com.azure.spring.service.storage.TestAzureStorageProperties;
import com.azure.storage.file.share.ShareServiceVersion;

/**
 * Properties for Azure Storage File Share service.
 */
class TestAzureStorageFileShareProperties extends TestAzureStorageProperties implements ShareServiceClientProperties {

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

}
