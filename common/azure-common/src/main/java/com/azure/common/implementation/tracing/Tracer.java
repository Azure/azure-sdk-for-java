// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.common.implementation.tracing;

import com.azure.common.http.ContextData;

// actually I don't  understand the idea behind TracerBuilder, so you may just ignore it
public interface Tracer {

    ContextData start(String methodName, com.azure.common.http.ContextData context);
    void end(int responseCode, Throwable error, ContextData context);
    void setAttribute(String key, String value, ContextData context);
    // void setAttribute(String key, long value, ContextData context);
    // void setAttribute(String key, double value, ContextData context);
    // void setAttribute(String key, boolean value, ContextData context);
}
