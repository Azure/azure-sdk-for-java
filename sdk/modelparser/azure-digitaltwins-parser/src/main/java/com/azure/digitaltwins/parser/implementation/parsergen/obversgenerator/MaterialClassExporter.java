// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;

import java.util.List;

public final class MaterialClassExporter {
    private static final ClientLogger LOGGER = new ClientLogger(MaterialClassExporter.class);

    private MaterialClassExporter() { }

    /**
     * Generate appropriate members for the material class.
     *
     * @param obverseClass A {@link JavaClass} object to which to add the members.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param classIsBase True if the material class is the DTDL base class.
     * @param classIsAbstract True if the material class is abstract.
     * @param classIsOvert True if the material class is overt (type is usable in a model).
     * @param classIsAugmentable True if the material class is augmentable.
     * @param properties A list of {@link MaterialProperty} objects for the properties of the material class.
     */
    public static void addMembers(JavaClass obverseClass, String typeName, boolean classIsBase, boolean classIsAbstract, boolean classIsOvert, boolean classIsAugmentable, List<MaterialProperty> properties) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
