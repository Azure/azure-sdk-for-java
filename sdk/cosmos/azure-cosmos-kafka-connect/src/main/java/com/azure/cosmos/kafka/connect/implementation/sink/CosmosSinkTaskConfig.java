// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import java.util.Map;

// Currently the sink task config shares the same config as sink connector config
public class CosmosSinkTaskConfig extends CosmosSinkConfig {
    public CosmosSinkTaskConfig(Map<String, ?> parsedConfig) {
        super(parsedConfig);
    }
}
