// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;

import java.util.List;

/**
 * Class for adding parsing code to material classes.
 */
public final class MaterialClassEqualizer {
    /**
     * This is a static class so it has a private constructor.
     */
    private MaterialClassEqualizer() { }

    /**
     * Generate appropriate members for the material class.
     *
     * @param obverseClass A {@link JavaClass} object to which to add the members.
     * @param className The java name of the material class.
     * @param baseClassName The java name of the DTDL base class.
     * @param kindProperty The property on the DTDL base obverse class that indicates the kind of DTDL element.
     * @param classIsBase True if the material class is the DTDL base class.
     * @param classIsAugmentable True if the material class is augmentable.
     * @param properties A @{link List} of {@link MaterialProperty} objects for the properties of the material class.
     */
    public static void addMembers(
        JavaClass obverseClass,
        String className,
        String baseClassName,
        String kindProperty,
        boolean classIsBase,
        boolean classIsAugmentable,
        List<MaterialProperty> properties) {

        // TODO: Implement the rest.
    }
}
