// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idStrategy;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class AbstractIdStrategyConfig extends AbstractConfig {
    public static final String ID = "id";
    public static final String ID_STRATEGY = ID + ".strategy";
    public static final String PREFIX = ID_STRATEGY + ".";

    public AbstractIdStrategyConfig(ConfigDef definition, Map<?, ?> originals) {
        super(definition, originals);
    }
}
