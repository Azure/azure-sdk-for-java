// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;

/**
 * Code-generation elements for declaring and parsing a boolean literal type.
 */
public class BooleanLiteralType implements LiteralType {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeNull(boolean isOptional) {
        return isOptional;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSingularType(boolean isOptional) {
        return isOptional ? ParserGeneratorStringValues.OBVERSE_TYPE_OBJECT_BOOLEAN : ParserGeneratorStringValues.OBVERSE_TYPE_BOOLEAN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitialValue(boolean isOptional) {
        return isOptional ? null : ParserGeneratorStringValues.OBVERSE_VALUE_FALSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParseExpression(String stringVar) {
        return ParserGeneratorStringValues.OBVERSE_TYPE_OBJECT_BOOLEAN.concat(".parseBoolean(").concat(stringVar).concat(")");
    }
}
