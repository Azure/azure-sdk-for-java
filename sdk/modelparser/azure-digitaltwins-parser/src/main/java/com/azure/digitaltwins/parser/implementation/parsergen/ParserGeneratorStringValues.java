// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

/**
 * Static class that holds values used by the Meta-parser.
 */
public final class ParserGeneratorStringValues {
    private ParserGeneratorStringValues() { }

    /**
     * Type of the identifier for referencing model elements.
     */
    public static final String IDENTIFIER_TYPE = "Dtmi";

    /**
     * Name to use for the identifier property on a material class.
     */
    public static final String IDENTIFIER_NAME = "Id";

    /**
     * Name to use for the defining parent property on a material class.
     */
    public static final String DEFINING_PARENT_NAME = "ChildOf";

    /**
     * Name to use for the defining partition property on a material class.
     */
    public static final String DEFINING_PARTITION_NAME = "DefinedIn";

    /**
     * Name of the internal property in obverse classes that holds the DTDL version in which an instance of the class is defined.
     */
    public static final String DTDL_VERSION_PROPERTY_NAME = "DtdlVersion";

    /**
     * Name of the SupplementalTypes property to generate in augmentable obverse classes.
     */
    public static final String SUPPLEMENTAL_TYPES_PROPERTY_NAME = "SupplementalTypes";

    /**
     * Name of the SupplementalProperties property to generate in augmentable obverse classes.
     */
    public static final String SUPPLEMENTAL_PROPERTIES_PROPERTY_NAME = "SupplementalProperties";

    /**
     * Name of the UndefinedTypes property to generate in obverse classes.
     */
    public static final String UNDEFINED_TYPES_PROPERTY_NAME = "UndefinedTypes";

    /**
     * Name of the UndefinedProperties property to generate in obverse classes.
     */
    public static final String UNDEFINED_PROPERTIES_PROPERTY_NAME = "UndefinedProperties";

    /**
     * Name of the property to generate on a material class to indicate whether it is a partition.
     */
    public static final String IS_PARTITION_PROPERTY_NAME = "IsPartition";

    /**
     * Name of the property to generate on a material class to provide the source JSON object.
     */
    public static final String SOURCE_OBJECT_PROPERTY_NAME = "SourceObject";

    /**
     * Name of the ValidateInstance method to generate in validating obverse classes.
     */
    public static final String VALIDATE_INSTANCE_METHOD_NAME = "ValidateInstance";

    /**
     * Name of the GetJsonLdText method to generate in partition obverse classes.
     */
    public static final String GET_JSON_LD_TEXT_METHOD_NAME = "GetJsonLdText";

    /**
     * Name of the GetJsonLd method to generate in partition obverse classes.
     */
    public static final String GET_JSON_LD_METHOD_NAME = "GetJsonLd";

    /**
     * Name of the pseudo-obverse class used for object property values that indicate obverse references.
     */
    public static final String REFERENCE_OBVERSE_NAME = "Reference";

    /**
     * Prefix for generating field names of properties that shadow other properties when importing via descendant control.
     */
    public static final String SHADDOW_PROPERTY_PREFIX = "original";

    /**
     * The type of boolean values in the java language.
     */
    public static final String OBVERSE_TYPE_BOOLEAN = "boolean";

    /**
     * The type of integer values in the java language.
     */
    public static final String OBVERSE_TYPE_INTEGER = "int";

    /**
     * The type of string values in the java language.
     */
    public static final String OBVERSE_TYPE_STRING = "String";

    /**
     * The keyword for the boolean literal value true in the java language.
     */
    public static final String OBVERSE_VALUE_TRUE = "true";

    /**
     * The keyword for the boolean literal value false in the java language.
     */
    public static final String OBVERSE_VALUE_FALSE = "false";

    /**
     * String form of the URI prefix for DTDL context versions greater than 1.
     */
    public static final String DTDL_CONTEXT_PREFIX = "dtmi:dtdl:context;";

    /**
     * Name of the model elements file to generate from the configured source element files.
     */
    public static final String ELEMENTS_FILE_NAME = "ModelElements.g.json";

    /**
     * Gets the context Id for a given DTDL language version.
     *
     * @param dtdlVersion The version of the DTDL language.
     * @return A string representation of the DTMI for the DTDL context.
     */
    public static String getDtdlContextIdString(int dtdlVersion) {
        return DTDL_CONTEXT_PREFIX + dtdlVersion;
    }

    /**
     * Gets a boolean literal value in the java language.
     *
     * @param value The value of the boolean literal to get.
     * @return A string representation of the java keyword.
     */
    public static String getBooleanLiteral(boolean value) {
        return value ? OBVERSE_VALUE_TRUE : OBVERSE_VALUE_FALSE;
    }
}
