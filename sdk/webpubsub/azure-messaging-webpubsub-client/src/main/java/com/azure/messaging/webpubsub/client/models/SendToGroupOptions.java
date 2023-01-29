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
    private Boolean noEcho = false;
    private Boolean fireAndForget = false;

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
     * @param ackId the ackId option.
     * @return itself.
     */
    public SendToGroupOptions setAckId(long ackId) {
        this.ackId = ackId;
        return this;
    }

    /**
     * Gets the noEcho option.
     *
     * @return the noEcho option.
     */
    public Boolean getNoEcho() {
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
    public boolean getFireAndForget() {
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
