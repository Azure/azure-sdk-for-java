/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ErrorSubCode.
 */
public enum ErrorSubCode {
    /** Enum value UnexpectedError. */
    UNEXPECTED_ERROR("UnexpectedError"),

    /** Enum value ResourceError. */
    RESOURCE_ERROR("ResourceError"),

    /** Enum value NotImplemented. */
    NOT_IMPLEMENTED("NotImplemented"),

    /** Enum value ParameterMissing. */
    PARAMETER_MISSING("ParameterMissing"),

    /** Enum value ParameterInvalidValue. */
    PARAMETER_INVALID_VALUE("ParameterInvalidValue"),

    /** Enum value HttpNotAllowed. */
    HTTP_NOT_ALLOWED("HttpNotAllowed"),

    /** Enum value Blocked. */
    BLOCKED("Blocked"),

    /** Enum value AuthorizationMissing. */
    AUTHORIZATION_MISSING("AuthorizationMissing"),

    /** Enum value AuthorizationRedundancy. */
    AUTHORIZATION_REDUNDANCY("AuthorizationRedundancy"),

    /** Enum value AuthorizationDisabled. */
    AUTHORIZATION_DISABLED("AuthorizationDisabled"),

    /** Enum value AuthorizationExpired. */
    AUTHORIZATION_EXPIRED("AuthorizationExpired");

    /** The actual serialized value for a ErrorSubCode instance. */
    private String value;

    ErrorSubCode(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ErrorSubCode instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ErrorSubCode object, or null if unable to parse.
     */
    @JsonCreator
    public static ErrorSubCode fromString(String value) {
        ErrorSubCode[] items = ErrorSubCode.values();
        for (ErrorSubCode item : items) {
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
