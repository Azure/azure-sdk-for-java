package com.azure.search.documents.models.dataType;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.search.documents.models.DataType;
import com.fasterxml.jackson.annotation.JsonCreator;

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

    public DataType toDataType() {
        return DataType.fromString(this.toString());
    }
}
