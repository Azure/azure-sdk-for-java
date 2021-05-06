// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Class for static declaration.
 */
public class JavaStatic extends JavaScope {
    /**
     * Initializes a new instance of the {@link JavaStatic} class.
     *
     */
    public JavaStatic() {
        super("static");
        this.setSuppressNewLine(true);
    }

    /**
     * Add a line of text to the static body.
     *
     * @param text Text to add.
     * @return The {@link JavaStatic} object itself.
     */
    @Override
    public JavaStatic line(String text) {
        super.line(text);
        return this;
    }
}
