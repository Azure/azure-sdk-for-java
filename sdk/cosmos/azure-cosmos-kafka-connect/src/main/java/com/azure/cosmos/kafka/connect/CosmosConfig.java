// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

/**
 * Configuration for Cosmos DB Kafka connector
 */
public class CosmosConfig extends AbstractConfig {

    /**
     * Initializes a new instance of the Cosmos DB Kafka Connector configuration
     * @param definition The configuration definition
     * @param originals The original config values
     * @param configProviderProps The configuration overrides for this provider
     * @param doLog Flag indicating whether the configuration should be logged
     */
    public CosmosConfig(ConfigDef definition, Map<?, ?> originals, Map<String, ?> configProviderProps, boolean doLog) {
        super(definition, originals, configProviderProps, doLog);
    }
}
