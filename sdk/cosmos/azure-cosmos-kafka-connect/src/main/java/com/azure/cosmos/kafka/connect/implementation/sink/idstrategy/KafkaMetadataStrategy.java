// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idstrategy;

import java.util.HashMap;
import java.util.Map;

public class KafkaMetadataStrategy extends TemplateStrategy {
    private KafkaMetadataStrategyConfig config;

    @Override
    public void configure(Map<String, ?> configs) {
        config = new KafkaMetadataStrategyConfig(configs);
        Map<String, Object> conf = new HashMap<>(configs);
        conf.put(TemplateStrategyConfig.TEMPLATE_CONFIG,
            "${topic}" + config.delimiter()
                + "${partition}" + config.delimiter() + "${offset}");

        super.configure(conf);
    }
}
