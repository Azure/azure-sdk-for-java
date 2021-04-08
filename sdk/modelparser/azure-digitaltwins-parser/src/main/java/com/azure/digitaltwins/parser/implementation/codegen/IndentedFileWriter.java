// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;
import java.io.OutputStreamWriter;

class IndentedFileWriter {
    private final OutputStreamWriter fileWriter;
    private int indent;
    private final String indentation;
    private boolean isDebug;
    private StringBuilder stringBuilder;

    IndentedFileWriter(OutputStreamWriter fileWriter, String indentation, boolean isDebug) {
        this.indent = 0;
        this.indentation = indentation;
        this.fileWriter = fileWriter;
        this.isDebug = isDebug;
        if (isDebug) {
            stringBuilder = new StringBuilder();
        }
    }

    void writeLineWithIndent(String input) throws IOException {
        if (this.isDebug) {
            stringBuilder.append(calculateIndentation()).append(input).append("\r\n");
        }

        fileWriter.append(calculateIndentation()).append(input).append("\r\n");
    }

    void writeWithIndent(String input) throws IOException {
        if (this.isDebug) {
            stringBuilder.append(calculateIndentation()).append(input);
        }
        fileWriter.append(calculateIndentation()).append(input);
    }

    void writeWithNoIndent(String input) throws IOException {
        if (this.isDebug) {
            this.stringBuilder.append(input);
        }
        fileWriter.append(input);
    }

    void writeLineWithNoIndent(String input) throws IOException {
        if (this.isDebug) {
            this.stringBuilder.append(input);
        }
        fileWriter.append(input).append("\r\n");
    }

    void increaseIndent() {
        indent++;
    }

    void decreaseIndent() {
        if (indent > 0) {
            indent--;
        }
    }

    private String calculateIndentation() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            output.append(indentation);
        }
        return output.toString();
    }

    public void close() throws IOException {
        if (this.isDebug) {
            stringBuilder.append("\r\n");
        }
        fileWriter.append("\r\n");
    }
}
