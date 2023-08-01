// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.instrumentation;

import java.util.Set;

/**
 * Azure Instrumentation Manager for Event Hubs or Service Bus processor.
 */
public interface InstrumentationManager {

    /**
     * Get all the instrumentation.
     * @return the set instrumentation.
     */
    Set<Instrumentation> getAllHealthInstrumentation();

    /**
     * Add the health instrumentation.
     * @param instrumentation the instrumentation.
     */
    void addHealthInstrumentation(Instrumentation instrumentation);

    /**
     * Get the health instrumentation by id.
     * @param id the id of instrumentation.
     * @return the instrumentation value of this id.
     */
    Instrumentation getHealthInstrumentation(String id);

}
