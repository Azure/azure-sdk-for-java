// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for LogsColumnType. */
public final class LogsColumnType extends ExpandableStringEnum<LogsColumnType> {
    /** Static value bool for LogsColumnType. */
    public static final LogsColumnType BOOL = fromString("bool");

    /** Static value datetime for LogsColumnType. */
    public static final LogsColumnType DATETIME = fromString("datetime");

    /** Static value dynamic for LogsColumnType. */
    public static final LogsColumnType DYNAMIC = fromString("dynamic");

    /** Static value int for LogsColumnType. */
    public static final LogsColumnType INT = fromString("int");

    /** Static value long for LogsColumnType. */
    public static final LogsColumnType LONG = fromString("long");

    /** Static value real for LogsColumnType. */
    public static final LogsColumnType REAL = fromString("real");

    /** Static value string for LogsColumnType. */
    public static final LogsColumnType STRING = fromString("string");

    /** Static value guid for LogsColumnType. */
    public static final LogsColumnType GUID = fromString("guid");

    /** Static value decimal for LogsColumnType. */
    public static final LogsColumnType DECIMAL = fromString("decimal");

    /** Static value timespan for LogsColumnType. */
    public static final LogsColumnType TIMESPAN = fromString("timespan");

    /**
     * Creates or finds a LogsColumnType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding LogsColumnType.
     */
    @JsonCreator
    public static LogsColumnType fromString(String name) {
        return fromString(name, LogsColumnType.class);
    }

    /** @return known LogsColumnType values. */
    public static Collection<LogsColumnType> values() {
        return values(LogsColumnType.class);
    }
}
