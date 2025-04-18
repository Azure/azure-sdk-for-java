// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.implementation.instrumentation;

import com.azure.spring.integration.core.instrumentation.Instrumentation;
import com.azure.spring.integration.core.instrumentation.InstrumentationManager;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 *
 */
public class InstrumentationSendCallback implements ListenableFutureCallback<Void> {

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
    public void onFailure(Throwable ex) {
        this.instrumentationManager.getHealthInstrumentation(instrumentationId)
                                   .setStatus(Instrumentation.Status.DOWN, ex);
    }

    @Override
    public void onSuccess(Void result) {
        this.instrumentationManager.getHealthInstrumentation(instrumentationId).setStatus(Instrumentation.Status.UP);
    }
}
