// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Class for writing lines of generated code to a file.
 */
public class CodeWriter {
    private static final String INDENTATION = "    ";
    private final OutputStreamWriter fileWriter;
    private final IndentedFileWriter indentedFileWriter;

    private boolean nextTextNeedsBlank;
    private boolean lastLineWasText;

    /**
     * Initializes a new instance of {@link CodeWriter}.
     *
     * @param filePath Full path of file to be generated.
     * @throws IOException
     */
    public CodeWriter(String filePath) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(filePath);
        this.fileWriter = new OutputStreamWriter(fileStream, "UTF-8");
        this.indentedFileWriter = new IndentedFileWriter(fileWriter, INDENTATION);
    }

    /**
     * Closes the file and flushes it.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        this.fileWriter.flush();
        this.fileWriter.close();
    }

    /**
     * Indicates that a blank line is appropriate between lines of code.
     */
    public void blank() {
        if (lastLineWasText) {
            nextTextNeedsBlank = true;
            lastLineWasText = false;
        }
    }

    /**
     * Write an open brace and increase indent level.
     *
     * @throws IOException
     */
    public void openScope() throws IOException {
        indentedFileWriter.writeLineWithIndent("{");
        this.increaseIndent();
        nextTextNeedsBlank = false;
        lastLineWasText = false;
    }

    /**
     * Decrease indent level and Write a close brace.
     *
     * @throws IOException
     */
    public void closeScope() throws IOException {
        indentedFileWriter.decreaseIndent();
        indentedFileWriter.writeWithIndent("}");
        nextTextNeedsBlank = true;
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
     * Writes a line of code
     *
     * @param text Code text to write.
     */
    public void writeLine(String text) throws IOException {
        writeLine(text, false, false);
    }

    /**
     * Writes a line of code
     *
     * @param text          Code text to write.
     * @param suppressBlank True if there should be no blank line preceding the text.
     */
    public void writeLine(String text, boolean suppressBlank) throws IOException {
        writeLine(text, suppressBlank, false);
    }

    /**
     * Writes a line of code
     *
     * @param text          Code text to write.
     * @param suppressBlank True if there should be no blank line preceding the text.
     * @param outdent       True if the line should be out-dented one level.
     */
    public void writeLine(String text, boolean suppressBlank, boolean outdent) throws IOException {
        if (nextTextNeedsBlank) {
            if (!suppressBlank) {
                indentedFileWriter.writeLineWithIndent("");
            }

            nextTextNeedsBlank = false;
        }

        if (outdent) {
            decreaseIndent();
        }

        this.indentedFileWriter.writeLineWithIndent(text);

        if (outdent) {
            increaseIndent();
        }

        lastLineWasText = true;
    }
}
