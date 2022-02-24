// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.instrumentation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        return new HashSet<>(healthInstrumentations.values());
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
