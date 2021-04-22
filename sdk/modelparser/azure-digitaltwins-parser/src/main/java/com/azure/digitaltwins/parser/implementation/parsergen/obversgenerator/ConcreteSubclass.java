// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSorted;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSwitch;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;
import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;
import com.azure.digitaltwins.parser.implementation.parsergen.StringRestriction;

import java.util.Map;

/**
 * Represents a concrete subclass of a class that is materialized in the parser object model.
 */
public class ConcreteSubclass {
    private final ClientLogger logger = new ClientLogger(ConcreteSubclass.class);

    private static final String REGEX_PATTERN_FIELD_SUFFIX = "IdentifierRegexPatternV";
    private int dtdlVersion;
    private String subclassType;
    private String kindValue;
    private String subclassTypeUri;
    private Integer maxIdLength;
    private String pattern;
    private String subClassName;

    /**
     * Initializes a new instance of the {@link ConcreteSubclass} class.
     * @param dtdlVersion The version of DTDL that defines the class and subclass.
     * @param subclassType The type name (DTDL term) of the subclass.
     * @param kindEnum The enum type used to represent DTDL element kind.
     * @param contexts A map that maps from a context Id to a map of term definitions.
     * @param classIdentifierDefinitionRestrictions A map that maps from class name to a map that maps from DTDL version to a {@link StringRestriction} object that describes restrictions on identifiers used in specific classes of definitions.
     */
    public ConcreteSubclass(
        int dtdlVersion,
        String subclassType,
        String kindEnum,
        Map<String, Map<String, String>> contexts,
        Map<String, Map<Integer, StringRestriction>> classIdentifierDefinitionRestrictions) {
        this.subClassName = NameFormatter.formatNameAsClass(subclassType);

        this.dtdlVersion = dtdlVersion;
        this.subclassType = subclassType;
        this.kindValue = kindEnum.concat(".").concat(NameFormatter.formatNameAsEnumValue(subclassType));
        this.subclassTypeUri = contexts.get(ParserGeneratorStringValues.getDtdlContextIdString(dtdlVersion)).get(subclassType);

        Map<Integer, StringRestriction> idRestriction = classIdentifierDefinitionRestrictions.get(subclassType);
        StringRestriction restriction = idRestriction == null ? null : idRestriction.get(dtdlVersion);

        if (idRestriction != null && restriction != null) {
            this.maxIdLength = restriction.getMaxLength();
            this.pattern = restriction.getPattern();
        } else {
            this.maxIdLength = null;
            this.pattern = null;
        }

        // TODO: remove once class is fully implemented.
        logger.info(String.format("%s", this.dtdlVersion));
        logger.info(String.format("%s", this.subclassType));
        logger.info(String.format("%s", this.kindValue));
        logger.info(String.format("%s", this.subclassTypeUri));
        logger.info(String.format("%s", this.maxIdLength));
        logger.info(String.format("%s", this.pattern));
    }

    /**
     * Generate appropriate members for the material class that has this concrete subclass.
     *
     * @param obverseClass A {@link JavaClass} object to which to add the members.
     * @param superclassType The name of the superclass to which the members are being added.
     */
    public void addMembers(JavaClass obverseClass, String superclassType) {
        // TODO: to be implemented.
    }

    /**
     * Add the enum kind for this subclass to an enum variable.
     *
     * @param sorted A {@link JavaSorted} object to which to add the code line.
     * @param varName Name of the variable to which to add the enum value.
     */
    public void addEnumValue(JavaSorted sorted, String varName) {
        // TODO: to be implemented.
    }

    /**
     * Add a case for this subclass to the switch statement in the obverse class method that parses a type string.
     *
     * @param switchOnTypeString A {@link JavaSwitch} object to which to add the code.
     * @param elementInfoVar Name of the variable that holds the new element info object created in this case.
     * @param elementIdVar Name of the variable that holds the identifier of the element.
     * @param parentIdVar Name of the variable that holds the identifier of the parent of the element.
     * @param definedInVar Name of the variable that holds the identifier of the partition or top-level element under which this element is defined.
     */
    public void addCaseToParseTypeStringSwitch(
        JavaSwitch switchOnTypeString,
        String elementInfoVar,
        String elementIdVar,
        String parentIdVar,
        String definedInVar) {
        // TODO: to be implemented.
    }

    /**
     * Add lines to the case for this subclass to the switch statement in the obverse class method that exemplifies a subclass.
     *
     * @param switchOnSubtypeIndex A {@link JavaSwitch} object to which to add the code.
     * @param elementInfoType Encapsulated type of the variable named by elementInfosVar
     * @param elementInfosVar Name of the variable that receives an {@link Iterable} of element information classes.
     * @param classConfiguratorVar Name of the variable that holds a ClassConfigurator object that provides exemplification instructions.
     * @param valueExemplifierVar Name of the variable that holds a ValueExemplifier object for exemplifying literal values.
     * @param descendantRestrictionsVar Name of the variable that holds a list of DescendantRestriction objects.
     * @param parentIdVar Name of the variable that holds the identifier of the parent of the element.
     * @param definedInVar Name of the variable that holds the identifier of the partition or top-level element under which this element is defined.
     * @param propNameVar Name of the variable that holds the name of the property by which the parent refers to this element.
     * @param keyValueVar Name of the variable that holds  value to be used for the key property if the parent exposes a collection of these elements as a map.
     * @param idRequiredVar Name of the variable that holds a boolean value indicating whether an explicit Id is required.
     */
    public void addCaseToExemplifySubclassSwitch(
        JavaSwitch switchOnSubtypeIndex,
        String elementInfoType,
        String elementInfosVar,
        String classConfiguratorVar,
        String valueExemplifierVar,
        String descendantRestrictionsVar,
        String parentIdVar,
        String definedInVar,
        String propNameVar,
        String keyValueVar,
        String idRequiredVar) {
        // TODO: azabbasi: implement
    }

    /**
     * @return Gets the name of the concrete subclass.
     */
    public String getSubClassName() {
        return this.subClassName;
    }
}
