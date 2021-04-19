// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Class that expresses restrictions on allowed strings in DTDL models.
 */
public class StringRestriction {

    private final Integer maxLength;
    private final String pattern;

    /**
     * @return Gets the maximum permissible length of a string, or null if no maximum.
     */
    public Integer getMaxLength() {
        return this.maxLength;
    }

    /**
     * @return Gets a regex that constrains the permissible values, or null if no pattern constraint.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Initializes a new instance of the {@link StringRestriction} class.
     *
     * @param restrictionObject A {@link JsonNode} from the meta-model digest containing restrictions on the string value.
     */
    public StringRestriction(JsonNode restrictionObject) {
        this.maxLength = JsonNodeHelper.getNullableIntegerValue(restrictionObject, DtdlStrings.MAX_LENGTH);
        this.pattern = JsonNodeHelper.getTextValue(restrictionObject, DtdlStrings.PATTERN);
    }
}
