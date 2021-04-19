// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class that abstracts material property information extracted from the meta-model digest provided by the meta-parser.
 */
public class InstanceValidationDigest {
    private final String criteriaText;
    private final Map<Integer, InstanceConditionDigest> elementConditions;
    private final Map<Integer, InstanceConditionDigest> childConditions;

    /**
     * Initializes a new instance of the {@link InstanceValidationDigest} class.
     */
    public InstanceValidationDigest() {
        this.criteriaText = null;
        this.childConditions = new HashMap<>();
        this.elementConditions = new HashMap<>();
    }

    /**
     * Initializes a new instance of the {@link InstanceValidationDigest} class.
     * @param instanceValidationObject A {@link JsonNode} from the metamodel digest containing information about validating instances of the model element.
     */
    public InstanceValidationDigest(JsonNode instanceValidationObject) {
        this.criteriaText = JsonNodeHelper.getTextValue(instanceValidationObject, DtdlStrings.CRITERIA_TEXT);
        this.elementConditions = findAndCreateProperties(instanceValidationObject, DtdlStrings.ELEMENT);
        this.childConditions = findAndCreateProperties(instanceValidationObject, DtdlStrings.EACH_CHILD);
    }

    /**
     *
     * @return Gets a textual description of the validation criteria for an instance of the element.
     */
    public String getCriteriaText() {
        return this.criteriaText;
    }

    /**
     *
     * @return Gets a map that maps from DTDL version to a {@link InstanceConditionDigest} object providing version-specific validation conditions on the element.
     */
    public Map<Integer, InstanceConditionDigest> getElementConditions() {
        return this.elementConditions;
    }

    /**
     *
     * @return Gets a map that maps from DTDL version to a {@link InstanceConditionDigest} object providing version-specific validation conditions on each child of the element.
     */
    public Map<Integer, InstanceConditionDigest> getChildConditions() {
        return this.childConditions;
    }

    private Map<Integer, InstanceConditionDigest> findAndCreateProperties(JsonNode instanceValidationObject, String propertyName) {
        HashMap<Integer, InstanceConditionDigest> map = new HashMap<>();

        for (Iterator<String> it = instanceValidationObject.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            if (JsonNodeHelper.isNumeric(fieldName)) {
                map.put(
                    Integer.parseInt(fieldName),
                    new InstanceConditionDigest(instanceValidationObject.get(fieldName).get(propertyName)));
            }
        }

        return map;
    }
}
