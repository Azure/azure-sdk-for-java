// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Generator for a java try statement.
 */
public class JavaTry extends JavaScope {

    private final StatementAdder parent;

    /**
     * Initializes a new instance of the {@link JavaTry} class.
     *
     * @param parent The scope that is the parent of this {@link JavaTry} object.
     */
    public JavaTry(StatementAdder parent) {
        super("try");
        this.parent = parent;
        this.setSuppressNewLine(true);
    }

    /**
     * Add a java catch statement following this try.
     *
     * @param catchText Text for the parenthesized portion of the catch statement.
     * @return The {@link JavaCatch} object added.
     */
    public JavaCatch addCatch(String catchText) {
        JavaCatch javaCatch = new JavaCatch(catchText, this.parent);
        this.parent.addStatement(javaCatch);
        return javaCatch;
    }

    /**
     * Add a java finally statement following this try.
     *
     * @return The {@link JavaFinally} object added.
     */
    public JavaFinally addFinally() {
        JavaFinally javaFinally = new JavaFinally();
        this.parent.addStatement(javaFinally);
        return javaFinally;
    }

    /**
     * Add a line of text to the try body.
     *
     * @param text Text to add.
     * @return The {@link JavaTry} object itself.
     */
    @Override
    public JavaTry line(String text) {
        super.line(text);
        return this;
    }
}
