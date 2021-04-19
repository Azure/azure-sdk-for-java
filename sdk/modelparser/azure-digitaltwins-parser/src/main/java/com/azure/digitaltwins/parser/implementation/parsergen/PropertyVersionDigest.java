// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Class that abstracts DTDL-version-specific material property information extracted from the metamodel digest provided by the meta-parser.
 */
public class PropertyVersionDigest {

    private final Boolean idRequired;
    private final Boolean typeRequired;
    private final Boolean isAllowed;

    private final String classType;
    private final String defaultLanguage;
    private final String pattern;

    private final Integer maxCount;
    private final Integer minCount;
    private final Integer maxInclusive;
    private final Integer minInclusive;
    private final Integer maxLength;
    private final Integer value;

    private final List<Integer> classVersions;
    private final List<String> values;
    private final List<String> uniqueProperties;

    /**
     * Initializes a new instance of the {@link PropertyVersionDigest} class.
     * @param propertyVersionObject A {@link JsonNode} from the metamodel digest containing DTDL-version-specific information about a material property.
     */
    public PropertyVersionDigest(JsonNode propertyVersionObject) {
        this.idRequired = JsonNodeHelper.getNotNullableBooleanValue(propertyVersionObject, DtdlStrings.ID_REQUIRED);
        this.typeRequired = JsonNodeHelper.getNotNullableBooleanValue(propertyVersionObject, DtdlStrings.TYPE_REQUIRED);
        this.isAllowed = JsonNodeHelper.getNotNullableBooleanValue(propertyVersionObject, DtdlStrings.ALLOWED);

        this.maxCount = JsonNodeHelper.getNullableIntegerValue(propertyVersionObject, DtdlStrings.MAX_COUNT);
        this.minCount = JsonNodeHelper.getNullableIntegerValue(propertyVersionObject, DtdlStrings.MIN_COUNT);
        this.maxInclusive = JsonNodeHelper.getNullableIntegerValue(propertyVersionObject, DtdlStrings.MAX_INCLUSIVE);
        this.minInclusive = JsonNodeHelper.getNullableIntegerValue(propertyVersionObject, DtdlStrings.MIN_INCLUSIVE);
        this.maxLength = JsonNodeHelper.getNullableIntegerValue(propertyVersionObject, DtdlStrings.MAX_LENGTH);
        this.value = JsonNodeHelper.getNullableIntegerValue(propertyVersionObject, DtdlStrings.VALUE);

        this.pattern = JsonNodeHelper.getTextValue(propertyVersionObject, DtdlStrings.PATTERN);
        this.classType = JsonNodeHelper.getTextValue(propertyVersionObject, DtdlStrings.CLASS);
        this.defaultLanguage = JsonNodeHelper.getTextValue(propertyVersionObject, DtdlStrings.DEFAULT_LANGUAGE);

        this.values = JsonNodeHelper.getArrayValues(propertyVersionObject, DtdlStrings.VALUES, String.class);
        this.uniqueProperties = JsonNodeHelper.getArrayValues(propertyVersionObject, DtdlStrings.UNIQUE_PROPERTIES, String.class);
        this.classVersions = JsonNodeHelper.getArrayValues(propertyVersionObject, DtdlStrings.VERSIONS, Integer.class);
    }

    /**
     * @return Gets a value indicating whether an identifier is required for the property.
     */
    public boolean isIdRequired() {
        return this.idRequired;
    }

    /**
     * @return Gets a value indicating whether a type must be specified for the property.
     */
    public boolean isTypeRequired() {
        return this.typeRequired;
    }

    /**
     * @return Gets a value indicating whether the property is allowed to be specified in a model.
     */
    public boolean isAllowed() {
        return this.isAllowed;
    }

    /**
     *
     * @return Gets the class for an object property.
     */
    public String getClassType() {
        return this.classType;
    }

    /**
     * @return Gets the default language code for a language-tagged string literal property.
     */
    public String getDefaultLanguage() {
        return this.defaultLanguage;
    }

    /**
     * @return Gets a regex that constrains the permissible values, or null if no pattern constraint.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * @return Gets the maximum count of permitted values of the property.
     */
    public Integer getMaxCount() {
        return this.maxCount;
    }

    /**
     * @return Gets the minimum count of permitted values of the property.
     */
    public Integer getMinCount() {
        return this.minCount;
    }

    /**
     * @return Gets the maximum permissible value, or null if no maximum.
     */
    public Integer getMaxInclusive() {
        return this.maxInclusive;
    }

    /**
     * @return Gets the minimum permissible value, or null if no minimum.
     */
    public Integer getMinInclusive() {
        return this.minInclusive;
    }

    /**
     * @return Gets the maximum permissible length of a string, or null if no maximum.
     */
    public Integer getMaxLength() {
        return this.maxLength;
    }

    /**
     * @return Gets the allowed versions of a class for an object property.
     */
    public List<Integer> getClassVersions() {
        return this.classVersions;
    }

    /**
     * @return Gets a list of class names that restricts the set of values the property is permitted to have.
     */
    public List<String> getValues() {
        return this.values;
    }

    /**
     * @return Gets a list of names of properties of child elements that must be unique.
     */
    public List<String> getUniqueProperties() {
        return this.uniqueProperties;
    }

    /**
     * @return Gets a value to be assigned to the property if the property is not set in a model.
     */
    public Integer getValue() {
        return this.value;
    }
}
