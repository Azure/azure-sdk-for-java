// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for RangeKind. */
public final class RangeKind extends ExpandableStringEnum<RangeKind> {
    /** Static value Number for RangeKind. */
    public static final RangeKind NUMBER = fromString("Number");

    /** Static value Speed for RangeKind. */
    public static final RangeKind SPEED = fromString("Speed");

    /** Static value Weight for RangeKind. */
    public static final RangeKind WEIGHT = fromString("Weight");

    /** Static value Length for RangeKind. */
    public static final RangeKind LENGTH = fromString("Length");

    /** Static value Volume for RangeKind. */
    public static final RangeKind VOLUME = fromString("Volume");

    /** Static value Area for RangeKind. */
    public static final RangeKind AREA = fromString("Area");

    /** Static value Age for RangeKind. */
    public static final RangeKind AGE = fromString("Age");

    /** Static value Information for RangeKind. */
    public static final RangeKind INFORMATION = fromString("Information");

    /** Static value Temperature for RangeKind. */
    public static final RangeKind TEMPERATURE = fromString("Temperature");

    /** Static value Currency for RangeKind. */
    public static final RangeKind CURRENCY = fromString("Currency");

    /**
     * Creates or finds a RangeKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RangeKind.
     */
    public static RangeKind fromString(String name) {
        return fromString(name, RangeKind.class);
    }
}
