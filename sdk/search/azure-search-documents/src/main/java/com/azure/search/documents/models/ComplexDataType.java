// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Simple DataType is used to create SimpleSearchField.
 */
public class ComplexDataType extends ExpandableStringEnum<ComplexDataType> {
    /**
     * Static value Edm.ComplexType for DataType.
     */
    public static final ComplexDataType EDM_COMPLEX_TYPE = fromString(DataType.EDM_COMPLEX_TYPE.toString());

    /**
     * Returns a collection of a specific DataType
     * @return a Collection of the corresponding DataType
     */
    @JsonCreator
    public static ComplexDataType collection() {
        return fromString(String.format("Collection(%s)", EDM_COMPLEX_TYPE.toString()));
    }

    /**
     * Creates or finds a DataType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding complex DataType.
     */
    @JsonCreator
    private static ComplexDataType fromString(String name) {
        return fromString(name, ComplexDataType.class);
    }

    /**
     * Convert {@link ComplexDataType} to {@link DataType}.
     *
     * @return {@link DataType}
     */
    public DataType toDataType() {
        return DataType.fromString(this.toString());
    }

}
