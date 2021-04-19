// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.codegen.JavaLibrary;

/**
 * Interface for classes that generate code for java types.
 */
public interface TypeGenerator {
    /**
     * Generate code for the type.
     *
     * @param parserLibrary A {@link JavaLibrary} object to which to add the generated code.
     */
    void generateCode(JavaLibrary parserLibrary);
}
