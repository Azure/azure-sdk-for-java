// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;

/**
 * Class for adding code to material classes that are marked as partitions.
 */
public final class MaterialClassPartitioner {
    private static final ClientLogger LOGGER = new ClientLogger(MaterialClassPartitioner.class);

    private MaterialClassPartitioner() { }

    /**
     * Generate code for the constructor of the material class.
     *
     * @param scope A {@link JavaScope} object to which to add the code.
     * @param classIsPartition True if the material class is a partition.
     */
    public static void generateConstructorCode(JavaScope scope, boolean classIsPartition) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * Generate appropriate members for the material class.
     *
     * @param obverseClass A {@link JavaClass} object to which to add the members.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param classIsPartition True if the material class is a partition.
     * @param classIsBase True if the material class is the DTDL base class.
     */
    public static void addMembers(JavaClass obverseClass, String typeName, boolean classIsPartition, boolean classIsBase) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
