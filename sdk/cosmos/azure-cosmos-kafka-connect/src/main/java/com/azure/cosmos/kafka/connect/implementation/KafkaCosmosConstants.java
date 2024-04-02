// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.core.util.CoreUtils;

public class KafkaCosmosConstants {
    public static final String PROPERTIES_FILE_NAME = "azure-cosmos-kafka-connect.properties";
    public static final String CURRENT_VERSION = CoreUtils.getProperties(PROPERTIES_FILE_NAME).get("version");
    public static final String CURRENT_NAME = CoreUtils.getProperties(PROPERTIES_FILE_NAME).get("name");
    public static final String USER_AGENT_SUFFIX = String.format("KafkaConnect/%s/%s", CURRENT_NAME, CURRENT_VERSION);
}
