// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.implementation.instrumentation;

import com.azure.spring.integration.core.instrumentation.Instrumentation;
import com.azure.spring.integration.core.instrumentation.InstrumentationManager;
import java.util.function.BiConsumer;

/**
 *
 */
public class InstrumentationSendCallback implements BiConsumer<Void, Throwable> {

    private final InstrumentationManager instrumentationManager;

    private final String instrumentationId;

    /**
     * Construct a {@link InstrumentationSendCallback} with the specified instrumentation id and {@link InstrumentationManager}.
     *
     * @param instrumentationId the instrumentation id
     * @param instrumentationManager the instrumentation manager
     */
    public InstrumentationSendCallback(String instrumentationId, InstrumentationManager instrumentationManager) {
        this.instrumentationId = instrumentationId;
        this.instrumentationManager = instrumentationManager;
    }

    @Override
    public void accept(Void result, Throwable ex) {
        if (ex != null) {
            instrumentationManager.getHealthInstrumentation(instrumentationId)
                .setStatus(Instrumentation.Status.DOWN, ex);
        } else {
            instrumentationManager.getHealthInstrumentation(instrumentationId)
                .setStatus(Instrumentation.Status.UP);
        }
    }
}
