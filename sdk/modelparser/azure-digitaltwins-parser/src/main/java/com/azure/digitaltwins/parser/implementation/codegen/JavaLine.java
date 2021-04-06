// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;

public class JavaLine extends JavaStatement {

    private final String text;

    /**
     * Initializes a new instance of the {@link JavaLine} class.
     *
     * @param text Code Text.
     */
    public JavaLine(String text) {
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSortingText() {
        return this.text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(CodeWriter codeWriter) throws IOException {
        codeWriter.writeLine(this.text);
    }
}
