// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosUtils;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class CosmosSinkTaskConfig extends CosmosSinkConfig {
    public static final long LOG_INTERVAL_MS = 60 * 1000;

    public static final String SINK_TASK_ID = "azure.cosmos.sink.task.id";
    public static final String COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT = "azure.cosmos.client.metadata.caches.snapshot";
    public static final String THROUGHPUT_CONTROL_COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT =
        "azure.cosmos.throughputControl.client.metadata.caches.snapshot";
    private final String taskId;
    private final CosmosClientMetadataCachesSnapshot clientMetadataCachesSnapshot;
    private final CosmosClientMetadataCachesSnapshot throughputControlClientMetadataCachesSnapshot;

    public CosmosSinkTaskConfig(Map<String, ?> parsedConfigs) {
        super(getConfigDef(), parsedConfigs);
        this.taskId = this.getString(SINK_TASK_ID);
        this.clientMetadataCachesSnapshot =
            KafkaCosmosUtils.getCosmosClientMetadataFromString(
                this.getString(COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT));
        this.throughputControlClientMetadataCachesSnapshot =
            KafkaCosmosUtils.getCosmosClientMetadataFromString(
                this.getString(THROUGHPUT_CONTROL_COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT));
    }

    public static ConfigDef getConfigDef() {
        ConfigDef configDef = CosmosSinkConfig.getConfigDef();
        defineTaskIdConfig(configDef);

        return configDef;
    }

    private static void defineTaskIdConfig(ConfigDef result) {
        result
            .defineInternal(
                SINK_TASK_ID,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.MEDIUM)
            .defineInternal(
                COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT,
                ConfigDef.Type.STRING,
                null,
                ConfigDef.Importance.LOW
            )
            .defineInternal(
                THROUGHPUT_CONTROL_COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT,
                ConfigDef.Type.STRING,
                null,
                ConfigDef.Importance.LOW
            );
    }

    public String getTaskId() {
        return taskId;
    }

    public CosmosClientMetadataCachesSnapshot getClientMetadataCachesSnapshot() {
        return clientMetadataCachesSnapshot;
    }

    public CosmosClientMetadataCachesSnapshot getThroughputControlClientMetadataCachesSnapshot() {
        return throughputControlClientMetadataCachesSnapshot;
    }
}
