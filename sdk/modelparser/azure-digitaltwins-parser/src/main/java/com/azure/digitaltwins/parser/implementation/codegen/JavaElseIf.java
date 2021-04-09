// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Generator for a java else if statement.
 */
public class JavaElseIf extends JavaScope {

    private final StatementAdder parent;

    /**
     * Initializes a new instance of the {@link JavaElseIf} class.
     *
     * @param elseIfText Text for the parenthesized portion of the else if statement.
     * @param parent     The scope that is the parent of this {@link JavaElseIf} object.
     */
    public JavaElseIf(String elseIfText, StatementAdder parent) {
        super("else if (" + elseIfText + ")");
        this.parent = parent;
        this.setSuppressNewLine(true);
        this.setOutDent(true);
    }

    /**
     * Add another java else if statement following this else if.
     *
     * @param elseIfText Text for the parenthesized portion of the else if statement.
     * @return The {@link JavaElseIf} object added.
     */
    public JavaElseIf elseIf(String elseIfText) {
        JavaElseIf javaElseIf = new JavaElseIf(elseIfText, this.parent);
        addStatement(javaElseIf);
        return javaElseIf;
    }

    /**
     * Add a java else statement following this if.
     *
     * @return The {@link JavaElse} object itself.
     */
    public JavaElse addElse() {
        JavaElse javaElse = new JavaElse();
        this.parent.addStatement(javaElse);
        return javaElse;
    }

    /**
     * Add a line of text to the if body.
     *
     * @param text Text to add.
     * @return The {@link JavaElseIf} object itself.
     */
    @Override
    public JavaElseIf line(String text) {
        super.line(text);
        return this;
    }
}
