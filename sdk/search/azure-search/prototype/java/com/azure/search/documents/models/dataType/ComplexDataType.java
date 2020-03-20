package com.azure.search.documents.models.dataType;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.search.documents.models.DataType;
import com.fasterxml.jackson.annotation.JsonCreator;

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
     * @return the corresponding DataType.
     */
    @JsonCreator
    private static ComplexDataType fromString(String name) {
        return fromString(name, ComplexDataType.class);
    }
    public DataType toDataType() {
        return DataType.fromString(this.toString());
    }

}
