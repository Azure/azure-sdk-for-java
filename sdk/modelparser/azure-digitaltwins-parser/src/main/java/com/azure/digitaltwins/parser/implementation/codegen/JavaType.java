// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Generator for a java class
 */
public class JavaType extends JavaDeclaration implements JavaFile {

    private ClientLogger logger = new ClientLogger(JavaType.class);
    private String typeName;

    private String extend;
    private String implement;
    private List<JavaField> fields;
    private List<JavaConstructor> constructors;
    private List<JavaEnum> enums;
    private List<JavaProperty> properties;
    private List<JavaMethod> methods;

    /**
     * Initializes a new instance of the {@link JavaType} class.
     *
     * @param access       Access level of class.
     * @param novelty      Novelty of the class.
     * @param typeName     The name of the class or struct being declared.
     * @param multiplicity Static or Instance.
     * @param extend       Interfaces extended by this interface.
     * @param implement    Interfaces implemented by this interface.
     */
    public JavaType(Access access, Novelty novelty, String typeName, Multiplicity multiplicity, String extend, String implement) {
        super(access, novelty, "class", typeName, multiplicity, Mutability.MUTABLE);

        this.typeName = typeName;

        this.extend = extend;
        this.implement = implement;
        this.extend = extend;
        this.implement = implement;
        this.fields = new ArrayList<>();
        this.constructors = new ArrayList<>();
        this.enums = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    public JavaField addField(
        Access access,
        String type,
        String name,
        String value,
        Multiplicity multiplicity,
        Mutability mutability,
        String description) {

        if (access != Access.PRIVATE && (multiplicity != Multiplicity.STATIC || mutability != Mutability.FINAL)) {
            logger.logThrowableAsError(new StyleException("Field '" + name + "' must be private unless it's static and final. --SA1401."));
        }

        if (multiplicity == Multiplicity.STATIC && mutability == Mutability.FINAL) {
            if (!name.toUpperCase(Locale.getDefault()).equals(name)) {
                logger.logThrowableAsError(new StyleException("Static final field name '" + name + "' must be all uppercase letters. --SA13311"));
            }
        } else {
            if (name.charAt(0) < 'a' || name.charAt(0) > 'z') {
                logger.logThrowableAsError(new StyleException("Field name '" + name + "' must begin with a lowercase letter. --SA1306."));
            }
        }

        if (description != null && !description.endsWith(".")) {
            logger.logThrowableAsError(new StyleException("Documentation text of field '" + name + "' must end with a period. -- SA1629."));
        }

        JavaField field = new JavaField(access, type, name, value, multiplicity, mutability);
        if (description != null) {
            this.addSummary(description);
        }

        this.fields.add(field);
        return field;
    }

    /**
     * Add a java constructor to the class or struct.
     *
     * @param access       Access level of the constructor.
     * @param multiplicity Static or Instance.
     * @return The {@link JavaConstructor} object added.
     */
    public JavaConstructor addConstructor(Access access, Multiplicity multiplicity) {
        JavaConstructor javaConstructor = new JavaConstructor(access, this.typeName, multiplicity);
        this.constructors.add(javaConstructor);
        return javaConstructor;
    }

    /**
     * Add a java property to the class.
     * A property consists of a private field and private getter and setters.
     *
     * @param access Access level of property.
     * @param type   Property type.
     * @param name   Property name.
     * @return The {@link JavaProperty} object added.
     */
    public JavaProperty addProperty(Access access, String type, String name) {
        JavaProperty javaProperty = new JavaProperty(access, name, type)
            .setter(access, "Set " + name + " property")
            .getter(access, "Get the " + name + " property");
        this.properties.add(javaProperty);
        return javaProperty;
    }

    /**
     * Add a java method to the class.
     *
     * @param access       Access level of method.
     * @param novelty      Novelty of the method.
     * @param type         Return type of the method.
     * @param name         Method name.
     * @param multiplicity Static or instance.
     * @return The {@link JavaMethod} object added.
     */
    public JavaMethod addMethod(Access access, Novelty novelty, String type, String name, Multiplicity multiplicity) {
        JavaMethod javaMethod = new JavaMethod(novelty != Novelty.ABSTRACT, access, novelty, type, name, multiplicity, Mutability.MUTABLE);
        this.methods.add(javaMethod);
        return javaMethod;
    }

    /**
     * Add a java enum to the class.
     *
     * @param access   Access level of enum.
     * @param typeName The name of the enum being declared.
     * @param isSorted True if the enum values should be sorted by name.
     * @return The {@link JavaEnum} object added.
     */
    public JavaEnum addEnum(Access access, String typeName, boolean isSorted) {
        JavaEnum javaEnum = new JavaEnum(access, typeName, isSorted);
        this.enums.add(javaEnum);
        return javaEnum;
    }

    /**
     * Determine whether a field with the given name is present on the class.
     *
     * @param name The name of the field to check for.
     * @return True if a field with the name is present.
     */
    public boolean hasField(String name) {
        return fields.stream().map(s -> s.getName()).anyMatch(s -> s.equals(name));
    }

    /**
     * Determine whether a method with the given name is present on the class.
     *
     * @param name The name of the method to check for.
     * @return True if a method with the name is present.
     */
    public boolean hasMethod(String name) {
        return methods.stream().map(s -> s.getName()).anyMatch(s -> s.equals(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(CodeWriter codeWriter) throws IOException {
        this.writeSummaryAndRemarks(codeWriter);
        this.writeAttributes(codeWriter);

        if (this.extend != null || this.implement != null) {
            if (this.extend == null) {
                codeWriter.writeLine(this.getDecoratedName() + "implements " + this.implement);
            } else if (implement == null) {
                codeWriter.writeLine(this.getDecoratedName() + "extends " + this.extend);
            } else {
                codeWriter.writeLine(this.getDecoratedName() + "extends " + this.extend + " implements " + this.implement);
            }
        } else {
            codeWriter.writeLine(this.getDecoratedName());
        }

        codeWriter.openScope();

        this.fields.sort(Comparator.comparing(JavaField::getName));
        for (JavaField javaField : this.fields) {
            javaField.generateCode(codeWriter);
        }

        codeWriter.blank();

        this.constructors.sort(Comparator.comparing(JavaConstructor::getName));
        for (JavaConstructor javaConstructor : this.constructors) {
            javaConstructor.generateCode(codeWriter);
        }

        this.enums.sort(Comparator.comparing(JavaEnum::getName));
        for (JavaEnum javaEnum : this.enums) {
            javaEnum.generateCode(codeWriter);
        }

        this.properties.sort(Comparator.comparing(JavaProperty::getName));
        for (JavaProperty javaProperty : this.properties) {
            javaProperty.generateCode(codeWriter);
        }

        this.methods.sort(Comparator.comparing(JavaMethod::getName));
        for (JavaMethod javaMethod : this.methods) {
            javaMethod.generateCode(codeWriter);
        }

        codeWriter.closeScope();
    }
}
