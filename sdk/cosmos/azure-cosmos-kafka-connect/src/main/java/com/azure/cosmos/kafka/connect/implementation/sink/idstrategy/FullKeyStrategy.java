// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idstrategy;

import java.util.HashMap;
import java.util.Map;

public class FullKeyStrategy extends TemplateStrategy {
    @Override
    public void configure(Map<String, ?> configs) {
        Map<String, Object> conf = new HashMap<>(configs);
        conf.put(TemplateStrategyConfig.TEMPLATE_CONFIG, "${key}");
        super.configure(conf);
    }
}
