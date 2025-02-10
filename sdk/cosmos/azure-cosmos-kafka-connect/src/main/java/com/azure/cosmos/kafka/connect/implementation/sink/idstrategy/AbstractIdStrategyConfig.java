// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idstrategy;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class AbstractIdStrategyConfig extends AbstractConfig {
    public static final String PREFIX = "azure.cosmos.sink.id.strategy" + ".";

    public AbstractIdStrategyConfig(ConfigDef definition, Map<?, ?> originals) {
        super(definition, originals);
    }
}
