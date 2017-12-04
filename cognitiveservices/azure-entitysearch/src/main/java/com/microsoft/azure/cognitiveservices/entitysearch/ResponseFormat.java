/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for ResponseFormat.
 */
public final class ResponseFormat extends ExpandableStringEnum<ResponseFormat> {
    /** Static value Json for ResponseFormat. */
    public static final ResponseFormat JSON = fromString("Json");

    /** Static value JsonLd for ResponseFormat. */
    public static final ResponseFormat JSON_LD = fromString("JsonLd");

    /**
     * Creates or finds a ResponseFormat from its string representation.
     * @param name a name to look for
     * @return the corresponding ResponseFormat
     */
    @JsonCreator
    public static ResponseFormat fromString(String name) {
        return fromString(name, ResponseFormat.class);
    }

    /**
     * @return known ResponseFormat values
     */
    public static Collection<ResponseFormat> values() {
        return values(ResponseFormat.class);
    }
}
