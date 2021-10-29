// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.fileshare;

import com.azure.spring.core.properties.retry.StorageRetryProperties;
import com.azure.spring.service.core.properties.TestAzureHttpProperties;
import com.azure.storage.file.share.ShareServiceVersion;

/**
 * Properties for Azure Storage File Share service.
 */
public class TestAzureStorageFileShareHttpProperties extends TestAzureHttpProperties implements StorageFileShareProperties {

    public static final String PREFIX = "spring.cloud.azure.storage.fileshare";
    public static final String FILE_ENDPOINT_PATTERN = "https://%s.file%s";

    private ShareServiceVersion serviceVersion;

    private String shareName;
    private String fileName;
    private String endpoint;
    private String accountKey;
    private String sasToken;
    private String connectionString;
    private String accountName;
    private final StorageRetryProperties retry = new StorageRetryProperties();

    @Override
    public StorageRetryProperties getRetry() {
        return retry;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public String getAccountKey() {
        return accountKey;
    }

    @Override
    public String getConnectionString() {
        return connectionString;
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

    @Override
    public void setSasToken(String sasToken) {
        this.sasToken = sasToken;
    }

    @Override
    public String getSasToken() {
        return sasToken;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
