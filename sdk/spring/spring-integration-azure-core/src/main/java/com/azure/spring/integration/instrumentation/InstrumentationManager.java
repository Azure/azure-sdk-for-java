// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.instrumentation;

import java.util.Set;

/**
 *
 */
public interface InstrumentationManager {

    Set<Instrumentation> getAllHealthInstrumentation();

    void addHealthInstrumentation(String id, Instrumentation instrumentation);

    Instrumentation getHealthInstrumentation(String id);

}
