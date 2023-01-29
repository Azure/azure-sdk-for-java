// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

@Immutable
public class ServerDataMessage extends WebPubSubMessage {

    private final WebPubSubDataType dataType;
    private final BinaryData data;
    private final Long sequenceId;

    public ServerDataMessage(WebPubSubDataType dataType, BinaryData data, Long sequenceId) {
        this.data = data;
        this.dataType = dataType;
        this.sequenceId = sequenceId;
    }

    public BinaryData getData() {
        return data;
    }

    public WebPubSubDataType getDataType() {
        return dataType;
    }

    public Long getSequenceId() {
        return sequenceId;
    }
}
