// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Class for writing lines of generated code to a file.
 */
public class CodeWriter {
    private static final String INDENTATION = "    ";
    private final OutputStreamWriter fileWriter;
    private final IndentedFileWriter indentedFileWriter;

    private boolean nextLineNeedsBlank;
    private boolean lastLineWasText;
    private boolean lastLineWasCloseScope;

    /**
     * Initializes a new instance of {@link CodeWriter}.
     *
     * @param filePath Full path of file to be generated.
     * @throws IOException IOException
     */
    public CodeWriter(String filePath) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(filePath);
        this.fileWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
        this.indentedFileWriter = new IndentedFileWriter(fileWriter, INDENTATION, false);
    }

    /**
     * Initializes a new instance of {@link CodeWriter}.
     *
     * @param filePath Full path of file to be generated.
     * @param isDebug  Boolean indicating whether or not debug mode is on or not.
     * @throws IOException IOException
     */
    public CodeWriter(String filePath, boolean isDebug) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(filePath);
        this.fileWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
        this.indentedFileWriter = new IndentedFileWriter(fileWriter, INDENTATION, isDebug);
    }

    /**
     * Closes the file and flushes it.
     *
     * @throws IOException IOException
     */
    public void close() throws IOException {
        this.indentedFileWriter.close();
        this.fileWriter.flush();
        this.fileWriter.close();
    }

    /**
     * Indicates that a blank line is appropriate between lines of code.
     */
    public void addNewLine() {
        if (lastLineWasText) {
            nextLineNeedsBlank = true;
            lastLineWasText = false;
        }
    }

    /**
     * Write an open brace and increase indent level.
     *
     * @throws IOException IOException
     */
    public void openScope() throws IOException {
        indentedFileWriter.writeLineWithNoIndent("{");
        this.increaseIndent();
        nextLineNeedsBlank = false;
        lastLineWasText = false;
    }

    /**
     * Decrease indent level and Write a close brace.
     *
     * @throws IOException IOException
     */
    public void closeScope() throws IOException {
        indentedFileWriter.decreaseIndent();
        if (nextLineNeedsBlank) {
            indentedFileWriter.writeWithNoIndent("\n");
            indentedFileWriter.writeWithIndent("}");
        } else {
            indentedFileWriter.writeWithIndent("}");
            lastLineWasCloseScope = true;
        }

        nextLineNeedsBlank = true;
        lastLineWasText = false;

    }

    /**
     * Increases indent level.
     */
    public void increaseIndent() {
        this.indentedFileWriter.increaseIndent();
    }

    /**
     * Decreases indent level.
     */
    public void decreaseIndent() {
        this.indentedFileWriter.decreaseIndent();
    }

    /**
     * Writes a line of code.
     *
     * @param text Code text to write.
     * @throws IOException IOException.
     */
    public void writeLine(String text) throws IOException {
        writeLine(text, false, false, false);
    }

    /**
     * Writes a line of code.
     *
     * @param text              Code text to write.
     * @param suppressLineBreak True if the text should not be followed by a new line.
     * @throws IOException IOException
     */
    public void writeLine(String text, boolean suppressLineBreak) throws IOException {
        writeLine(text, suppressLineBreak, false, false);
    }

    /**
     * Writes a line of code
     *
     * @param text              Code text to write.
     * @param suppressLineBreak True if the text should not be followed by a new line.
     * @param suppressBlank     True if there should be no blank line preceding the text.
     * @param outDent           True if the line should be out-dented one level.
     */
    public void writeLine(String text, boolean suppressLineBreak, boolean suppressBlank, boolean outDent) throws IOException {
        WriteMode writeMode = WriteMode.WRITE_LINE_WITH_INDENT;

        if (suppressLineBreak) {
            text = text + " ";
            writeMode = WriteMode.WRITE_WITH_INDENT;
        }

        if (suppressBlank) {
            if (nextLineNeedsBlank) {
                text = " " + text;
            }
            nextLineNeedsBlank = false;
        }

        if (outDent) {
            if (writeMode == WriteMode.WRITE_LINE_WITH_INDENT) {
                writeMode = WriteMode.WRITE_LINE_WITH_NO_INDENT;
            } else {
                writeMode = WriteMode.WRITE_WITH_NO_INDENT;
            }
        }

        // If next line needs to be blank, write a line with no indent.
        if (nextLineNeedsBlank) {
            if (lastLineWasCloseScope) {
                this.indentedFileWriter.writeLineWithNoIndent("");
            }

            this.indentedFileWriter.writeLineWithNoIndent("");
            nextLineNeedsBlank = false;
        }

        writeToFile(text, writeMode);
        lastLineWasText = true;
        lastLineWasCloseScope = false;
    }

    private void writeToFile(String text, WriteMode writeMode) throws IOException {
        switch (writeMode) {
            case WRITE_LINE_WITH_NO_INDENT:
                this.indentedFileWriter.writeLineWithNoIndent(text);
                break;
            case WRITE_WITH_INDENT:
                this.indentedFileWriter.writeWithIndent(text);
                break;
            case WRITE_WITH_NO_INDENT:
                this.indentedFileWriter.writeWithNoIndent(text);
                break;
            default:
                this.indentedFileWriter.writeLineWithIndent(text);
                break;
        }
    }

    private enum WriteMode {
        WRITE_LINE_WITH_NO_INDENT,
        WRITE_LINE_WITH_INDENT,
        WRITE_WITH_NO_INDENT,
        WRITE_WITH_INDENT,
    }
}
