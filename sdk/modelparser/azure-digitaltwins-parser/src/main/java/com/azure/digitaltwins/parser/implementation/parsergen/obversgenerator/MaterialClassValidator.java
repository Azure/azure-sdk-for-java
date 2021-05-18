// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.parsergen.InstanceValidationDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;

import java.util.List;
import java.util.Map;

/**
 * Class for adding code that validates JSON instances against objects of material classes.
 */
public final class MaterialClassValidator {
    private static final ClientLogger LOGGER = new ClientLogger(MaterialClassValidator.class);

    private MaterialClassValidator() { }

    /**
     * Generate appropriate members for the material class.
     *
     * @param dtdlVersions A list of DTDL major version numbers to generate members for.
     * @param obverseClass A {@link JavaClass} object to which to add the members.
     * @param kindProperty The property on the DTDL base obverse class that indicates the kind of DTDL element.
     * @param classIsBase True if the material class is the DTDL base class.
     * @param classIsAbstract True if the material class is abstract.
     * @param instanceValidationDigest An {@link InstanceValidationDigest} object providing instance validation criteria for the DTDL type.
     * @param propertyDigests A dictionary that maps from property name to a {@link MaterialPropertyDigest} object providing details about the property.
     */
    public static void addMembers(
        List<Integer> dtdlVersions,
        JavaClass obverseClass,
        String kindProperty,
        boolean classIsBase,
        boolean classIsAbstract,
        InstanceValidationDigest instanceValidationDigest,
        Map<String, MaterialPropertyDigest> propertyDigests) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
