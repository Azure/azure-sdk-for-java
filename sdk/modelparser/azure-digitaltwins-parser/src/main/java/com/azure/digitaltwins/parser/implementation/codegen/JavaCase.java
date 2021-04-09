// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;

public class JavaCase extends JavaStatement {
    private final String value;

    public JavaCase(String value) {
        this.value = value;
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
        codeWriter.writeLine("case " + this.value + ":", false, false, false);
    }
}
