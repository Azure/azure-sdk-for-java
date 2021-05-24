// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.*;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;
import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;

public final class MaterialClassAugmentor {
    private static final ClientLogger LOGGER = new ClientLogger(MaterialClassAugmentor.class);

    private MaterialClassAugmentor() { }

    public static void generateConstructorCode(JavaScope scope, boolean classIsAugmentable) {
        if (classIsAugmentable) {
            scope.jBreak();
            scope.line("this.supplementalTypeIds = new ArrayList<>();");
            scope.line("this.supplementalProperties = new HashMap<>();");
            scope.line("this.supplementalTypes = new ArrayList<>();");
        }
    }

    /**
     * Generate appropriate members for the material class.
     *
     * @param obverseClass A {@link JavaClass} object to which to add the members.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param classIsAugmentable True if the material class is augmentable.
     * @param classIsBase True if the material class is the DTDL base class.
     * @param anyObjectProperties True if the material class as any object properties.
     */
    public static void addMembers(JavaClass obverseClass, String typeName, boolean classIsAugmentable, boolean classIsBase, boolean anyObjectProperties) {
        JavaMethod addTypeMethod = obverseClass.method(Access.PACKAGE_PRIVATE, Novelty.NORMAL, "void", "addType", Multiplicity.INSTANCE);

        if (!classIsBase) {
            addTypeMethod.addAttributes("Override");
        }


        addTypeMethod.addSummary("Add a supplemental type.");
        addTypeMethod.parameter(ParserGeneratorStringValues.IDENTIFIER_TYPE, NameFormatter.formatNameAsParameter(ParserGeneratorStringValues.IDENTIFIER_NAME), "Identifier for supplemental type to add.");
        addTypeMethod.parameter("DTSupplementalTypeInfo", "supplementalType", "{@link DTSupplementalTypeInfo} for the supplemental type.");

        if (classIsAugmentable) {
            addTypeMethod.getBody().line("this.supplementalTypeIds.add(".concat(NameFormatter.formatNameAsParameter(ParserGeneratorStringValues.IDENTIFIER_NAME)).concat(");"));
            addTypeMethod.getBody().line("this.supplementalTypes.add(supplementalType);");
            if (anyObjectProperties) {
                addTypeMethod.getBody().line("supplementalType.attachConstraints(this);");
                addTypeMethod.getBody().line("supplementalType.bindInstanceProperties(this);");
            }
        } else {
            addTypeMethod.getBody().line(
                "throw new Exception(\"attempt to add type"
                    .concat(NameFormatter.formatNameAsParameter(ParserGeneratorStringValues.IDENTIFIER_NAME))
                    .concat("to non-augmentable type ")
                    .concat(NameFormatter.formatNameAsClass(typeName)).
                    concat("\");"));
        }

        JavaProperty supplementalTypeAccessor = obverseClass.property(
            Access.PUBLIC,
            Access.PUBLIC,
            Access.PUBLIC,
            "ArrayList<".concat(ParserGeneratorStringValues.IDENTIFIER_TYPE).concat(">"),
            ParserGeneratorStringValues.SUPPLEMENTAL_PROPERTIES_PROPERTY_NAME,
            "Gets a collection of identifiers, each of which is a {@link Dtmi} that indicates a supplemental type that applies to the DTDL element that corresponds to this object.");
// TODO: azabbasi: Fix property in here.
//        if (classIsAugmentable)
//        {
//            supplementalTypesAccessor.Body().Get()
//                .Line("return this.supplementalTypeIds;");
//        }
//        else
//        {
//            supplementalTypesAccessor.Body().Get()
//                .Line($"return new List<{ParserGeneratorValues.IdentifierType}>();");
//        }
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
