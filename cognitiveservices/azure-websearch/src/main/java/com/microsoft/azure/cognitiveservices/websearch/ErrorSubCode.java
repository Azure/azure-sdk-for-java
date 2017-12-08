/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for ErrorSubCode.
 */
public final class ErrorSubCode extends ExpandableStringEnum<ErrorSubCode> {
    /** Static value UnexpectedError for ErrorSubCode. */
    public static final ErrorSubCode UNEXPECTED_ERROR = fromString("UnexpectedError");

    /** Static value ResourceError for ErrorSubCode. */
    public static final ErrorSubCode RESOURCE_ERROR = fromString("ResourceError");

    /** Static value NotImplemented for ErrorSubCode. */
    public static final ErrorSubCode NOT_IMPLEMENTED = fromString("NotImplemented");

    /** Static value ParameterMissing for ErrorSubCode. */
    public static final ErrorSubCode PARAMETER_MISSING = fromString("ParameterMissing");

    /** Static value ParameterInvalidValue for ErrorSubCode. */
    public static final ErrorSubCode PARAMETER_INVALID_VALUE = fromString("ParameterInvalidValue");

    /** Static value HttpNotAllowed for ErrorSubCode. */
    public static final ErrorSubCode HTTP_NOT_ALLOWED = fromString("HttpNotAllowed");

    /** Static value Blocked for ErrorSubCode. */
    public static final ErrorSubCode BLOCKED = fromString("Blocked");

    /** Static value AuthorizationMissing for ErrorSubCode. */
    public static final ErrorSubCode AUTHORIZATION_MISSING = fromString("AuthorizationMissing");

    /** Static value AuthorizationRedundancy for ErrorSubCode. */
    public static final ErrorSubCode AUTHORIZATION_REDUNDANCY = fromString("AuthorizationRedundancy");

    /** Static value AuthorizationDisabled for ErrorSubCode. */
    public static final ErrorSubCode AUTHORIZATION_DISABLED = fromString("AuthorizationDisabled");

    /** Static value AuthorizationExpired for ErrorSubCode. */
    public static final ErrorSubCode AUTHORIZATION_EXPIRED = fromString("AuthorizationExpired");

    /**
     * Creates or finds a ErrorSubCode from its string representation.
     * @param name a name to look for
     * @return the corresponding ErrorSubCode
     */
    @JsonCreator
    public static ErrorSubCode fromString(String name) {
        return fromString(name, ErrorSubCode.class);
    }

    /**
     * @return known ErrorSubCode values
     */
    public static Collection<ErrorSubCode> values() {
        return values(ErrorSubCode.class);
    }
}
