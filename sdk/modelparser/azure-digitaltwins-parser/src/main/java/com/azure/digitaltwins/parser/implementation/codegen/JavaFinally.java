// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Generator for java finally statement.
 */
public class JavaFinally extends JavaScope {

    /**
     * Initializes a new instance of the {@link JavaFinally} class.
     */
    public JavaFinally() {
        super("finally");
        this.setSuppressBlank(true);
    }

    /**
     * Add a line of text to the finally body.
     *
     * @param text Text to add.
     * @return The {@link JavaFinally} object itself.
     */
    @Override
    public JavaFinally line(String text) {
        super.line(text);
        return this;
    }
}
