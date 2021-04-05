// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generator for a series of java code lines whose code-generation order is determined by a lexicoraphical sort rather than the order in which they are added.
 * A {@link JavaSorted} object is appropriate for blocks of code for which :
 * (a) Lines are added in arbitrary order, perhaps by multiple sources.
 * (b) The order of the lines is not important for correctness.
 * (c) A consistent order aids readability and eases diffing across generated versions.
 */
public class JavaSorted extends JavaStatement {
    private List<JavaStatement> javaStatements;

    public JavaSorted() {
        this.javaStatements = new ArrayList<>();
    }

    /**
     * Add a line of text to the multi-line statement.
     *
     * @param text Text to add.
     * @return the {@link JavaSorted} object itself.
     */
    public JavaSorted line(String text) {
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
     * {@inheritDoc}
     */
    @Override
    public String getSortingText() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(CodeWriter codeWriter) throws IOException {
        javaStatements.sort(Comparator.comparing(JavaStatement::getSortingText));

        for (JavaStatement javaStatement : javaStatements) {
            javaStatement.generateCode(codeWriter);
        }
    }
}
