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

    private final WebPubSubDataType dataType;
    private final BinaryData data;
    private final Long sequenceId;

    /**
     * Creates a new instance of ServerMessageEvent.
     *
     * @param data the data.
     * @param dataType the data type.
     * @param sequenceId the sequenceId.
     */
    public ServerMessageEvent(BinaryData data, WebPubSubDataType dataType, Long sequenceId) {
        this.data = data;
        this.dataType = dataType;
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
     * Gets the data type.
     *
     * @return the data type.
     */
    public WebPubSubDataType getDataType() {
        return dataType;
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
