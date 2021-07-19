// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.health;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InstrumentationManager {

    private final Map<String, Object> runtime = new ConcurrentHashMap<>();

    private final Map<String, Instrumentation> healthInstrumentations = new HashMap<>();

    public Set<Instrumentation> getHealthInstrumentations() {
        return healthInstrumentations.entrySet().stream().map(Map.Entry::getValue)
                                     .collect(Collectors.toSet());
    }

    public void addHealthInstrumentation(Instrumentation instrumentation) {
        healthInstrumentations.put(instrumentation.getName(), instrumentation);
    }

    public Instrumentation getHealthInstrumentation(String key) {
        return healthInstrumentations.get(key);
    }

    public Map<String, Object> getRuntime() {
        return runtime;
    }

}
