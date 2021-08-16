// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation.graalvm;

import java.util.EnumSet;
import java.util.Objects;

/**
 * Represents the attributes of a given class that should be made available reflectively as part of a GraalVM native
 * image compilation.
 */
public final class ClassReflectionAttributes {
    private final String name;
    private final EnumSet<ReflectionAttributes> set;

    public enum ReflectionAttributes {
        DECLARED_FIELDS,
        PUBLIC_FIELDS,
        DECLARED_CONSTRUCTORS,
        PUBLIC_CONSTRUCTORS,
        DECLARED_METHODS,
        PUBLIC_METHODS,
        DECLARED_CLASSES,
        PUBLIC_CLASSES
    }

    private ClassReflectionAttributes(String name, EnumSet<ReflectionAttributes> set) {
        this.name = Objects.requireNonNull(name);
        this.set = set;
    }

    public static ClassReflectionAttributes createWithAll(String name) {
        return create(name, EnumSet.allOf(ReflectionAttributes.class));
    }

    public static ClassReflectionAttributes create(String name, EnumSet<ReflectionAttributes> attributes) {
        return new ClassReflectionAttributes(name, attributes);
    }

    public static ClassReflectionAttributes createWithAllDeclared(String name) {
        return new ClassReflectionAttributes(name, EnumSet.of(
            ReflectionAttributes.DECLARED_CLASSES,
            ReflectionAttributes.DECLARED_FIELDS,
            ReflectionAttributes.DECLARED_CONSTRUCTORS,
            ReflectionAttributes.DECLARED_METHODS));
    }

    public static ClassReflectionAttributes createWithAllPublic(String name) {
        return new ClassReflectionAttributes(name, EnumSet.of(
            ReflectionAttributes.PUBLIC_CLASSES,
            ReflectionAttributes.PUBLIC_FIELDS,
            ReflectionAttributes.PUBLIC_CONSTRUCTORS,
            ReflectionAttributes.PUBLIC_METHODS));
    }

    public String getName() {
        return name;
    }

    public boolean includeDeclaredFields() {
        return set.contains(ReflectionAttributes.DECLARED_FIELDS);
    }

    public boolean includePublicFields() {
        return set.contains(ReflectionAttributes.PUBLIC_FIELDS);
    }

    public boolean includeDeclaredConstructors() {
        return set.contains(ReflectionAttributes.DECLARED_CONSTRUCTORS);
    }

    public boolean includePublicConstructors() {
        return set.contains(ReflectionAttributes.PUBLIC_CONSTRUCTORS);
    }

    public boolean includeDeclaredMethods() {
        return set.contains(ReflectionAttributes.DECLARED_METHODS);
    }

    public boolean includePublicMethods() {
        return set.contains(ReflectionAttributes.PUBLIC_METHODS);
    }

    public boolean includeDeclaredClasses() {
        return set.contains(ReflectionAttributes.DECLARED_CLASSES);
    }

    public boolean includePublicClasses() {
        return set.contains(ReflectionAttributes.PUBLIC_CLASSES);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ClassReflectionAttributes that = (ClassReflectionAttributes) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
