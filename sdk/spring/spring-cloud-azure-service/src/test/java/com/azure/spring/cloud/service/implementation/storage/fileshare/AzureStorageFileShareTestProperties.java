// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.fileshare;

import com.azure.spring.cloud.service.implementation.storage.AzureStorageTestProperties;
import com.azure.storage.file.share.ShareServiceVersion;

/**
 * Properties for Azure Storage File Share service.
 */
class AzureStorageFileShareTestProperties extends AzureStorageTestProperties implements ShareServiceClientProperties {

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
