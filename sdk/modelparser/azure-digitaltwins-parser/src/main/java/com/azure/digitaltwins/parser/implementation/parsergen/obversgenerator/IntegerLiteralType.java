// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;

/**
 * Code-generation elements for declaring and parsing an integer literal type.
 */
public class IntegerLiteralType implements LiteralType {
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
        return isOptional ? ParserGeneratorStringValues.OBVERSE_TYPE_OBJECT_INTEGER : ParserGeneratorStringValues.OBVERSE_TYPE_INTEGER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitialValue(boolean isOptional) {
        return isOptional ? null : "0";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParseExpression(String stringVar) {
        return ParserGeneratorStringValues.OBVERSE_TYPE_OBJECT_INTEGER.concat(".parseInt(").concat(stringVar).concat(")");
    }
}
