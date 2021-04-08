// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Generator for a java class.
 */
public class JavaClass extends JavaType {
    /**
     * Initializes a new instance of the {@link JavaClass} class.
     *
     * @param access       Access level of class.
     * @param novelty      Novelty of the class.
     * @param typeName     The name of the class being declared.
     * @param multiplicity Static or Instance.
     * @param extend       Interfaces extended by this class.
     * @param implement    Interfaces implemented by this class.
     */
    public JavaClass(Access access, Novelty novelty, String typeName, Multiplicity multiplicity, String extend, String implement) {
        super(access, novelty, typeName, multiplicity, extend, implement);
    }
}
