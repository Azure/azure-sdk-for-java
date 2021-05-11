// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

/**
 * Code-generation elements for declaring and parsing a specific literal type.
 */
public interface LiteralType {
    /**
     * Indicates whether it is possible for the type to be null.
     *
     * @param isOptional True if the property is optional.
     * @return True if the type can be null.
     */
    boolean canBeNull(boolean isOptional);

    /**
     * Gets the type declaration for a singular value.
     * @param isOptional True if the property is optional.
     * @return A string representation of the type.
     */
    String getSingularType(boolean isOptional);

    /**
     * Gets an appropriate initial value.
     * @param isOptional True if the property is optional.
     * @return A string representation of the value.
     */
    String getInitialValue(boolean isOptional);

    /**
     * Gets a parse expression for a singular value.
     * @param stringVar Name of a string variable that holds the value to parse.
     * @return A string representation of the parse expression.
     */
    String getParseExpression(String stringVar);
}
