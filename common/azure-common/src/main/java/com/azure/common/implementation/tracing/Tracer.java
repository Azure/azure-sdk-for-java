// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.common.implementation.tracing;

import com.azure.common.http.ContextData;

// actually I don't  understand the idea behind TracerBuilder, so you may just ignore it
public interface Tracer {

    ContextData start(String methodName, com.azure.common.http.ContextData context);
    void end(int responseCode, Throwable error, ContextData context);
}
