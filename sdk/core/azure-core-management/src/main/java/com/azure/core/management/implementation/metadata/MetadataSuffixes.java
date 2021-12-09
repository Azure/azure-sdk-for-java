// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.metadata;

public final class MetadataSuffixes {
    private String keyVaultDns;
    private String storage;
    private String sqlServerHostname;
    private String azureDataLakeStoreFileSystem;
    private String azureDataLakeAnalyticsCatalogAndJob;

    public String getKeyVaultDns() {
        return keyVaultDns;
    }

    public String getStorage() {
        return storage;
    }

    public String getSqlServerHostname() {
        return sqlServerHostname;
    }

    public String getAzureDataLakeStoreFileSystem() {
        return azureDataLakeStoreFileSystem;
    }

    public String getAzureDataLakeAnalyticsCatalogAndJob() {
        return azureDataLakeAnalyticsCatalogAndJob;
    }
}
