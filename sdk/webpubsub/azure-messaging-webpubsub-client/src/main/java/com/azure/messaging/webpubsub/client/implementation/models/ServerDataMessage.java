// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;

/**
 * The message of server.
 */
@Immutable
public final class ServerDataMessage {

    private final WebPubSubDataFormat dataType;
    private final BinaryData data;
    private final Long sequenceId;

    /**
     * Creates a new instance of ServerDataMessage.
     *
     * @param dataType the data type.
     * @param data the data.
     * @param sequenceId the sequenceId.
     */
    public ServerDataMessage(WebPubSubDataFormat dataType, BinaryData data, Long sequenceId) {
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
    public WebPubSubDataFormat getDataType() {
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
