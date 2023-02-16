// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email.models;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Status of an email message that was sent previously. */
@Immutable
public final class SendEmailResult {
    /*
     * System generated id of an email message sent.
     */
    @JsonProperty(value = "messageId", required = true)
    private String messageId;

    /**
     * Constructor for SendEmailResult
     * @param messageId the system generated id of an email message sent
     */
    public SendEmailResult(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Get the messageId property: System generated id of an email message sent.
     *
     * @return the messageId value.
     */
    public String getMessageId() {
        return this.messageId;
    }
}
