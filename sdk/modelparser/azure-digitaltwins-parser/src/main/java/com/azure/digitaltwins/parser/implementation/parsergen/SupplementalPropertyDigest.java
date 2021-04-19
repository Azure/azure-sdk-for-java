// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Class that abstracts supplemental property information extracted from the meta-model digest provided by the meta-parser.
 */
public class SupplementalPropertyDigest {
    private final String typeUri;
    private final Integer maxCount;
    private final Integer minCount;
    private final boolean isPlural;
    private final boolean isOptional;
    private final String dictionaryKey;
    private final String instanceOfProperty;

    /**
     * Initializes a new instance of the {@link SupplementalPropertyDigest} class.
     * @param supplementalPropertyObject A {@link JsonNode} from the meta-model digest containing information about a supplemental property.
     */
    public SupplementalPropertyDigest(JsonNode supplementalPropertyObject) {
        this.typeUri = JsonNodeHelper.getTextValue(supplementalPropertyObject, DtdlStrings.TYPE);
        this.maxCount = JsonNodeHelper.getNullableIntegerValue(supplementalPropertyObject, DtdlStrings.MAX_COUNT);
        this.minCount = JsonNodeHelper.getNullableIntegerValue(supplementalPropertyObject, DtdlStrings.MIN_COUNT);
        this.isPlural = JsonNodeHelper.getNotNullableBooleanValue(supplementalPropertyObject, DtdlStrings.PLURAL);
        this.isOptional = JsonNodeHelper.getNotNullableBooleanValue(supplementalPropertyObject, DtdlStrings.OPTIONAL);
        this.dictionaryKey = JsonNodeHelper.getTextValue(supplementalPropertyObject, DtdlStrings.DICTIONARY_KEY);
        this.instanceOfProperty = JsonNodeHelper.getTextValue(supplementalPropertyObject, DtdlStrings.INSTANCE_OF);
    }
    /**
     *
     * @return Gets the type URI for the property.
     */
    public String getTypeUri() {
        return this.typeUri;
    }

    /**
     *
     * @return Gets the maximum count of permitted values of the property.
     */
    public Integer getMaxCount() {
        return this.maxCount;
    }

    /**
     *
     * @return Gets the minimum count of permitted values of the property.
     */
    public Integer getMinCount() {
        return this.minCount;
    }

    /**
     *
     * @return Gets a value indicating whether the property is plural.
     */
    public boolean isPlural() {
        return this.isPlural;
    }

    /**
     *
     * @return Gets a value indicating whether the property is optional.
     */
    public boolean isOptional() {
        return this.isOptional;
    }

    /**
     *
     * @return Gets the name of a property of a child element that is used for the dictionary key of the property.
     */
    public String getDictionaryKey() {
        return this.dictionaryKey;
    }

    /**
     *
     * @return Gets the name of a property of which this property's value must be an instance.
     */
    public String getInstanceOfProperty() {
        return this.instanceOfProperty;
    }
}
