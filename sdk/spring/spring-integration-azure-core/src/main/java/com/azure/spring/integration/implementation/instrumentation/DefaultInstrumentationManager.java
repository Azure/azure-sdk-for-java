// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.implementation.instrumentation;

import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.instrumentation.InstrumentationManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 */
public class DefaultInstrumentationManager implements InstrumentationManager {

    private final Map<String, Instrumentation> healthInstrumentations = new ConcurrentHashMap<>();

    /**
     * Get all health instrumentation.
     *
     * @return healthInstrumentations the health instrumentations
     */
    public Set<Instrumentation> getAllHealthInstrumentation() {
        return healthInstrumentations.entrySet().stream().map(Map.Entry::getValue)
                                     .collect(Collectors.toSet());
    }

    @Override
    public void addHealthInstrumentation(Instrumentation instrumentation) {
        healthInstrumentations.put(instrumentation.getId(), instrumentation);
    }

    @Override
    public Instrumentation getHealthInstrumentation(String id) {
        return healthInstrumentations.get(id);
    }

}
