// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.codegen.implementation.codegen;


import com.azure.core.util.logging.ClientLogger;

import java.util.Locale;

/**
 * Generator for a java property.
 * With java properties, there is a field, a getter and a setter.
 */
public class JavaProperty extends JavaStatement {
    private static final ClientLogger LOGGER = new ClientLogger(JavaProperty.class);

    private final String propertyName;
    private final String propertyType;
    private final JavaField field;
    private final Access getterAccess;
    private final Access setterAccess;
    private final Novelty novelty;
    private JavaMethod getterMethod;
    private JavaMethod setterMethod;

    /**
     * Initializes a new instance of the {@link JavaProperty} class.
     *
     * @param propertyName Name of the property.
     * @param propertyType Type of the property.
     */
    public JavaProperty(Access access, Access getterAccess, Access setterAccess, String propertyName, String propertyType, Novelty novelty) {
        // propertyName should start with a lowercase character.
        if (!Character.isLowerCase(propertyName.charAt(0))) {
            throw LOGGER.logExceptionAsError(new StyleException("Property name '" + propertyName + "' should start with a lowercase character."));
        }

        this.propertyName = propertyName;
        this.propertyType = propertyType;

        this.getterAccess = getterAccess;
        this.setterAccess = setterAccess;
        this.novelty = novelty;

        // Create a private field.
        this.field = new JavaField(access == Access.PROTECTED ? access : Access.PRIVATE, propertyType, propertyName, null, Multiplicity.INSTANCE, Mutability.MUTABLE);
    }

    /**
     * Gets the property name.
     *
     * @return The property name.
     */
    public String getName() {
        return this.propertyName;
    }

    /**
     * Adds a getter method for the private field.
     *
     * @param description Getter method description.
     * @return The {@link JavaProperty} object itself.
     */
    JavaProperty getter(String description) {
        String getterMethodName = getMethodName(this.propertyName, "get");
        JavaMethod getterMethod = new JavaMethod(true, getterAccess, novelty, this.propertyType, getterMethodName, Multiplicity.INSTANCE, Mutability.MUTABLE);

        if (description != null) {
            if (!description.endsWith(".")) {
                throw LOGGER.logExceptionAsError(new StyleException("Documentation text of method '" + getterMethodName + "' must end with a period. -- SA1629"));
            }

            getterMethod.addSummary(description);
            getterMethod.addReturnComment(propertyName + ".");
        }

        JavaScope bodyScope = new JavaScope(null);
        bodyScope.addStatement(
            new JavaLine("return this." + propertyName));

        getterMethod.body(bodyScope);

        this.getterMethod = getterMethod;
        return this;
    }

    /**
     * Adds a setter method for the private field.
     *
     * @param description Setter method description.
     * @return The {@link JavaProperty} object itself.
     */
    JavaProperty setter(String description) {
        String setterMethodName = getMethodName(this.propertyName, "set");
        JavaMethod setterMethod = new JavaMethod(true, setterAccess, novelty, "void", setterMethodName, Multiplicity.INSTANCE, Mutability.MUTABLE);

        if (description != null) {
            if (!description.endsWith(".")) {
                throw LOGGER.logExceptionAsError(new StyleException("Documentation text of method '" + setterMethodName + "' must end with a period. -- SA1629"));
            }

            setterMethod.addSummary(description);
        }

        setterMethod.parameter(this.propertyType, "value", "Property value.");
        JavaScope bodyScope = new JavaScope(null);
        bodyScope.addStatement(
            new JavaLine("this." + this.propertyName + " = value;"));

        setterMethod.body(bodyScope);

        this.setterMethod = setterMethod;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSortingText() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(CodeWriter codeWriter) {
    }

    public JavaMethod getGetterMethod() {
        return this.getterMethod;
    }

    public JavaMethod getSetterMethod() {
        return this.setterMethod;
    }

    public JavaField getPropertyField() {
        return this.field;
    }

    /**
     * Creates a method name using a prefix and a property name.
     * The first character of the property name will be converted to upper case and the prefix will be added to the beginning.
     *
     * @param propertyName Property name.
     * @param prefix       Method prefix.
     * @return Correct method name.
     */
    private static String getMethodName(String propertyName, String prefix) {
        return prefix + propertyName.substring(0, 1).toUpperCase(Locale.getDefault()) + propertyName.substring(1);
    }
}
