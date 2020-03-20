package com.azure.search.documents.models.dataType;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.search.documents.models.DataType;
import com.fasterxml.jackson.annotation.JsonCreator;

public class PrimitiveType extends ExpandableStringEnum<PrimitiveType>{
    /**
     * Static value Edm.String for DataType.
     */
    public static final PrimitiveType EDM_STRING = fromString(DataType.EDM_STRING.toString());

    /**
     * Static value Edm.Int32 for DataType.
     */
    public static final PrimitiveType EDM_INT32 = fromString(DataType.EDM_INT32.toString());

    @JsonCreator
    private static PrimitiveType fromString(String name) {
        return fromString(name, PrimitiveType.class);
    }

    public DataType toDataType() {
        return DataType.fromString(this.toString());
    }
}
