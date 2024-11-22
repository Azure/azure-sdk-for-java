// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.mocking;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;

/**
 * Mock implementation of the Tracer interface.
 */
public class MockTracer implements Tracer {
    @Override
    public Context start(String s, Context context) {
        return null;
    }

    @Override
    public void end(String s, Throwable throwable, Context context) {

    }

    @Override
    public void setAttribute(String s, String s1, Context context) {

    }
}
