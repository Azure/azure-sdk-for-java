// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A secure ObjectInputStream that restricts deserialization to a whitelist of allowed classes.
 * This prevents Remote Code Execution (RCE) attacks via unsafe deserialization.
 */
public class SafeObjectInputStream extends ObjectInputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeObjectInputStream.class);
    private final Set<String> allowedClasses;

    /**
     * Creates a SafeObjectInputStream with the specified allowed classes.
     *
     * @param in the input stream to read from
     * @param allowedClasses the set of fully qualified class names that are allowed to be deserialized
     * @throws IOException if an I/O error occurs
     */
    public SafeObjectInputStream(InputStream in, Set<String> allowedClasses) throws IOException {
        super(in);
        this.allowedClasses = new HashSet<>(allowedClasses);
    }

    /**
     * Creates a SafeObjectInputStream with the specified allowed classes.
     *
     * @param in the input stream to read from
     * @param allowedClasses varargs array of fully qualified class names that are allowed to be deserialized
     * @throws IOException if an I/O error occurs
     */
    public SafeObjectInputStream(InputStream in, String... allowedClasses) throws IOException {
        super(in);
        this.allowedClasses = new HashSet<>(Arrays.asList(allowedClasses));
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String className = desc.getName();
        
        // Always allow primitive types and arrays of primitives
        if (isPrimitiveOrPrimitiveArray(className)) {
            return super.resolveClass(desc);
        }

        // Check if the class is in the allowlist
        if (!allowedClasses.contains(className)) {
            LOGGER.error("Unauthorized deserialization attempt for class: {}", className);
            throw new InvalidClassException("Unauthorized deserialization attempt", className);
        }

        return super.resolveClass(desc);
    }

    /**
     * Checks if the class name represents a primitive type or array of primitives.
     */
    private boolean isPrimitiveOrPrimitiveArray(String className) {
        // Primitive types
        if (className.equals("byte") || className.equals("short") || className.equals("int") ||
            className.equals("long") || className.equals("float") || className.equals("double") ||
            className.equals("boolean") || className.equals("char")) {
            return true;
        }
        
        // Primitive arrays start with [ and use specific type codes
        // [B = byte[], [S = short[], [I = int[], [J = long[], [F = float[], [D = double[], [Z = boolean[], [C = char[]
        if (className.startsWith("[") && className.length() == 2) {
            char typeCode = className.charAt(1);
            return typeCode == 'B' || typeCode == 'S' || typeCode == 'I' || typeCode == 'J' ||
                   typeCode == 'F' || typeCode == 'D' || typeCode == 'Z' || typeCode == 'C';
        }
        
        return false;
    }
}
