// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Interface for top-level declarations in java code files.
 */
public interface JavaFile {

    /**
     * @return Gets the type name of the declaration, used to generate the filename.
     */
    String getTypeName();

    /**
     * Generate code for the file.
     *
     * @param codeWriter A {@link CodeWriter} object for generating the file code.
     */
    void generateCode(CodeWriter codeWriter);
}
