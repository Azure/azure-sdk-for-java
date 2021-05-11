// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;

import java.util.List;
import java.util.Map;

/**
 * Class for adding parsing code to material classes.
 */
public final class MaterialClassParser {
    private static final ClientLogger LOGGER = new ClientLogger(MaterialClassParser.class);

    private MaterialClassParser() { }

    /**
     * Generate code for the constructor of the material class.
     *
     * @param scope A {@link JavaScope} object to which to add the code.
     * @param classIsBase True if the material class is the DTDL base class.
     */
    public static void generateConstructorCode(JavaScope scope, boolean classIsBase) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * Generate appropriate members for the material class.
     *
     * @param dtdlVersions A list of DTDL major version numbers to generate members for.
     * @param obverseClass A {@link JavaClass} object to which to add the members.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param kindEnum The enum type used to represent DTDL element kind.
     * @param kindProperty The property on the DTDL base obverse class that indicates the kind of DTDL element.
     * @param classIsBase True if the material class is the DTDL base class.
     * @param classIsAbstract True if the material class is abstract.
     * @param classIsAugmentable True if the material class is augmentable.
     * @param classIsPartition True if the material class is a partition.
     * @param concreteSubclasses A map from DTDL version to a list of {@link ConcreteSubclass} objects.
     * @param extensibleMaterialClasses A map from DTDL version to a list of {@link ExtensibleMaterialClass} objects.
     * @param extensibleMaterialSubtypes A map from DTDL version to a list of strings, each representing an extensible material subtype of the material class.
     * @param properties A list of {@link MaterialProperty} objects for the properties of the material class.
     */
    public static void addMembers(
        List<Integer> dtdlVersions,
        JavaClass obverseClass,
        String typeName,
        String kindEnum,
        String kindProperty,
        boolean classIsBase,
        boolean classIsAbstract,
        boolean classIsAugmentable,
        boolean classIsPartition,
        Map<Integer, List<ConcreteSubclass>> concreteSubclasses,
        Map<Integer, List<ExtensibleMaterialClass>> extensibleMaterialClasses,
        Map<Integer, List<String>> extensibleMaterialSubtypes,
        List<MaterialProperty> properties) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
