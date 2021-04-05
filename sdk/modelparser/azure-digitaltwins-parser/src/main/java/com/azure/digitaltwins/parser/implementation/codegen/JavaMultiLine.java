// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generator for a multiple-line of Java code statement.
 */
public class JavaMultiLine extends JavaStatement {

    private String headText;
    private List<JavaStatement> javaStatements;

    public JavaMultiLine(String headText) {
        this.headText = headText;
        this.javaStatements = new ArrayList<>();
    }

    /**
     * Add a line of text to the multi-line statement.
     *
     * @param text Text to add.
     * @return The {@link JavaMultiLine} object itself.
     */
    public JavaMultiLine line(String text) {
        this.javaStatements.add(new JavaLine(text));
        return this;
    }

    /**
     * Add a nested multiple-line statement within the multi-line statement.
     *
     * @param subHeadText Text for the first line of the nested statement.
     * @return The {@link JavaMultiLine} object added.
     */
    public JavaMultiLine multiLine(String subHeadText) {
        JavaMultiLine javaMultiLine = new JavaMultiLine(subHeadText);
        this.javaStatements.add(javaMultiLine);
        return javaMultiLine;
    }

    /**
     * Add to the multi-line statement a sequence of statements that will be lexicographically sorted at code-generation time.
     *
     * @return The {@link JavaSorted} object added.
     */
    public JavaSorted sorted() {
        JavaSorted javaSorted = new JavaSorted();
        javaStatements.add(javaSorted);
        return javaSorted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSortingText() {
        return headText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(CodeWriter codeWriter) throws IOException {
        codeWriter.writeLine(headText);
        codeWriter.increaseIndent();

        for (JavaStatement javaStatement : this.javaStatements) {
            javaStatement.generateCode(codeWriter);
        }

        codeWriter.decreaseIndent();
    }
}
