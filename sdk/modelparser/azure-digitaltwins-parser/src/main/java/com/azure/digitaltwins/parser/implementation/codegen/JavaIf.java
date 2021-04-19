// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 *
 */
public class JavaIf extends JavaScope {
    private final StatementAdder parent;

    /**
     * Initializes a new instance of the {@link JavaIf} class.
     *
     * @param ifText Text for the parenthesized portion of the if statement.
     * @param parent The scope that is the parent of this {@link JavaIf} object.
     */
    public JavaIf(String ifText, StatementAdder parent) {
        super("if (" + ifText + ")");
        this.parent = parent;
        this.setSuppressNewLine(true);
    }

    /**
     * Add a java else if statement following this if.
     *
     * @param elseIfText Text for the parenthesized portion of the else if statement.
     * @return The {@link JavaElseIf} object added.
     */
    public JavaElseIf jElseIf(String elseIfText) {
        JavaElseIf javaElseIf = new JavaElseIf(elseIfText, this.parent);
        this.parent.addStatement(javaElseIf);
        return javaElseIf;
    }

    /**
     * Add a java else statement following this if.
     *
     * @return The {@link JavaElse} object itself.
     */
    public JavaElse jElse() {
        JavaElse javaElse = new JavaElse();
        this.parent.addStatement(javaElse);
        return javaElse;
    }

    /**
     * Add a line of text to the if body.
     *
     * @param text Text to add.
     * @return The {@link JavaIf} object itself.
     */
    @Override
    public JavaIf line(String text) {
        super.line(text);
        return this;
    }
}
