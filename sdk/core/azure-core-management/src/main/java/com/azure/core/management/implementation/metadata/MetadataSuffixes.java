// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class MetadataSuffixes {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String keyVaultDns;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String storage;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String sqlServerHostname;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String azureDataLakeStoreFileSystem;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
