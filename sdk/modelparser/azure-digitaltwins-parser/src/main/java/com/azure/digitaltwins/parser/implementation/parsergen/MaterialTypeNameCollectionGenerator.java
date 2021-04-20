// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.azure.digitaltwins.parser.implementation.codegen.JavaMethod;
import com.azure.digitaltwins.parser.implementation.codegen.JavaConstructor;
import com.azure.digitaltwins.parser.implementation.codegen.JavaLibrary;
import com.azure.digitaltwins.parser.implementation.codegen.Access;
import com.azure.digitaltwins.parser.implementation.codegen.Novelty;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.Multiplicity;
import com.azure.digitaltwins.parser.implementation.codegen.Mutability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Code generator for MaterialTypeNameCollection class.
 */
public class MaterialTypeNameCollectionGenerator implements TypeGenerator {
    private final List<String> typeNames;

    /**
     * Initializes a new instance of the {@link MaterialTypeNameCollectionGenerator} class.
     *
     * @param classNames An {@link Iterable} of class names from the DTDL metamodel digest.
     * @param contexts An {@link Iterable} of maps of term definitions.
     */
    public MaterialTypeNameCollectionGenerator(Iterable<String> classNames, Iterable<Map<String, String>> contexts) {
        this.typeNames = new ArrayList<>();

        for (String className : classNames) {
            this.typeNames.add(className);

            for (Map<String, String> termDefinitions : contexts) {
                String classDefinition = termDefinitions.get(className);
                if (classDefinition != null) {
                    typeNames.add(classDefinition);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(JavaLibrary parserLibrary) {
        JavaClass collectionClass = parserLibrary.jClass(
            Access.PACKAGE_PRIVATE,
            Novelty.NORMAL,
            "MaterialTypeNameCollection",
            Multiplicity.STATIC,
            null,
            null);

        collectionClass.addSummary("A collection of all material type names.");
        collectionClass.field(
            Access.PRIVATE,
            "HashSet<String>",
            "TYPE_NAMES",
            "new HashSet<>()",
            Multiplicity.STATIC,
            Mutability.FINAL,
            null);

        JavaConstructor constructor = collectionClass.constructor(Access.PACKAGE_PRIVATE, Multiplicity.STATIC);

        for (String typeName : this.typeNames) {
            constructor.getBody().line("TYPE_NAMES.add(\"" + typeName + "\");");
        }

        JavaMethod isMaterialTypeMethod = collectionClass.method(
            Access.PUBLIC,
            Novelty.NORMAL,
            ParserGeneratorStringValues.OBVERSE_TYPE_BOOLEAN,
            "isMaterialType",
            Multiplicity.STATIC);

        isMaterialTypeMethod.addSummary("Indicates whether a given type is material or supplemental.");
        isMaterialTypeMethod.parameter(ParserGeneratorStringValues.OBVERSE_TYPE_STRING, "typeString", "The type to check.");
        isMaterialTypeMethod.addReturnComment("True if the type is material.");
        isMaterialTypeMethod.getBody().line("return TYPE_NAMES.contains(typeString);");
    }
}
