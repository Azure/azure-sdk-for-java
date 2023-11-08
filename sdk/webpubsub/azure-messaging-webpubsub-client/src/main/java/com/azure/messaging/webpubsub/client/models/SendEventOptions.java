// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Fluent;

/**
 * The options when send event.
 */
@Fluent
public final class SendEventOptions {

    private Long ackId;
    private boolean fireAndForget = false;

    /**
     * Creates a new instance of SendEventOptions.
     */
    public SendEventOptions() {
    }

    /**
     * Gets the ackId option.
     *
     * @return the ackId option.
     */
    public Long getAckId() {
        return ackId;
    }

    /**
     * Sets the ackId option.
     *
     * @param ackId the ackId option. Client will provide auto increment ID, if set to {@code null}.
     * @return itself.
     */
    public SendEventOptions setAckId(Long ackId) {
        this.ackId = ackId;
        return this;
    }

    /**
     * Gets the fireAndForget option.
     *
     * @return the fireAndForget option.
     */
    public boolean isFireAndForget() {
        return fireAndForget;
    }

    /**
     * Sets the fireAndForget option. When true, client does not wait for AckMessage.
     *
     * @param fireAndForget the fireAndForget option.
     * @return itself.
     */
    public SendEventOptions setFireAndForget(boolean fireAndForget) {
        this.fireAndForget = fireAndForget;
        return this;
    }
}
