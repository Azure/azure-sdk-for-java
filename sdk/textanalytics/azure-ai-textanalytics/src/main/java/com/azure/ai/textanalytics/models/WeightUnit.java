// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for WeightUnit. */
public final class WeightUnit extends ExpandableStringEnum<WeightUnit> {
    /** Static value Unspecified for WeightUnit. */
    public static final WeightUnit UNSPECIFIED = fromString("Unspecified");

    /** Static value Kilogram for WeightUnit. */
    public static final WeightUnit KILOGRAM = fromString("Kilogram");

    /** Static value Gram for WeightUnit. */
    public static final WeightUnit GRAM = fromString("Gram");

    /** Static value Milligram for WeightUnit. */
    public static final WeightUnit MILLIGRAM = fromString("Milligram");

    /** Static value Gallon for WeightUnit. */
    public static final WeightUnit GALLON = fromString("Gallon");

    /** Static value MetricTon for WeightUnit. */
    public static final WeightUnit METRIC_TON = fromString("MetricTon");

    /** Static value Ton for WeightUnit. */
    public static final WeightUnit TON = fromString("Ton");

    /** Static value Pound for WeightUnit. */
    public static final WeightUnit POUND = fromString("Pound");

    /** Static value Ounce for WeightUnit. */
    public static final WeightUnit OUNCE = fromString("Ounce");

    /** Static value Grain for WeightUnit. */
    public static final WeightUnit GRAIN = fromString("Grain");

    /** Static value PennyWeight for WeightUnit. */
    public static final WeightUnit PENNY_WEIGHT = fromString("PennyWeight");

    /** Static value LongTonBritish for WeightUnit. */
    public static final WeightUnit LONG_TON_BRITISH = fromString("LongTonBritish");

    /** Static value ShortTonUS for WeightUnit. */
    public static final WeightUnit SHORT_TON_US = fromString("ShortTonUS");

    /** Static value ShortHundredWeightUS for WeightUnit. */
    public static final WeightUnit SHORT_HUNDRED_WEIGHT_US = fromString("ShortHundredWeightUS");

    /** Static value Stone for WeightUnit. */
    public static final WeightUnit STONE = fromString("Stone");

    /** Static value Dram for WeightUnit. */
    public static final WeightUnit DRAM = fromString("Dram");

    /**
     * Creates or finds a WeightUnit from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding WeightUnit.
     */
    public static WeightUnit fromString(String name) {
        return fromString(name, WeightUnit.class);
    }
}
