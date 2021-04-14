// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
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
        JsonNode idRequiredNode = propertyVersionObject.get(DtdlStrings.ID_REQUIRED);
        this.idRequired = idRequiredNode != null && idRequiredNode.booleanValue();

        JsonNode typeRequiredNode = propertyVersionObject.get(DtdlStrings.TYPE_REQUIRED);
        this.typeRequired = typeRequiredNode != null && typeRequiredNode.booleanValue();

        JsonNode allowedNode = propertyVersionObject.get(DtdlStrings.ALLOWED);
        this.isAllowed = allowedNode != null && allowedNode.booleanValue();

        JsonNode maxCountNode = propertyVersionObject.get(DtdlStrings.MAX_COUNT);
        this.maxCount = maxCountNode != null
            ? maxCountNode.intValue()
            : null;

        JsonNode minCountNode = propertyVersionObject.get(DtdlStrings.MIN_COUNT);
        this.minCount = minCountNode != null
            ? minCountNode.intValue()
            : null;

        JsonNode maxInclusiveNode = propertyVersionObject.get(DtdlStrings.MAX_INCLUSIVE);
        this.maxInclusive = maxInclusiveNode != null
            ? maxInclusiveNode.intValue()
            : null;

        JsonNode minInclusiveNode = propertyVersionObject.get(DtdlStrings.MIN_INCLUSIVE);
        this.minInclusive = minInclusiveNode != null
            ? minInclusiveNode.intValue()
            : null;

        JsonNode maxLengthNode = propertyVersionObject.get(DtdlStrings.MAX_LENGTH);
        this.maxLength = maxLengthNode != null
            ? maxLengthNode.intValue()
            : null;

        JsonNode patternNode = propertyVersionObject.get(DtdlStrings.PATTERN);
        this.pattern = patternNode != null
            ? patternNode.textValue()
            : null;

        JsonNode valueNode = propertyVersionObject.get(DtdlStrings.VALUE);
        this.value = valueNode != null
            ? valueNode.intValue()
            : null;

        JsonNode classTypeNode = propertyVersionObject.get(DtdlStrings.CLASS);
        this.classType = classTypeNode != null
            ? classTypeNode.textValue()
            : null;

        JsonNode defaultLanguageNode = propertyVersionObject.get(DtdlStrings.DEFAULT_LANGUAGE);
        this.defaultLanguage = defaultLanguageNode != null
            ? defaultLanguageNode.textValue()
            : null;

        JsonNode valuesNode = propertyVersionObject.get(DtdlStrings.VALUES);
        if(valuesNode != null && valuesNode.isArray()) {
            this.values = new ArrayList<>();
            valuesNode
                .forEach(jsonNode ->
                    values.add(jsonNode.textValue()));
        } else {
            values = null;
        }

        JsonNode uniquePropertiesNode = propertyVersionObject.get(DtdlStrings.UNIQUE_PROPERTIES);
        if(uniquePropertiesNode != null && uniquePropertiesNode.isArray()) {
            this.uniqueProperties = new ArrayList<>();
            uniquePropertiesNode
                .forEach(jsonNode ->
                    uniqueProperties.add(jsonNode.textValue()));
        } else {
            uniqueProperties = null;
        }

        JsonNode classVersionsNode = propertyVersionObject.get(DtdlStrings.VERSIONS);
        if(classVersionsNode != null && classVersionsNode.isArray()) {
            this.classVersions = new ArrayList<>();
            classVersionsNode
                .forEach(jsonNode ->
                    classVersions.add(jsonNode.intValue()));
        } else {
            classVersions = null;
        }
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
