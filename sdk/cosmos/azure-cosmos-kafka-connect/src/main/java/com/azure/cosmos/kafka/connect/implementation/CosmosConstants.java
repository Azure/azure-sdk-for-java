// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.core.util.CoreUtils;

public class CosmosConstants {
    public static final String propertiesFileName = "azure-cosmos-kafka-connect.properties";
    public static final String currentVersion = CoreUtils.getProperties(propertiesFileName).get("version");
    public static final String currentName = CoreUtils.getProperties(propertiesFileName).get("name");
    public static final String userAgentSuffix = String.format("KafkaConnect/%s/%s", currentName, currentVersion);
}
