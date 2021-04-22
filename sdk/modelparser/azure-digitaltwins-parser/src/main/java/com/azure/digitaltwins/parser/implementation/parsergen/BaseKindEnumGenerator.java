// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.azure.digitaltwins.parser.implementation.codegen.Access;
import com.azure.digitaltwins.parser.implementation.codegen.JavaEnum;
import com.azure.digitaltwins.parser.implementation.codegen.JavaLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Code generator for {@link Enum} that enumerates the kinds of concrete classes that are subclasses of the DTDL base class.
 */
public class BaseKindEnumGenerator implements TypeGenerator {
    private final String baseName;
    private final String typeName;
    private final List<String> elementNames = new ArrayList<>();

    public BaseKindEnumGenerator(Map<String, MaterialClassDigest> materialClasses, String baseName) {
        this.baseName = baseName;
        this.typeName = NameFormatter.formatNameAsEnum(baseName);
        elementNames.add(ParserGeneratorStringValues.REFERENCE_OBVERSE_NAME);
        elementNames.addAll(materialClasses.keySet()
            .stream()
            .filter(s -> !materialClasses.get(s).isAbstract())
            .collect(Collectors.toList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(JavaLibrary parserLibrary) {
        JavaEnum javaEnum = parserLibrary.jEnum(Access.PUBLIC, typeName, true);
        javaEnum.addSummary("Indicates the kind of " + this.baseName + ".");

        for (String elementName : this.elementNames) {
            javaEnum.value(NameFormatter.formatNameAsEnumValue(elementName), "The kind of the " + this.baseName + " is " + elementName + ".");
        }
    }
}
