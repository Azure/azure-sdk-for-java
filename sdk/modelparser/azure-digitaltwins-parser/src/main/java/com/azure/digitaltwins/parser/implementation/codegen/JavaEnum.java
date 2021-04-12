// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generator for java enum
 */
public class JavaEnum extends JavaDeclaration implements JavaFile {
    private static ClientLogger logger = new ClientLogger(JavaEnum.class);
    private List<EnumVal> enumValues;
    private boolean isSorted;
    private String typeName;

    /**
     * Initializes a new instance of the {@link JavaEnum} class.
     *
     * @param access   Access level of enum.
     * @param typeName The name of the enum being declared.
     * @param isSorted True if the enum values should be sorted by name.
     */
    public JavaEnum(Access access, String typeName, boolean isSorted) {
        super(access, Novelty.NORMAL, "enum", typeName, Multiplicity.INSTANCE, Mutability.MUTABLE);

        this.enumValues = new ArrayList<>();
        this.isSorted = isSorted;
        this.typeName = typeName;
    }

    /**
     * Add a java value to the enum.
     *
     * @param name        Name of value.
     * @param description Description of value.
     * @return The {@link JavaEnum} object itself.
     */
    public JavaEnum value(String name, String description) {
        if (description != null && !description.endsWith(".")) {
            throw logger.logExceptionAsError(new StyleException("Documentation text of enum value '" + name + "' must end with a period. -- SA1629."));
        }

        this.enumValues.add(
            new EnumVal()
                .name(name)
                .description(description));

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(CodeWriter codeWriter) throws IOException {
        this.writeSummaryAndRemarks(codeWriter);
        this.writeAttributes(codeWriter);

        codeWriter.writeLine(this.getDecoratedName(null), true);
        codeWriter.openScope();

        if (this.isSorted) {
            this.enumValues.sort(Comparator.comparing(o -> o.name));
        }

        for (EnumVal enumVal : this.enumValues) {
            codeWriter.addNewLine();
            codeWriter.writeLine("/**");
            codeWriter.writeLine(" * " + enumVal.getDescription());
            codeWriter.writeLine(" */");
            codeWriter.writeLine(enumVal.getName() + ",");
        }

        codeWriter.closeScope();
    }

    private static class EnumVal {
        private String name;
        private String description;

        public String getName() {
            return this.name;
        }

        public JavaEnum.EnumVal name(String value) {
            this.name = value;
            return this;
        }

        public String getDescription() {
            return this.description;
        }

        public JavaEnum.EnumVal description(String value) {
            this.description = value;
            return this;
        }
    }
}
