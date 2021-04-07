// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

public class JavaConstructor extends JavaMethod {

    /**
     * Initializes a new instance of the {@link JavaConstructor} class.
     *
     * @param access       Access level of method.
     * @param name         Name of the class.
     * @param multiplicity Static or Instance.
     */
    public JavaConstructor(Access access, String name, Multiplicity multiplicity) {
        super(true, access, Novelty.NORMAL, null, name, multiplicity, Mutability.MUTABLE);

        if (access != Access.IMPLICIT && access != Access.PRIVATE) {
            this.addSummary("Initializes a new instance of the {@link " + name + "} class.");
        }
    }
}
