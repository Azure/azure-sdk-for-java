// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Fluent;

/**
 * The options when send to group.
 */
@Fluent
public final class SendToGroupOptions {

    private Long ackId;
    private boolean noEcho = false;
    private boolean fireAndForget = false;

    /**
     * Creates a new instance of SendToGroupOptions.
     */
    public SendToGroupOptions() {
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
    public SendToGroupOptions setAckId(Long ackId) {
        this.ackId = ackId;
        return this;
    }

    /**
     * Gets the noEcho option.
     *
     * @return the noEcho option.
     */
    public boolean isNoEcho() {
        return noEcho;
    }

    /**
     * Sets the noEcho option. When true, the client does not receive this message from the group.
     *
     * @param noEcho the noEcho option.
     * @return itself.
     */
    public SendToGroupOptions setNoEcho(boolean noEcho) {
        this.noEcho = noEcho;
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
    public SendToGroupOptions setFireAndForget(boolean fireAndForget) {
        this.fireAndForget = fireAndForget;
        return this;
    }
}
