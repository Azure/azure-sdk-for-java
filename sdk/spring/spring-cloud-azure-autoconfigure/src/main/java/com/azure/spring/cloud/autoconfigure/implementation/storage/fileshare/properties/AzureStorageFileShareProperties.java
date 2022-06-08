// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.fileshare.properties;

import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import com.azure.spring.cloud.service.implementation.storage.fileshare.ShareServiceClientProperties;
import com.azure.storage.file.share.ShareServiceVersion;

/**
 * Properties for Azure Storage File Share service.
 */
public class AzureStorageFileShareProperties extends AzureStorageProperties implements ShareServiceClientProperties {

    public static final String PREFIX = "spring.cloud.azure.storage.fileshare";
    public static final String FILE_ENDPOINT_PATTERN = "https://%s.file%s";

    /**
     * Share service version used when making API requests
     */
    private ShareServiceVersion serviceVersion;
    /**
     * Name of the share.
     */
    private String shareName;
    /**
     * Path to the file. For instance, 'directory1/file1'.
     */
    private String filePath;
    /**
     * Path to the directory. For instance, 'directory1/directory2'.
     */
    private String directoryPath;

    @Override
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }
}
