// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

/**
 * The result of sending message.
 */
@Immutable
public final class WebPubSubResult {

    private final Long ackId;

    /**
     * Creates a new instance of WebPubSubResult.
     *
     * @param ackId the ackId.
     */
    public WebPubSubResult(Long ackId) {
        this.ackId = ackId;
    }

    /**
     * Gets the ackId.
     *
     * @return the ackId.
     */
    public Long getAckId() {
        return ackId;
    }
}
