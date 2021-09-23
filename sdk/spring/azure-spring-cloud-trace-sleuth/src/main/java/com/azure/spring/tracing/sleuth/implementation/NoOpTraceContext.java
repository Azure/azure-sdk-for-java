// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth.implementation;

import org.springframework.cloud.sleuth.TraceContext;

/**
 * A noop implementation. Does nothing.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
class NoOpTraceContext implements TraceContext {

    @Override
    public String traceId() {
        return "";
    }

    @Override
    public String parentId() {
        return "";
    }

    @Override
    public String spanId() {
        return "";
    }

    @Override
    public Boolean sampled() {
        return false;
    }

}
