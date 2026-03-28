// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/**
 * The result of invoking an event.
 */
@Immutable
public final class InvokeEventResult {

    private final String invocationId;
    private final WebPubSubDataFormat dataFormat;
    private final BinaryData data;

    /**
     * Creates a new instance of InvokeEventResult.
     *
     * @param invocationId the invocation ID correlated with the response.
     * @param dataFormat the response payload data type.
     * @param data the response payload.
     */
    public InvokeEventResult(String invocationId, WebPubSubDataFormat dataFormat, BinaryData data) {
        this.invocationId = invocationId;
        this.dataFormat = dataFormat;
        this.data = data;
    }

    /**
     * Gets the invocation ID correlated with the response.
     *
     * @return the invocation ID.
     */
    public String getInvocationId() {
        return invocationId;
    }

    /**
     * Gets the response payload data type.
     *
     * @return the data format.
     */
    public WebPubSubDataFormat getDataFormat() {
        return dataFormat;
    }

    /**
     * Gets the response payload.
     *
     * @return the data.
     */
    public BinaryData getData() {
        return data;
    }
}
