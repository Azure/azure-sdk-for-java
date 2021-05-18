// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;

public final class MaterialClassAugmentor {
    private static final ClientLogger LOGGER = new ClientLogger(MaterialClassAugmentor.class);

    private MaterialClassAugmentor() { }

    public static void generateConstructorCode(JavaScope scope, boolean classIsAugmentable) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * Generate appropriate members for the material class.
     *
     * @param obverseClass A {@link JavaClass} object to which to add the members.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param classIsAugmentable True if the material class is augmentable.
     * @param classIsBase True if the material class is the DTDL base class.
     * @param anyObjectProperties True if the material class as any object properties.
     */
    public static void addMembers(JavaClass obverseClass, String typeName, boolean classIsAugmentable, boolean classIsBase, boolean anyObjectProperties) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
