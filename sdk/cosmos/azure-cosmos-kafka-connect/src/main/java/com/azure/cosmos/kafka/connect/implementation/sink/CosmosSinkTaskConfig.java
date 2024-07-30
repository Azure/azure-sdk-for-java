// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class CosmosSinkTaskConfig extends CosmosSinkConfig {
    public static final String SINK_TASK_ID = "azure.cosmos.sink.task.id";
    private final String taskId;

    public CosmosSinkTaskConfig(Map<String, ?> parsedConfigs) {
        super(getConfigDef(), parsedConfigs);
        this.taskId = this.getString(SINK_TASK_ID);
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
                ConfigDef.Importance.MEDIUM);
    }

    public String getTaskId() {
        return taskId;
    }
}
