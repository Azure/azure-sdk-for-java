// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;

/**
 * Code-generation elements for declaring and parsing a string literal type.
 */
public class StringLiteralType implements LiteralType {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeNull(boolean isOptional) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSingularType(boolean isOptional) {
        return ParserGeneratorStringValues.OBVERSE_TYPE_STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitialValue(boolean isOptional) {
        return isOptional ? null : "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParseExpression(String stringVar) {
        return stringVar;
    }
}
