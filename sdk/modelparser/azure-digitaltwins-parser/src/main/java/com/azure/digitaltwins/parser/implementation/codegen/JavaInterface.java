// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generator for a java interface.
 */
public class JavaInterface extends JavaDeclaration implements JavaFile {

    private String extend;
    private String implement;
    private String typeName;
    private List<JavaMethod> methods;

    /**
     * Initializes a new instance of the {@link JavaInterface} class.
     *
     * @param access    Access level of interface.
     * @param typeName  The name of the interface being declared.
     * @param extend    Interfaces extended by this interface.
     * @param implement Interfaces implemented by this interface.
     */
    public JavaInterface(Access access, String typeName, String extend, String implement) {
        super(access, Novelty.NORMAL, "interface", typeName, Multiplicity.INSTANCE, Mutability.MUTABLE);
        this.extend = extend;
        this.implement = implement;
        this.methods = new ArrayList<>();
        this.typeName = typeName;
    }

    /**
     * Add a java method to the interface.
     *
     * @param access Access level of method.
     * @param type   Type of method.
     * @param name   Name of method.
     * @return The {@link JavaMethod} object added.
     */
    public JavaMethod method(Access access, String type, String name) {
        JavaMethod javaMethod = new JavaMethod(false, access, Novelty.NORMAL, type, name, Multiplicity.INSTANCE, Mutability.MUTABLE);
        this.methods.add(javaMethod);
        return javaMethod;
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
                codeWriter.writeLine(this.getDecoratedName(null) + " implements " + this.implement, true);
            } else if (implement == null) {
                codeWriter.writeLine(this.getDecoratedName(null) + " extends " + this.extend, true);
            } else {
                codeWriter.writeLine(this.getDecoratedName(null) + " extends " + this.extend + " implements " + this.implement, true);
            }
        } else {
            codeWriter.writeLine(this.getDecoratedName(null), true);
        }

        codeWriter.openScope();

        this.methods.sort(Comparator.comparing(JavaDeclaration::getName));

        for (JavaMethod javaMethod : this.methods) {
            javaMethod.generateCode(codeWriter);
        }

        codeWriter.closeScope();
    }
}
