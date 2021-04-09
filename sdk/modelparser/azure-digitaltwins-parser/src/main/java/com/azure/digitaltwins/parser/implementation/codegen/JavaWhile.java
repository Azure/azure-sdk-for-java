// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Generator for a java while statement.
 */
public class JavaWhile extends JavaScope {

    /**
     * Initializes a new instance of the {@link JavaWhile} class.
     *
     * @param whileText Text for the parenthesized portion of the while statement.
     */
    public JavaWhile(String whileText) {
        super("while (" + whileText + ")");
        this.setSuppressNewLine(true);
    }

    /**
     * Add a line of text to the while body.
     *
     * @param text Text to add.
     * @return The {@link JavaWhile} object itself.
     */
    @Override
    public JavaWhile line(String text) {
        super.line(text);
        return this;
    }
}
