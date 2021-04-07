// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;
import java.io.OutputStreamWriter;

class IndentedFileWriter {
    private final OutputStreamWriter fileWriter;
    private int indent;
    private final String indentation;

    IndentedFileWriter(OutputStreamWriter fileWriter, String indentation) {
        this.indent = 0;
        this.indentation = indentation;
        this.fileWriter = fileWriter;
    }

    void writeLineWithIndent(String input) throws IOException {
        fileWriter.append(calculateIndentation()).append(input).append("\r\n");
    }

    void writeWithIndent(String input) throws IOException {
        fileWriter.append(calculateIndentation()).append(input);
    }

    void writeWithNoIndent(String input) throws IOException {
        fileWriter.append(input);
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
        fileWriter.append("\r\n");
    }
}
