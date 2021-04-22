// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSwitch;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;

import java.util.List;

/**
 * Represents a concrete subclass of a class that is materialized in the parser object model.
 */
public class ExtensibleMaterialClass {
    private final ClientLogger logger = new ClientLogger(ExtensibleMaterialClass.class);

    private final int dtdlVersion;
    private final String typeName;
    private final String className;
    private final String kindEnum;
    private final String kindValue;

    /**
     * Initializes a new instance of the {@link ExtensibleMaterialClass} class.
     *
     * @param dtdlVersion The version of DTDL that defines the class and subclass.
     * @param typeName The type name (DTDL term) of the class.
     * @param kindEnum The enum type used to represent DTDL element kind.
     */
    public ExtensibleMaterialClass(int dtdlVersion, String typeName, String kindEnum) {
        this.dtdlVersion = dtdlVersion;
        this.typeName = typeName;
        this.className = NameFormatter.formatNameAsClass(typeName);
        this.kindEnum = kindEnum;
        this.kindValue = NameFormatter.formatNameAsEnumValue(typeName);

        // TODO: remove once class is fully implemented.
        logger.info(String.format("%s", this.dtdlVersion));
        logger.info(String.format("%s", this.kindValue));
        logger.info(String.format("%s", this.typeName));
        logger.info(String.format("%s", this.className));
        logger.info(String.format("%s", this.kindEnum));
        logger.info(String.format("%s", this.kindValue));
    }

    /**
     * Add a case for this subclass to the switch statement in the obverse class method that parses a type string.
     *
     * @param switchOnExtensionKind A {@link JavaSwitch} object to which to add the code.
     * @param extensibleMaterialSubtypes A list of strings representing the extensible material types that are subtypes of the class.
     * @param parentIdVar Name of the variable that holds the identifier of the parent of the element.
     * @param definedInVar Name of the variable that holds the identifier of the partition or top-level element under which this element is defined.
     */
    public void addCaseToParseTypeStringSwitch(
        JavaSwitch switchOnExtensionKind,
        List<String> extensibleMaterialSubtypes,
        String parentIdVar,
        String definedInVar) {
        // TODO: implement method.
    }
}
