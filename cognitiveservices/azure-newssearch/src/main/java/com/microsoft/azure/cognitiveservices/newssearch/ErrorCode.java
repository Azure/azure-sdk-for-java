/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for ErrorCode.
 */
public final class ErrorCode extends ExpandableStringEnum<ErrorCode> {
    /** Static value None for ErrorCode. */
    public static final ErrorCode NONE = fromString("None");

    /** Static value ServerError for ErrorCode. */
    public static final ErrorCode SERVER_ERROR = fromString("ServerError");

    /** Static value InvalidRequest for ErrorCode. */
    public static final ErrorCode INVALID_REQUEST = fromString("InvalidRequest");

    /** Static value RateLimitExceeded for ErrorCode. */
    public static final ErrorCode RATE_LIMIT_EXCEEDED = fromString("RateLimitExceeded");

    /** Static value InvalidAuthorization for ErrorCode. */
    public static final ErrorCode INVALID_AUTHORIZATION = fromString("InvalidAuthorization");

    /** Static value InsufficientAuthorization for ErrorCode. */
    public static final ErrorCode INSUFFICIENT_AUTHORIZATION = fromString("InsufficientAuthorization");

    /**
     * Creates or finds a ErrorCode from its string representation.
     * @param name a name to look for
     * @return the corresponding ErrorCode
     */
    @JsonCreator
    public static ErrorCode fromString(String name) {
        return fromString(name, ErrorCode.class);
    }

    /**
     * @return known ErrorCode values
     */
    public static Collection<ErrorCode> values() {
        return values(ErrorCode.class);
    }
}
