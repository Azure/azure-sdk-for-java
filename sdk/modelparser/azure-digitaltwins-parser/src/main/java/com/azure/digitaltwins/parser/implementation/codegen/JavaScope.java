// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generator for a java code statement with an initial line of code followed by brace-enclosed lines.
 */
public class JavaScope extends JavaStatement implements StatementAdder {

    private final String headText;
    private final List<JavaStatement> javaStatementList;

    /**
     * Boolean value indicating whether the blank line preceding the scope should be suppressed.
     */
    private boolean suppressBlank;

    /**
     * Boolean value indicating whether or not the next text should be written with any indents.
     */
    private boolean outDent;

    /**
     * Boolean value indicating whether or not the nex text should end the line with a line break.
     */
    private boolean suppressNewLine;

    /**
     * @return Boolean value indicating whether or not the next text should end the line with a line break.
     */
    protected boolean getSuppressNewLine() {
        return this.suppressNewLine;
    }

    /**
     * Sets a boolean value indicating whether or not the next text should end the line with a line break.
     *
     * @param value True if the next text should not end the line with a line break.
     */
    protected void setSuppressNewLine(boolean value) {
        this.suppressNewLine = value;
    }

    /**
     * @return Boolean value indicating whether or not the next text should be written with any indents.
     */
    protected boolean getOutDent() {
        return this.outDent;
    }

    /**
     * Sets a boolean value indicating whether or not the next text should be written with any indents.
     *
     * @param value True if the next text should not be written with any indent.
     */
    protected void setOutDent(boolean value) {
        this.outDent = value;
    }

    /**
     * @return Boolean value indicating whether the blank line preceding the scope should be suppressed.
     */
    protected boolean getSuppressBlank() {
        return this.suppressBlank;
    }

    /**
     * Sets boolean value indicating whether the blank line preceding the scope should be suppressed.
     *
     * @param value boolean value indicating whether the blank line preceding the scope should be suppressed.
     */
    protected void setSuppressBlank(boolean value) {
        this.suppressBlank = value;
    }

    /**
     * Boolean value indicating whether the scope should be doubly indented instead of singly indented.
     */
    private boolean doubleIndent;

    /**
     * @return Boolean value indicating whether the scope should be doubly indented instead of singly indented.
     */
    protected boolean getDoubleIndent() {
        return this.doubleIndent;
    }

    /**
     * Sets boolean value indicating whether the scope should be doubly indented instead of singly indented.
     *
     * @param value Boolean value indicating whether the scope should be doubly indented instead of singly indented.
     */
    protected void setDoubleIndent(boolean value) {
        this.doubleIndent = value;
    }

    /**
     * Initializes a new instance of the {@link JavaScope} class.
     *
     * @param headText Text for the first line of the statement.
     */
    public JavaScope(String headText) {
        this.headText = headText;
        javaStatementList = new ArrayList<>();

        this.suppressBlank = false;
        this.doubleIndent = false;
    }

    /**
     * Add a nested scope to the scope.
     *
     * @param subHeadText Text for the first line of the nested scope.
     * @return The {@link JavaScope} object added.
     */
    public JavaScope scope(String subHeadText) {
        JavaScope javaScope = new JavaScope(subHeadText);
        this.javaStatementList.add(javaScope);
        return javaScope;
    }

    /**
     * Add a line of text to the scope.
     *
     * @param text Text to add.
     * @return The {@link JavaScope} object itself.
     */
    public JavaScope line(String text) {
        javaStatementList.add(new JavaLine(text));
        return this;
    }

    /**
     * Add a multiple-line statement to the scope.
     *
     * @param subHeadText Text for the first line of the statement.
     * @return The {@link JavaMultiLine} object added.
     */
    public JavaMultiLine multiLine(String subHeadText) {
        JavaMultiLine javaMultiLine = new JavaMultiLine(subHeadText);
        this.javaStatementList.add(javaMultiLine);
        return javaMultiLine;
    }

    /**
     * Add to the scope a sequence of statements that will be lexicographically sorted at code-generation time.
     *
     * @return The {@link JavaSorted} object added.
     */
    public JavaSorted sorted() {
        JavaSorted javaSorted = new JavaSorted();
        javaStatementList.add(javaSorted);
        return javaSorted;
    }

    /**
     * Add a java while statement to the scope.
     *
     * @param whileText Text for the parenthesized portion of the while statement.
     * @return The {@link JavaWhile} object added.
     */
    public JavaWhile jWhile(String whileText) {
        JavaWhile javaWhile = new JavaWhile(whileText);
        javaStatementList.add(javaWhile);
        return javaWhile;
    }

    /**
     * Add a java for statement to the scope.
     *
     * @param foreText Text for the parenthesized portion of the for statement.
     * @return The {@link JavaFor} object added.
     */
    public JavaFor jFor(String foreText) {
        JavaFor javaFor = new JavaFor(foreText);
        this.javaStatementList.add(javaFor);
        return javaFor;
    }

    /**
     * Add a java if statement to the scope.
     *
     * @param ifText Text for the parenthesized portion of the if statement.
     * @return The {@link JavaIf} object added.
     */
    public JavaIf jIf(String ifText) {
        JavaIf javaIf = new JavaIf(ifText, this);
        this.javaStatementList.add(javaIf);
        return javaIf;
    }

    /**
     * Add a java switch statement to the scope.
     *
     * @param switchText Text for the parenthesized portion of the switch statement.
     * @return The {@link JavaSwitch} object added.
     */
    public JavaSwitch jSwitch(String switchText) {
        JavaSwitch javaSwitch = new JavaSwitch(switchText);
        this.javaStatementList.add(javaSwitch);
        return javaSwitch;
    }

    /**
     * Add a java try statement to the scope.
     *
     * @return The {@link JavaTry} object added.
     */
    public JavaTry jTry() {
        JavaTry javaTry = new JavaTry(this);
        this.javaStatementList.add(javaTry);
        return javaTry;
    }

    /**
     * Add a break between statements.
     *
     * @return The {@link JavaScope} object itself.
     */
    public JavaScope jBreak() {
        this.javaStatementList.add(new JavaBreak());
        return this;
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
        if (this.headText != null) {
            codeWriter.writeLine(headText, getSuppressNewLine(), getSuppressBlank(), getOutDent());
        }

        codeWriter.openScope();

        if (this.getDoubleIndent()) {
            codeWriter.increaseIndent();
        }

        for (JavaStatement javaStatement : this.javaStatementList) {
            javaStatement.generateCode(codeWriter);
        }

        if (this.getDoubleIndent()) {
            codeWriter.decreaseIndent();
        }
        codeWriter.closeScope();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addStatement(JavaStatement javaStatement) {
        this.javaStatementList.add(javaStatement);
    }
}
