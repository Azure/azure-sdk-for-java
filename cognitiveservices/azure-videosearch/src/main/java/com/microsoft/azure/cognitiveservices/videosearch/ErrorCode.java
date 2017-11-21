/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ErrorCode.
 */
public enum ErrorCode {
    /** Enum value None. */
    NONE("None"),

    /** Enum value ServerError. */
    SERVER_ERROR("ServerError"),

    /** Enum value InvalidRequest. */
    INVALID_REQUEST("InvalidRequest"),

    /** Enum value RateLimitExceeded. */
    RATE_LIMIT_EXCEEDED("RateLimitExceeded"),

    /** Enum value InvalidAuthorization. */
    INVALID_AUTHORIZATION("InvalidAuthorization"),

    /** Enum value InsufficientAuthorization. */
    INSUFFICIENT_AUTHORIZATION("InsufficientAuthorization");

    /** The actual serialized value for a ErrorCode instance. */
    private String value;

    ErrorCode(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ErrorCode instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ErrorCode object, or null if unable to parse.
     */
    @JsonCreator
    public static ErrorCode fromString(String value) {
        ErrorCode[] items = ErrorCode.values();
        for (ErrorCode item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
