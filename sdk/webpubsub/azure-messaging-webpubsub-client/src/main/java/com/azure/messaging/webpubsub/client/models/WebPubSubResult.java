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
    private final boolean isDuplicated;

    /**
     * Creates a new instance of WebPubSubResult.
     *
     * @param ackId the ackId.
     * @param isDuplicated whether the message is duplicated.
     */
    public WebPubSubResult(Long ackId, boolean isDuplicated) {
        this.ackId = ackId;
        this.isDuplicated = isDuplicated;
    }

    /**
     * Gets the ackId.
     *
     * @return the ackId.
     */
    public Long getAckId() {
        return ackId;
    }

    /**
     * Gets whether the message is duplicated.
     *
     * @return whether the message is duplicated.
     */
    public boolean isDuplicated() {
        return isDuplicated;
    }
}
