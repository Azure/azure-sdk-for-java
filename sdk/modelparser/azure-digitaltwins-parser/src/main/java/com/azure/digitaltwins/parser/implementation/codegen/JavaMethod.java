// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generator for java method.
 */
public class JavaMethod extends JavaDeclaration {

    private List<TypeParameter> typeParameters;
    private List<Parameter> parameters;
    private String returnDescription;
    private List<String> preambleTexts;
    private JavaScope body;

    /**
     * Initializes a new instance of the {@link JavaMethod} class.
     *
     * @param hasBody      True if the method has a body, false for a declaration in an interface.
     * @param access       Access level of method.
     * @param novelty      Novelty of the method.
     * @param type         Type of method.
     * @param name         Name of the method.
     * @param multiplicity Static or Instance.
     * @param mutability   Mutability of the method.
     */
    public JavaMethod(boolean hasBody, Access access, Novelty novelty, String type, String name, Multiplicity multiplicity, Mutability mutability) {
        super(access, novelty, type, name, multiplicity, mutability);

        this.typeParameters = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.returnDescription = null;
        this.preambleTexts = new ArrayList<>();

        if (hasBody) {
            this.body = new JavaScope(null);
        } else {
            this.body = null;
        }
    }

    /**
     * Get the method body.
     *
     * @return The method body.
     */
    public JavaScope getBody() {
        return this.body;
    }

    /**
     * Sets the method body.
     *
     * @param body Body content as a scope.
     * @return The {@link JavaMethod} object itself.
     */
    public JavaMethod setBody(JavaScope body) {
        this.body = body;
        return this;
    }

    /**
     * Add any lines that precede the method body.
     *
     * @param text A line of code text.
     * @return The {@link JavaMethod} object itself.
     */
    public JavaMethod preamble(String text) {
        this.preambleTexts.add(text);
        return this;
    }

    /**
     * Add a type parameter to the method.
     *
     * @param name        Name of type parameter.
     * @param description Optional text description of type parameter.
     * @return The {@link JavaMethod} object itself.
     */
    public JavaMethod typeParam(String name, String description) throws StyleException {
        if (name.charAt(0) != 'T') {
            throw new StyleException("Type parameter name `" + name + "' of method '" + this.getName() + "' must begin with a 'T' -- SA1314");
        }

        if (description != null && !description.endsWith(".")) {
            throw new StyleException("Documentation text of method '" + this.getName() + "' must end with a period -- SA1629.");
        }

        TypeParameter tp = new TypeParameter();
        tp.name = name;
        tp.description = description;

        this.typeParameters.add(tp);
        return this;
    }
    
    /**
     * Add a parameter to the method.
     *
     * @param type        Type of parameter.
     * @param name        Name of parameter.
     * @param description Optional text description of parameter.
     * @return The {@link JavaMethod} object itself.
     * @throws StyleException
     */
    public JavaMethod param(String type, String name, String description) throws StyleException {
        if (description != null && !description.endsWith(".")) {
            throw new StyleException("Documentation text of method '" + this.getName() + "' must end with a period -- SA1629.");
        }

        Parameter param = new Parameter();
        param.type = type;
        param.name = name;
        param.description = description;

        this.parameters.add(param);
        return this;
    }

    /**
     * Describe the return value of the method.
     *
     * @param returnDescription Description of the return value.
     * @return The {@link JavaMethod} object itself.
     */
    public JavaMethod returns(String returnDescription) throws StyleException {
        if (this.getType().equals("void")) {
            throw new StyleException("Void return value of method '" + this.getName() + "' must not be documented. -- SA1617");
        }

        if (!returnDescription.endsWith(".")) {
            throw new StyleException("Documentation text of method '" + this.getName() + "' must end with a period. -- SA1629");
        }

        this.returnDescription = returnDescription;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(CodeWriter codeWriter) throws IOException {
        this.writeSummaryAndRemarks(codeWriter);

        codeWriter.writeLine("/**");

        for (TypeParameter typeParam : this.typeParameters) {
            if (typeParam.description != null) {
                codeWriter.writeLine("* @param " + typeParam.name + " " + typeParam.description);
            }
        }

        for (Parameter param : this.parameters) {
            if (param.description != null) {
                codeWriter.writeLine("* @param " + param.name + " " + param.description);
            }
        }

        if (this.returnDescription != null) {
            codeWriter.writeLine("* @return " + this.returnDescription);
        }

        codeWriter.writeLine("*/");

        this.writeAttributes(codeWriter);

        String typeParams = "";

        if (!this.typeParameters.isEmpty()) {
            typeParams = "<" + String.join(", ", this.typeParameters.stream().map(tp -> tp.name).collect(Collectors.toList())) + ">";
        }

        String paramList = String.join(", ", this.parameters.stream().map(p -> p.type + " " + p.name).collect(Collectors.toList()));
        String terminator = "";

        if (this.body == null) {
            terminator = ";";
        }

        codeWriter.writeLine(this.getDecoratedName() + typeParams + "(" + paramList + ")" + terminator);

        if (!this.preambleTexts.isEmpty()) {
            codeWriter.increaseIndent();
            for (String text : this.preambleTexts) {
                codeWriter.writeLine(text);
            }

            codeWriter.decreaseIndent();
        }

        if (this.body != null) {
            this.body.generateCode(codeWriter);
        } else {
            codeWriter.blank();
        }
    }

    private class TypeParameter {
        public String name;
        public String description;
    }

    private class Parameter {
        public String type;
        public String name;
        public String description;
    }
}
