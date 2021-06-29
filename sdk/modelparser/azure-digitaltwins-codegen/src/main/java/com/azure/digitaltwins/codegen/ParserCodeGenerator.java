// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.codegen;

import com.azure.digitaltwins.codegen.implementation.parsergen.CodeGeneratorTask;

public class ParserCodeGenerator {
    public static void main(String[] args) throws Exception {
        CodeGeneratorTask task = new CodeGeneratorTask(args[0], args[1]);
        task.run();
    }
}
