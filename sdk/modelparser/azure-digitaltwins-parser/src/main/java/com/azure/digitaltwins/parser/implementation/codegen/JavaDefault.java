// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;

/**
 * Generator for java default statement.
 */
public class JavaDefault extends JavaStatement {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getSortingText() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(CodeWriter codeWriter) throws IOException {
        codeWriter.writeLine("default:", false, true);
    }
}
