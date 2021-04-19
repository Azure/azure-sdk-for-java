// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Class that abstracts supplemental constraint information extracted from the meta-model digest provided by the meta-parser.
 */
public class SupplementalConstraintDigest {
    private final String propertyName;
    private final List<String> requiredTypes;
    private final String requiredTypesString;
    private final List<String> requiredValues;
    private final String requiredValueString;

    /**
     * Initializes a new instance of the {@link SupplementalConstraintDigest} class.
     * @param supplementalConstraintObject A {@link JsonNode} from the meta-model digest containing information about a supplemental constraint.
     */
    public SupplementalConstraintDigest(JsonNode supplementalConstraintObject) {
        this.propertyName = JsonNodeHelper.getTextValue(supplementalConstraintObject, DtdlStrings.PROPERTY);
        this.requiredTypes = JsonNodeHelper.getArrayValues(supplementalConstraintObject, DtdlStrings.REQUIRED_TYPES, String.class);
        this.requiredTypesString = JsonNodeHelper.getTextValue(supplementalConstraintObject, DtdlStrings.REQUIRED_TYPES_STRING);
        this.requiredValues = JsonNodeHelper.getArrayValues(supplementalConstraintObject, DtdlStrings.REQUIRED_VALUES, String.class);
        this.requiredValueString = JsonNodeHelper.getTextValue(supplementalConstraintObject, DtdlStrings.REQUIRED_VALUES_STRING);
    }

    /**
     *
     * @return Gets the name of the property.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     *
     * @return Gets a list of type URIs, one of which must apply to the property.
     */
    public List<String> getRequiredTypes() {
        return requiredTypes;
    }

    /**
     *
     * @return Gets a string describing the required types for the property.
     */
    public String getRequiredTypesString() {
        return requiredTypesString;
    }

    /**
     *
     * @return Gets a list of value URIs, one of which must be the value of the property.
     */
    public List<String> getRequiredValues() {
        return requiredValues;
    }

    /**
     *
     * @return Gets a string describing the required values for the property.
     */
    public String getRequiredValueString() {
        return requiredValueString;
    }
}
