// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Primitive DataType for building SearchField.
 */
public class PrimitiveType extends ExpandableStringEnum<PrimitiveType>{
//    [typeof(long)] = DataType.Int64,
//        [typeof(double)] = DataType.Double,
//        [typeof(bool)] = DataType.Boolean,
//        [typeof(DateTime)] = DataType.DateTimeOffset,
//        [typeof(DateTimeOffset)] = DataType.DateTimeOffset,
//        [typeof(GeographyPoint)] = DataType.GeographyPoint
    /**
     * Static value Edm.String for DataType.
     */
    public static final PrimitiveType EDM_STRING = fromString(DataType.EDM_STRING.toString());

    /**
     * Static value Edm.Int32 for DataType.
     */
    public static final PrimitiveType EDM_INT32 = fromString(DataType.EDM_INT32.toString());

    /**
     * Static value Edm.EDM_Int64 for DataType.
     */
    public static final PrimitiveType EDM_INT64 = fromString(DataType.EDM_INT64.toString());


    /**
     * Static value Edm.EDM_Double for DataType.
     */
    public static final PrimitiveType EDM_DOUBLE = fromString(DataType.EDM_DOUBLE.toString());

    /**
     * Static value Edm.EDM_Boolean for DataType.
     */
    public static final PrimitiveType EDM_BOOLEAN = fromString(DataType.EDM_BOOLEAN.toString());

    /**
     * Static value Edm.EDM_DateTimeOffset for DataType.
     */
    public static final PrimitiveType EDM_DATE_TIME_OFFSET = fromString(DataType.EDM_DATE_TIME_OFFSET.toString());

    /**
     * Static value Edm.EDM_GeoPoint for DataType.
     */
    public static final PrimitiveType EDM_GEOGRAPHY_POINT = fromString(DataType.EDM_GEOGRAPHY_POINT.toString());

    @JsonCreator
    private static PrimitiveType fromString(String name) {
        return fromString(name, PrimitiveType.class);
    }

    public DataType toDataType() {
        return DataType.fromString(this.toString());
    }
}
