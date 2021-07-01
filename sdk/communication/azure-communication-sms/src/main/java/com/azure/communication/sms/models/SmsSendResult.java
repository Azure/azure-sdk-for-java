// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms.models;

import com.azure.core.annotation.Immutable;

/** The SmsSendResult model. */
@Immutable
public final class SmsSendResult {
    private final String to;
    private final String messageId;
    private final int httpStatusCode;
    private final boolean isSuccessful;
    private final String errorMessage;

    /**
     * Constructor to wrap the smsSendResponseItem
     * @param to The recipient's phone number in E.164 format.
     * @param messageId The identifier of the outgoing Sms message. Only present if message processed.
     * @param httpStatusCode The httpStatusCode property: HTTP Status code.
     * @param isSuccessful The successful property: Indicates if the message is processed successfully or not.
     * @param errorMessage Optional error message in case of 4xx/5xx/repeatable errors.
     */
    public SmsSendResult(String to, String messageId, int httpStatusCode, boolean isSuccessful, String errorMessage) {
        this.to = to;
        this.messageId = messageId;
        this.httpStatusCode = httpStatusCode;
        this.isSuccessful = isSuccessful;
        this.errorMessage = errorMessage;
    }

    /**
     * Get the to property: The recipient's phone number in E.164 format.
     *
     * @return the to value.
     */
    public String getTo() {
        return this.to;
    }

    /**
     * Get the messageId property: The identifier of the outgoing Sms message. Only present if message processed.
     *
     * @return the messageId value.
     */
    public String getMessageId() {
        return this.messageId;
    }

    /**
     * Get the httpStatusCode property: HTTP Status code.
     *
     * @return the httpStatusCode value.
     */
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    /**
     * Get the successful property: Indicates if the message is processed successfully or not.
     *
     * @return the successful value.
     */
    public boolean isSuccessful() {
        return this.isSuccessful;
    }

    /**
     * Get the errorMessage property: Optional error message in case of 4xx/5xx/repeatable errors.
     *
     * @return the errorMessage value.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
