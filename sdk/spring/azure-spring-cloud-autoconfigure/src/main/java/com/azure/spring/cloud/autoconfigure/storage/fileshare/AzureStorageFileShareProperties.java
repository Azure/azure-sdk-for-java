// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.storage.common.AzureStorageProperties;
import com.azure.storage.file.share.ShareServiceVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for Azure Storage File Share service.
 */
@ConfigurationProperties(prefix = "spring.cloud.azure.storage.fileshare")
public class AzureStorageFileShareProperties extends AzureStorageProperties {


    private String endpoint;
    private ShareServiceVersion serviceVersion;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public ShareServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(ShareServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
