// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Class that abstracts descendant control information extracted from the meta-model digest provided by the meta-parser.
 */
public class DescendantControlDigest {
    private final int dtdlVersion;
    private final String rootClass;
    private final String definingClass;
    private final List<String> propertyNames;
    private final boolean isNarrow;
    private final String excludeType;
    private final String dataTypeProperty;
    private final Integer maxDepth;
    private final List<String> importProperties;

    /**
     * Initializes a new instance of the {@link DescendantControlDigest} class.
     * @param descendantControlObject A {@link JsonNode} that is the descendant control object extracted from the metamodel digest.
     */
    public DescendantControlDigest(JsonNode descendantControlObject) {
        this.dtdlVersion = JsonNodeHelper.getNullableIntegerValue(descendantControlObject, DtdlStrings.DTDL_VERSION);
        this.rootClass = JsonNodeHelper.getTextValue(descendantControlObject, DtdlStrings.ROOT_CLASS);
        this.definingClass = JsonNodeHelper.getTextValue(descendantControlObject, DtdlStrings.DEFINING_CLASS);
        this.propertyNames = JsonNodeHelper.getArrayValues(descendantControlObject, DtdlStrings.PROPERTIES, String.class);
        this.isNarrow = JsonNodeHelper.getNotNullableBooleanValue(descendantControlObject, DtdlStrings.NARROW);
        this.excludeType = JsonNodeHelper.getTextValue(descendantControlObject, DtdlStrings.EXCLUDE_TYPE);
        this.dataTypeProperty = JsonNodeHelper.getTextValue(descendantControlObject, DtdlStrings.DATA_TYPE_PROPERTY);
        this.maxDepth = JsonNodeHelper.getNullableIntegerValue(descendantControlObject, DtdlStrings.MAX_DEPTH);
        this.importProperties = JsonNodeHelper.getArrayValues(descendantControlObject, DtdlStrings.IMPORT_PROPERTIES, String.class);
    }

    /**
     * @return Gets the DTDL version in which the descendant control is defined.
     */
    public int getDtdlVersion() {
        return this.dtdlVersion;
    }

    /**
     * @return Gets the name of the concrete class the control pertains to.
     */
    public String getRootClass() {
        return this.rootClass;
    }

    /**
     * @return Gets the name of the class in which the the control is defined.
     */
    public String getDefiningClass() {
        return this.definingClass;
    }

    /**
     * @return Gets the names of the properties for which this control is relevant.
     */
    public List<String> getPropertyNames() {
        return this.propertyNames;
    }

    /**
     * @return Gets a value indicating whether whether the descendant hierarchy should be scanned only along relevant properties.
     */
    public boolean isNarrow() {
        return this.isNarrow;
    }

    /**
     * @return Gets the type that is to be excluded from the relevant properties.
     */
    public String getExcludeType() {
        return this.excludeType;
    }

    /**
     * @return Gets the property whose value determines the required data type of the relevant properties.
     */
    public String getDataTypeProperty() {
        return this.dataTypeProperty;
    }

    /**
     * @return Gets the maximum allowed count of relevant properties in a hierarchical chain.
     */
    public Integer getMaxDepth() {
        return this.maxDepth;
    }

    /**
     * @return Gets a list of names of properties whose values should be imported from the relevant descendants.
     */
    public List<String> getImportProperties() {
        return this.importProperties;
    }
}
