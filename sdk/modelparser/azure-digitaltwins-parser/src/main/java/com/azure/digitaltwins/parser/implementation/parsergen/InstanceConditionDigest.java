// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Data-type that abstracts instance validation conditions extracted from the metamodel digest provided by the meta-parser.
 */
public class InstanceConditionDigest {
    private final String jsonType;
    private final String dataType;
    private final List<String> instanceOf;
    private final String pattern;
    private final String hasValue;
    private final String namePattern;
    private final String nameHasValue;

    /**
     * Initializes a new instance of the {@link InstanceConditionDigest} class.
     * @param instanceConditionObject A {@link JsonNode} from the metamodel digest containing conditions that must apply to an instance of the model element.
     */
    public InstanceConditionDigest(JsonNode instanceConditionObject) {
        this.jsonType = JsonNodeHelper.getTextValue(instanceConditionObject, DtdlStrings.JSON_TYPE);
        this.dataType = JsonNodeHelper.getTextValue(instanceConditionObject, DtdlStrings.DATA_TYPE);
        this.instanceOf = JsonNodeHelper.getArrayValues(instanceConditionObject, DtdlStrings.INSTANCE_OF, String.class);
        this.pattern = JsonNodeHelper.getTextValue(instanceConditionObject, DtdlStrings.PATTERN);
        this.hasValue = JsonNodeHelper.getTextValue(instanceConditionObject, DtdlStrings.HAS_VALUE);
        this.namePattern = JsonNodeHelper.getTextValue(instanceConditionObject, DtdlStrings.NAME_PATTERN);
        this.nameHasValue = JsonNodeHelper.getTextValue(instanceConditionObject, DtdlStrings.NAME_HAS_VALUE);
    }

    /**
     * @return Gets a string indicating the type of {@link JsonNode} for the instance, or null if no element type constraint.
     */
    public String getJsonType() {
        return this.jsonType;
    }

    /**
     * @return Gets a string indicating the datatype of the instance, or null if no datatype constraint.
     */
    public String getDataType() {
        return this.dataType;
    }

    /**
     * @return Gets a list of property names whose values' validation criteria must be satisfied by the instance, or null if no such constraint.
     */
    public List<String> getInstanceOf() {
        return this.instanceOf;
    }

    /**
     * @return Gets a regex that constrains string instances, or null if no pattern constraint.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * @return Gets the name of a literal property whose value must match the instance value, or null if no such constraint.
     */
    public String getHasValue() {
        return this.hasValue;
    }

    /**
     * @return Gets a regex that constrains the JSON property name, or null if not a property or no such constraint.
     */
    public String getNamePattern() {
        return this.namePattern;
    }

    /**
     * @return Gets the name of a literal property whose value must match the JSON property name, or null if not a property or no such constraint.
     */
    public String getNameHasValue() {
        return this.nameHasValue;
    }

}
