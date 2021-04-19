// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.digitaltwins.parser.implementation.codegen;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavaDeclaration {
    private static final ClientLogger LOGGER = new ClientLogger(JavaDeclaration.class);

    private final Access access;
    private final Novelty novelty;
    private final Multiplicity multiplicity;
    private final Mutability mutability;

    private final String type;
    private final String name;
    protected boolean inheritDoc;
    protected final List<String> summaryLines;
    protected final List<String> remarksLines;
    protected final List<String> attributes;

    /**
     * Initializes a new instance of the {@link JavaDeclaration} class.
     *
     * @param access       Access level of declaration.
     * @param novelty      Novelty of the declaration.
     * @param type         Type of declaration.
     * @param name         Name of the declaration.
     * @param multiplicity Static or Instance.
     * @param mutability   Mutability of the declaration.
     */
    public JavaDeclaration(
        Access access,
        Novelty novelty,
        String type,
        String name,
        Multiplicity multiplicity,
        Mutability mutability) {

        if (multiplicity == null) {
            multiplicity = Multiplicity.INSTANCE;
        }
        if (mutability == null) {
            mutability = Mutability.MUTABLE;
        }

        this.access = access;
        this.novelty = novelty;
        this.type = type;
        this.name = name;
        this.multiplicity = multiplicity;
        this.mutability = mutability;
        this.inheritDoc = false;
        this.summaryLines = new ArrayList<>();
        this.remarksLines = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    /**
     * @return Get declaration name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return Get declaration type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * If set to true, inherit documentation from comments of the base class.
     *
     * @param value Whether or not documentation will be inherited.
     */
    public void setInheritDoc(boolean value) {
        this.inheritDoc = value;
    }

    /**
     * @return True if documentation is inherited from the base class, False otherwise.
     */
    public boolean getInheritDoc() {
        return this.inheritDoc;
    }

    /**
     * Gets the value of the name decorated with appropriate keywords.
     *
     * @param typeParams Compiled list of all typed parameters for declaration.
     * @return The value of the name decorated with appropriate keywords.
     */
    public String getDecoratedName(String typeParams) {
        StringBuilder decoratedName = new StringBuilder();

        switch (access) {
            case IMPLICIT:
            case PACKAGE_PRIVATE:
                break;
            case PUBLIC:
                decoratedName.append("public ");
                break;
            case PROTECTED:
                decoratedName.append("protected ");
                break;
            case PRIVATE:
                decoratedName.append("private ");
                break;
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException("Unexpected value: " + access));
        }

        if (typeParams != null && !typeParams.isEmpty()) {
            decoratedName.append(typeParams).append(" ");
        }

        if (multiplicity == Multiplicity.STATIC) {
            decoratedName.append("static ");
        }

        if (this.novelty == Novelty.ABSTRACT) {
            decoratedName.append("abstract ");
        }

        if (this.mutability == Mutability.FINAL) {
            decoratedName.append("final ");
        }

        if (this.type != null) {
            decoratedName.append(this.type).append(" ");
        }

        decoratedName.append(this.name);

        return decoratedName.toString();
    }

    /**
     * Add a line of summary text describing the declaration.
     *
     * @param text Text for the summary.
     * @return The {@link JavaDeclaration} object itself.
     */
    public JavaDeclaration addSummary(String text) {
        if (!text.endsWith(".")) {
            throw LOGGER.logExceptionAsError(new StyleException("Summary text of declaration '" + this.name + "' must end with a period -- SA1629."));
        }

        this.summaryLines.add(text);
        return this;
    }

    /**
     * Add a line of remarks text for the declaration.
     *
     * @param text Text for the remarks.
     * @return The {@link JavaDeclaration} object itself.
     */
    public JavaDeclaration addRemarks(String text) {
        if (!text.endsWith(".")) {
            throw LOGGER.logExceptionAsError(new StyleException("Remarks text of declaration '" + this.name + "' must end with a period -- SA1629."));
        }

        this.remarksLines.add(text);
        return this;
    }

    /**
     * Add an attribute to the declaration.
     *
     * @param text Attribute text.
     */
    public void addAttributes(String text) {
        this.attributes.add(text);
    }

    /**
     * Generate code for the declaration.
     *
     * @param codeWriter A {@link CodeWriter} object for generating the declaration code.
     */
    public void generateCode(CodeWriter codeWriter) throws IOException {
    }

    /**
     * Write the summary comments for the declaration.
     *
     * @param codeWriter A {@link CodeWriter} object for writing the declaration comments.
     * @throws IOException IOException
     */
    protected void writeSummaryAndRemarks(CodeWriter codeWriter) throws IOException {
        codeWriter.addNewLine();
        if (this.inheritDoc) {
            codeWriter.writeLine("/** {@inheritDoc} */");
        } else if (!summaryLines.isEmpty()) {
            codeWriter.writeLine("/**");

            for (String summaryLine : summaryLines) {
                codeWriter.writeLine(" * " + summaryLine);
            }

            if (!remarksLines.isEmpty()) {
                codeWriter.writeLine(" * <p>");
                for (String remarksLine : remarksLines) {
                    codeWriter.writeLine(" * " + remarksLine);
                }
                codeWriter.writeLine(" * </p>");
            }

            codeWriter.writeLine(" */");
        }
    }

    protected void writeAttributes(CodeWriter codeWriter) throws IOException {
        for (String attribute : this.attributes) {
            codeWriter.writeLine("@" + attribute);
        }
    }
}
