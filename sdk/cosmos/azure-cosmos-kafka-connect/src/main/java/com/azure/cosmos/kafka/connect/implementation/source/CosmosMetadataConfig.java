// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class CosmosMetadataConfig {
    private final int metadataPollDelayInMs;
    private final CosmosMetadataStorageType storageType;
    private final String storageName;

    public CosmosMetadataConfig(
        int metadataPollDelayInMs,
        CosmosMetadataStorageType metadataStorageType,
        String storageName) {

        checkArgument(metadataPollDelayInMs > 0, "Argument 'metadataPollDelayInMs' should be larger than 0");
        checkArgument(StringUtils.isNotEmpty(storageName), "Argument 'storageName' should not be null");

        this.metadataPollDelayInMs = metadataPollDelayInMs;
        this.storageType = metadataStorageType;
        this.storageName = storageName;
    }

    public int getMetadataPollDelayInMs() {
        return metadataPollDelayInMs;
    }

    public String getStorageName() {
        return storageName;
    }

    public CosmosMetadataStorageType getStorageType() {
        return storageType;
    }
}
