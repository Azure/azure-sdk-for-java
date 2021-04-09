// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Generator for a java for loop statement.
 */
public class JavaFor extends JavaScope {

    /**
     * Initializes a new instance of the {@link JavaFor} class.
     *
     * @param forText Text for the parenthesized portion of the for statement.
     */
    public JavaFor(String forText) {
        super("for (" + forText + ")");
        this.setSuppressNewLine(true);
    }


    /**
     * Add a line of text to the for body.
     *
     * @param text Text to add.
     * @return The {@link JavaFor} object itself.
     */
    @Override
    public JavaFor line(String text) {
        super.line(text);
        return this;
    }
}
