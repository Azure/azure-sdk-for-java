/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.spellcheck;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for ErrorType.
 */
public final class ErrorType extends ExpandableStringEnum<ErrorType> {
    /** Static value UnknownToken for ErrorType. */
    public static final ErrorType UNKNOWN_TOKEN = fromString("UnknownToken");

    /** Static value RepeatedToken for ErrorType. */
    public static final ErrorType REPEATED_TOKEN = fromString("RepeatedToken");

    /**
     * Creates or finds a ErrorType from its string representation.
     * @param name a name to look for
     * @return the corresponding ErrorType
     */
    @JsonCreator
    public static ErrorType fromString(String name) {
        return fromString(name, ErrorType.class);
    }

    /**
     * @return known ErrorType values
     */
    public static Collection<ErrorType> values() {
        return values(ErrorType.class);
    }
}
