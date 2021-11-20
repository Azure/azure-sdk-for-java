// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.instrumentation;

import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 *
 */
public class InstrumentationSendCallback implements ListenableFutureCallback<Void> {

    private final InstrumentationManager instrumentationManager;

    private final String instrumentationId;

    public InstrumentationSendCallback(String instrumentationId, InstrumentationManager instrumentationManager) {
        this.instrumentationId = instrumentationId;
        this.instrumentationManager = instrumentationManager;
    }

    @Override
    public void onFailure(Throwable ex) {
        this.instrumentationManager.getHealthInstrumentation(instrumentationId).markDown(ex);
    }

    @Override
    public void onSuccess(Void result) {
        this.instrumentationManager.getHealthInstrumentation(instrumentationId).markUp();
    }
}
