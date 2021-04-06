// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Generator for a java catch statement.
 */
public class JavaCatch extends JavaScope {

    private final StatementAdder parent;

    /**
     * Initializes a new instance of the {@link JavaCatch} class.
     *
     * @param catchText Text for the parenthesized portion of the catch statement.
     * @param parent    The scope that is the parent of this {@link JavaCatch} object.
     */
    public JavaCatch(String catchText, StatementAdder parent) {
        super("catch (" + catchText + ")");
        this.parent = parent;
        this.setSuppressBreak(true);
    }

    /**
     * Add another java catch statement following this catch.
     *
     * @param catchText Text for the parenthesized portion of the catch statement
     * @return The {@link JavaCatch} object added.
     */
    public JavaCatch addCatch(String catchText) {
        JavaCatch javaCatch = new JavaCatch(catchText, this.parent);
        this.parent.addStatement(javaCatch);
        return javaCatch;
    }

    /**
     * Add a java finally statement following this catch.
     *
     * @return The {@link JavaFinally} object added.
     */
    public JavaFinally addFinally() {
        JavaFinally javaFinally = new JavaFinally();
        this.parent.addStatement(javaFinally);
        return javaFinally;
    }

    /**
     * Add a line of text to the catch body.
     *
     * @param text Text to add.
     * @return The {@link JavaCatch} object itself.
     */
    @Override
    public JavaCatch line(String text) {
        super.line(text);
        return this;
    }
}
