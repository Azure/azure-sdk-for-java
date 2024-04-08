// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class CosmosMetadataConfig {
    private final int metadataPollDelayInMs;
    private final CosmosMetadataStorageType storageType;
    private final String topicName;
    private final String containerName;

    public CosmosMetadataConfig(
        int metadataPollDelayInMs,
        CosmosMetadataStorageType metadataStorageType,
        String metadataTopicName,
        String metadataContainerName) {

        checkArgument(metadataPollDelayInMs > 0, "Argument 'metadataPollDelayInMs' should be larger than 0");

        if (metadataStorageType == CosmosMetadataStorageType.KAFKA) {
            checkArgument(StringUtils.isNotEmpty(metadataTopicName), "Argument 'metadataTopicName' should not be null");
        }

        if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
            checkArgument(StringUtils.isNotEmpty(metadataContainerName), "Argument 'metadataContainerName' should not be null");
        }

        this.metadataPollDelayInMs = metadataPollDelayInMs;
        this.storageType = metadataStorageType;
        this.topicName = metadataTopicName;
        this.containerName = metadataContainerName;
    }

    public int getMetadataPollDelayInMs() {
        return metadataPollDelayInMs;
    }

    public String getMetadataTopicName() {
        return topicName;
    }

    public CosmosMetadataStorageType getStorageType() {
        return storageType;
    }

    public String getContainerName() {
        return containerName;
    }
}
