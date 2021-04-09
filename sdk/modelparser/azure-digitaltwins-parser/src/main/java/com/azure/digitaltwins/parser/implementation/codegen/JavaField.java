// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;

/**
 * Generator for java field.
 */
public class JavaField extends JavaDeclaration {

    private final String value;

    /**
     * @param access       Access level of the field.
     * @param type         Type of the field.
     * @param name         Name of the field.
     * @param value        Optional value for field. If null, no default value will be used.
     * @param multiplicity Static vs Instance. If null, {@link Multiplicity#INSTANCE} will be used.
     * @param mutability   {@link Mutability} of the field. If null, {@link Mutability#MUTABLE} will be used.
     */
    public JavaField(Access access, String type, String name, String value, Multiplicity multiplicity, Mutability mutability) {
        super(access, Novelty.NORMAL, type, name, multiplicity, mutability);

        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(CodeWriter codeWriter) throws IOException {
        this.writeSummaryAndRemarks(codeWriter);
        this.writeAttributes(codeWriter);

        if (this.value != null) {
            codeWriter.writeLine(getDecoratedName(null) + " = " + this.value + ";");
            codeWriter.addNewLine();
        } else {
            codeWriter.writeLine(this.getDecoratedName(null) + ";");
            codeWriter.addNewLine();
        }
    }
}
