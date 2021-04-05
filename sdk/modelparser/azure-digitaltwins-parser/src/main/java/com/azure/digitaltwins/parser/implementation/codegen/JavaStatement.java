// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;

/**
 * Generator for Java statement.
 */
public abstract class JavaStatement {

    /**
     * @return Gets text for sorting against other {@link JavaStatement} objects.
     */
    public abstract String getSortingText();

    /**
     * Generate code for the statement.
     *
     * @param codeWriter A {@link CodeWriter} object for generating the statement code.
     */
    public abstract void generateCode(CodeWriter codeWriter) throws IOException;
}
