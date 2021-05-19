// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * The column data type of the table returned as a result of a logs query.
 */
public final class ColumnDataType extends ExpandableStringEnum<ColumnDataType> {
    /** Static value bool for ColumnDataType. */
    public static final ColumnDataType BOOL = fromString("bool");

    /** Static value datetime for ColumnDataType. */
    public static final ColumnDataType DATETIME = fromString("datetime");

    /** Static value dynamic for ColumnDataType. */
    public static final ColumnDataType DYNAMIC = fromString("dynamic");

    /** Static value int for ColumnDataType. */
    public static final ColumnDataType INT = fromString("int");

    /** Static value long for ColumnDataType. */
    public static final ColumnDataType LONG = fromString("long");

    /** Static value real for ColumnDataType. */
    public static final ColumnDataType DOUBLE = fromString("real");

    /** Static value string for ColumnDataType. */
    public static final ColumnDataType STRING = fromString("string");

    /**
     * Creates or finds a ColumnDataType from its string representation.
     * @param name a name to look for.
     * @return the corresponding ColumnDataType.
     */
    @JsonCreator
    public static ColumnDataType fromString(String name) {
        return fromString(name, ColumnDataType.class);
    }

    /** @return known ColumnDataType values. */
    public static Collection<ColumnDataType> values() {
        return values(ColumnDataType.class);
    }
}
