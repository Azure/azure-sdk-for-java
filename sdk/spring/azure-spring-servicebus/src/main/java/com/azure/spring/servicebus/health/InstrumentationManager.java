// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.health;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ServiceBus health details management class.
 */
public class InstrumentationManager {

    private final Map<String, Instrumentation> healthInstrumentations = new HashMap<>();

    public Set<Instrumentation> getHealthInstrumentations() {
        return healthInstrumentations.entrySet().stream().map(Map.Entry::getValue)
                                     .collect(Collectors.toSet());
    }

    public void addHealthInstrumentation(Instrumentation instrumentation) {
        healthInstrumentations.put(instrumentation.getName() + ":" + instrumentation.getType().getTypeName(),
            instrumentation);
    }

    public Instrumentation getHealthInstrumentation(Instrumentation instrumentation) {
        return healthInstrumentations.get(instrumentation.getName() + ":" + instrumentation.getType().getTypeName());
    }

}
