// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source.configs;

public class CosmosMetadataConfig {
    private final int metadataPollDelayInMs;
    private final String metadataTopicName;

    public CosmosMetadataConfig(int metadataPollDelayInMs, String metadataTopicName) {
        this.metadataPollDelayInMs = metadataPollDelayInMs;
        this.metadataTopicName = metadataTopicName;
    }

    public int getMetadataPollDelayInMs() {
        return metadataPollDelayInMs;
    }

    public String getMetadataTopicName() {
        return metadataTopicName;
    }
}
