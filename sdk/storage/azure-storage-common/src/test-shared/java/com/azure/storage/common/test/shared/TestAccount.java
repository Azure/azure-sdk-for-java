// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.storage.common.StorageSharedKeyCredential;

public class TestAccount {
    private final String name;
    private final String key;
    private final String connectionString;
    private final String blobEndpoint;
    private final String blobEndpointSecondary;
    private final String dataLakeEndpoint;
    private final String queueEndpoint;
    private final String fileEndpoint;

    public TestAccount(String name, String key, String connectionString,
                       String blobEndpoint, String blobEndpointSecondary,
                       String dataLakeEndpoint, String queueEndpoint, String fileEndpoint) {
        this.name = name;
        this.key = key;
        this.connectionString = connectionString;
        this.blobEndpoint = blobEndpoint;
        this.blobEndpointSecondary = blobEndpointSecondary;
        this.dataLakeEndpoint = dataLakeEndpoint;
        this.queueEndpoint = queueEndpoint;
        this.fileEndpoint = fileEndpoint;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getBlobEndpoint() {
        return blobEndpoint;
    }

    public String getBlobEndpointSecondary() {
        return blobEndpointSecondary;
    }

    public String getQueueEndpoint() {
        return queueEndpoint;
    }

    public String getFileEndpoint() {
        return fileEndpoint;
    }

    public StorageSharedKeyCredential getCredential() {
        return new StorageSharedKeyCredential(name, key);
    }

    public String getDataLakeEndpoint() {
        return dataLakeEndpoint;
    }

    public String getConnectionString() {
        return connectionString;
    }
}
