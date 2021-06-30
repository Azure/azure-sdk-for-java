// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.codegen.implementation.parsergen;

import com.azure.digitaltwins.codegen.implementation.codegen.*;

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
            Multiplicity.INSTANCE,
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

        JavaScope staticDeclaration = collectionClass.staticDeclaration();

        for (String typeName : this.typeNames) {
            staticDeclaration.line("TYPE_NAMES.add(\"" + typeName + "\");");
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
