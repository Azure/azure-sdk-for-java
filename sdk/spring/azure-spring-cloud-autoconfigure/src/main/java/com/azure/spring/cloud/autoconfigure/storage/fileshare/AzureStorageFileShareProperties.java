// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.storage.common.AzureStorageProperties;
import com.azure.storage.file.share.ShareServiceVersion;

/**
 * Properties for Azure Storage File Share service.
 */
public class AzureStorageFileShareProperties extends AzureStorageProperties {

    public static final String PREFIX = "spring.cloud.azure.storage.fileshare";
    public static final String FILE_ENDPOINT_PATTERN = "https://%s.file%s";

    private ShareServiceVersion serviceVersion;

    private String shareName;
    private String fileName;

    public String getEndpoint() {
        return endpoint == null ? buildEndpointFromAccountName() : endpoint;
    }

    private String buildEndpointFromAccountName() {
        return String.format(FILE_ENDPOINT_PATTERN, accountName, profile.getEnvironment().getStorageEndpointSuffix());
    }

    public ShareServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(ShareServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getShareName() {
        return shareName;
    }

    public void setShareName(String shareName) {
        this.shareName = shareName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
