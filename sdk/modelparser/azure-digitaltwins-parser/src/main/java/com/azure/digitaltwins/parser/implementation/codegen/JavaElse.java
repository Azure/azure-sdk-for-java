// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Generator for a java else statement.
 */
public class JavaElse extends JavaScope {

    /**
     * Initializes a new instance of the {@link JavaElse} class.
     */
    public JavaElse() {
        super("else");
        this.setSuppressBlank(true);
        this.setSuppressNewLine(true);
        this.setOutDent(true);
    }

    /**
     * Add a line of text to the else body.
     *
     * @param text Text to add.
     * @return The {@link JavaElse} object itself.
     */
    @Override
    public JavaElse line(String text) {
        super.line(text);
        return this;
    }
}
