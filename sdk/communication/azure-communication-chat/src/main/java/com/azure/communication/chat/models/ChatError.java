// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;
import java.util.List;

/** The Chat Services error. */
@Fluent
public final class ChatError {

    private String code;

    private String message;

    private String target;

    private List<ChatError> details;

    private ChatError innerError;

    /**
     * Get the code property: The error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Set the code property: The error code.
     *
     * @param code the code value to set.
     * @return the ChatError object itself.
     */
    public ChatError setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message property: The error message.
     *
     * @param message the message value to set.
     * @return the ChatError object itself.
     */
    public ChatError setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get the target property: The error target.
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the details property: Further details about specific errors that led to this error.
     *
     * @return the details value.
     */
    public List<ChatError> getDetails() {
        return this.details;
    }

    /**
     * Get the innerError property: The inner error if any.
     *
     * @return the innerError value.
     */
    public ChatError getInnerError() {
        return this.innerError;
    }

    /**
     * Set the target property
     *
     * @param target the code value to set.
     * @return the ChatError object itself.
     */
    public ChatError setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Set the details property
     *
     * @param details the code value to set.
     * @return the ChatError object itself.
     */
    public ChatError setDetails(List<ChatError> details) {
        this.details = details;
        return this;
    }

    /**
     * Set the innerError property
     *
     * @param innerError the code value to set.
     * @return the ChatError object itself.
     */
    public ChatError setInnerError(ChatError innerError) {
        this.innerError = innerError;
        return this;
    }
}
