// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;

/**
 * Represents a restriction on the properties of a class that is materialized in the parser object model.
 */
public interface PropertyRestriction {
    /**
     * Add code to the CheckRestrictions method in the material class that has this restriction.
     *
     * @param checkRestrictionMethodBody A {@link JavaScope} object to which to add the code.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param materialProperty The {@link MaterialProperty} that has the restriction.
     */
    void addRestriction(JavaScope checkRestrictionMethodBody, String typeName, MaterialProperty materialProperty);
}
