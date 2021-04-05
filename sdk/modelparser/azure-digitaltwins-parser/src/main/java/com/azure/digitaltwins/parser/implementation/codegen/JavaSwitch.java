// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Generator for a java switch statement.
 */
public class JavaSwitch extends JavaScope {

    /**
     * Initializes a new instance of the {@link JavaSwitch} class.
     *
     * @param switchText Text for the parenthesized portion of the switch statement.
     */
    public JavaSwitch(String switchText) {
        super("switch (" + switchText + ")");
        this.setDoubleIndent(true);
    }

    /**
     * Add a case statement to the switch.
     *
     * @param value Case value.
     * @return The {@link JavaSwitch} itself.
     */
    public JavaSwitch addCase(String value) {
        JavaCase javaCase = new JavaCase(value);
        ((StatementAdder) this).addStatement(javaCase);
        return this;
    }

    /**
     * Add a default statement to the switch.
     *
     * @return The {@link JavaSwitch} object itself.
     */
    public JavaSwitch addDefault() {
        JavaDefault javaDefault = new JavaDefault();
        ((StatementAdder) this).addStatement(javaDefault);
        return this;
    }

    /**
     * Add a line of text to the switch body.
     *
     * @param text Text to add.
     * @return The {@link JavaSwitch} object itself.
     */
    @Override
    public JavaSwitch line(String text) {
        super.line(text);
        return this;
    }
}
