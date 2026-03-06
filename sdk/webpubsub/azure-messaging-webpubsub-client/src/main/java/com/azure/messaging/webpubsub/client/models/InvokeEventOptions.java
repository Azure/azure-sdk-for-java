// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Fluent;

/**
 * The options when invoking an event.
 */
@Fluent
public final class InvokeEventOptions {

    private String invocationId;

    /**
     * Creates a new instance of InvokeEventOptions.
     */
    public InvokeEventOptions() {
    }

    /**
     * Gets the invocation ID. If not specified, the client generates one automatically.
     *
     * @return the invocation ID.
     */
    public String getInvocationId() {
        return invocationId;
    }

    /**
     * Sets the invocation ID. If not specified, the client generates one automatically.
     *
     * @param invocationId the invocation ID.
     * @return itself.
     */
    public InvokeEventOptions setInvocationId(String invocationId) {
        this.invocationId = invocationId;
        return this;
    }
}
