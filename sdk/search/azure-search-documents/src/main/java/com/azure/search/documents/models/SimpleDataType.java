// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Simple DataType is used to create SimpleSearchField.
 */
public class SimpleDataType extends ExpandableStringEnum<SimpleDataType> {

    public static SimpleDataType primitive(PrimitiveType dataType) {
        return fromString(dataType.toString());
    }

    /**
     * Returns a collection of a specific DataType
     * @param dataType the corresponding DataType
     * @return a Collection of the corresponding DataType
     */
    @JsonCreator
    public static SimpleDataType collection(PrimitiveType dataType) {
        return fromString(String.format("Collection(%s)", dataType.toString()));
    }

    /**
     * Creates or finds a DataType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DataType.
     */
    @JsonCreator
    private static SimpleDataType fromString(String name) {
        return fromString(name, SimpleDataType.class);
    }

    /**
     * Get collections of {@link PrimitiveType} values.
     *
     * @return a Collection of the primitive DataType.
     */
    public static Collection<DataType> values() {
        return values(PrimitiveType.class).stream().map(PrimitiveType::toDataType)
            .collect(Collectors.toList());
    }

    /**
     * Convert {@link SimpleDataType} to {@link DataType}.
     *
     * @return {@link DataType}
     */
    public DataType toDataType() {
        return DataType.fromString(this.toString());
    }
}
