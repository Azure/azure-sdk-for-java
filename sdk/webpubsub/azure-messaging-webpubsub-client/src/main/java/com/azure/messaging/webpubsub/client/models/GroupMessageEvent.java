// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/**
 * The event for GroupDataMessage.
 */
@Immutable
public final class GroupMessageEvent {

    private final String group;
    private final WebPubSubDataType dataType;
    private final BinaryData data;
    private final String fromUserId;
    private final Long sequenceId;

    /**
     * Creates a new instance of GroupMessageEvent.
     *
     * @param group the group name.
     * @param data the data.
     * @param dataType the data type.
     * @param fromUserId the userId of sender.
     * @param sequenceId the sequenceId.
     */
    public GroupMessageEvent(String group, BinaryData data, WebPubSubDataType dataType, String fromUserId,
                             Long sequenceId) {
        this.data = data;
        this.dataType = dataType;
        this.fromUserId = fromUserId;
        this.group = group;
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
     * Gets the userId of sender.
     *
     * @return the userId of sender.
     */
    public String getFromUserId() {
        return fromUserId;
    }

    /**
     * Gets the group name.
     *
     * @return the group name.
     */
    public String getGroup() {
        return group;
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
