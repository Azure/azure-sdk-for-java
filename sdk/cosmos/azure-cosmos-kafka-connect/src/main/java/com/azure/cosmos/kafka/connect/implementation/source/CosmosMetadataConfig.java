// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class CosmosMetadataConfig {
    private final int metadataPollDelayInMs;
    private final String metadataTopicName;

    public CosmosMetadataConfig(int metadataPollDelayInMs, String metadataTopicName) {
        checkArgument(StringUtils.isNotEmpty(metadataTopicName), "Argument 'metadataTopicName' can not be null");
        checkArgument(metadataPollDelayInMs > 0, "Argument 'metadataPollDelayInMs' should be larger than 0");

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
