// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Collections;
import java.util.Set;

/**
 * An {@link ObjectInputStream} that restricts deserialization to an explicit
 * allowlist of class names and package prefixes, preventing arbitrary class
 * instantiation (CWE-502).
 *
 * <p>Usage example:
 * <pre>{@code
 * Set<String> allowedClasses = new HashSet<>(Arrays.asList(MyClass.class.getName(), "[B"));
 * Set<String> allowedPrefixes = new HashSet<>(Arrays.asList("com.azure.cosmos."));
 * try (SafeObjectInputStream ois = new SafeObjectInputStream(inputStream, allowedClasses, allowedPrefixes)) {
 *     MyClass obj = (MyClass) ois.readObject();
 * }
 * }</pre>
 */
public final class SafeObjectInputStream extends ObjectInputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeObjectInputStream.class);

    private final Set<String> allowedClassNames;
    private final Set<String> allowedPackagePrefixes;

    /**
     * Creates a SafeObjectInputStream with the specified allowlists.
     *
     * @param in the underlying input stream
     * @param allowedClassNames exact class names permitted for deserialization (e.g. "com.example.MyClass", "[B")
     * @param allowedPackagePrefixes package prefixes permitted for deserialization (e.g. "com.example.internal.")
     * @throws IOException if an I/O error occurs
     */
    public SafeObjectInputStream(InputStream in, Set<String> allowedClassNames, Set<String> allowedPackagePrefixes)
        throws IOException {
        super(in);
        this.allowedClassNames = allowedClassNames != null ? allowedClassNames : Collections.emptySet();
        this.allowedPackagePrefixes = allowedPackagePrefixes != null ? allowedPackagePrefixes : Collections.emptySet();
    }

    /**
     * Creates a SafeObjectInputStream with only exact class name matching (no prefix matching).
     *
     * @param in the underlying input stream
     * @param allowedClassNames exact class names permitted for deserialization
     * @throws IOException if an I/O error occurs
     */
    public SafeObjectInputStream(InputStream in, Set<String> allowedClassNames) throws IOException {
        this(in, allowedClassNames, Collections.emptySet());
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String className = desc.getName();

        if (allowedClassNames.contains(className)) {
            return super.resolveClass(desc);
        }

        for (String prefix : allowedPackagePrefixes) {
            if (className.startsWith(prefix)) {
                return super.resolveClass(desc);
            }
        }

        LOGGER.error("Blocked unauthorized deserialization attempt for class: {}", className);
        throw new InvalidClassException("Unauthorized deserialization attempt", className);
    }
}
