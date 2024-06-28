// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/**
 * The event for ServerDataMessage.
 */
@Immutable
public final class ServerMessageEvent {

    private final WebPubSubDataFormat dataFormat;
    private final BinaryData data;
    private final Long sequenceId;

    /**
     * Creates a new instance of ServerMessageEvent.
     *
     * @param data the data.
     * @param dataFormat the data format.
     * @param sequenceId the sequenceId.
     */
    public ServerMessageEvent(BinaryData data, WebPubSubDataFormat dataFormat, Long sequenceId) {
        this.data = data;
        this.dataFormat = dataFormat;
        this.sequenceId = sequenceId;
    }

    /**
     * Gets the data.
     *
     * @return the data.
     */
    public BinaryData getData() {
        return data;
    }

    /**
     * Gets the data format.
     *
     * @return the data format.
     */
    public WebPubSubDataFormat getDataFormat() {
        return dataFormat;
    }

    /**
     * Gets the sequenceId.
     *
     * @return the sequenceId.
     */
    public Long getSequenceId() {
        return sequenceId;
    }
}
