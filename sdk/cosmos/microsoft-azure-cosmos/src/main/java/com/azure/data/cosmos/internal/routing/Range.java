// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;

@JsonIgnoreProperties({ "empty", "singleValue", "hashMap" })
public final class Range<T extends Comparable<T>> extends JsonSerializable {
    private static final String MIN_PROPERTY = "min";
    private static final String MAX_PROPERTY = "max";
    private static final String IS_MIN_INCLUSIVE_PROPERTY = "isMinInclusive";
    private static final String IS_MAX_INCLUSIVE_PROPERTY = "isMaxInclusive";

    private T minValue;
    private T maxValue;

    public Range() {
        super();
    }

    public Range(String jsonString) {
        super(jsonString);
    }

    public Range(T min, T max, boolean isMinInclusive, boolean isMaxInclusive) {
        this.setMin(min);
        this.setMax(max);
        this.setMinInclusive(isMinInclusive);
        this.setMaxInclusive(isMaxInclusive);
    }

    public static <T extends Comparable<T>> Range<T> getPointRange(T value) {
        return new Range<T>(value, value, true, true);
    }

    public static <T extends Comparable<T>> Range<T> getEmptyRange(T value) {
        return new Range<T>(value, value, true, false);
    }

    public static <T extends Comparable<T>> boolean checkOverlapping(Range<T> range1, Range<T> range2) {
        if (range1 == null || range2 == null || range1.isEmpty() || range2.isEmpty()) {
            return false;
        }

        int cmp1 = range1.getMin().compareTo(range2.getMax());
        int cmp2 = range2.getMin().compareTo(range1.getMax());

        if (cmp1 <= 0 && cmp2 <= 0) {
            return !((cmp1 == 0 && !(range1.isMinInclusive() && range2.isMaxInclusive()))
                    || (cmp2 == 0 && !(range2.isMinInclusive() && range1.isMaxInclusive())));
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public T getMin() {
        if (this.minValue == null) {
            this.minValue = (T) super.get(Range.MIN_PROPERTY);
        }

        return this.minValue;
    }

    public void setMin(T min) {
        this.minValue = min;
        BridgeInternal.setProperty(this, Range.MIN_PROPERTY, min);
    }

    @SuppressWarnings("unchecked")
    public T getMax() {
        if (this.maxValue == null) {
            this.maxValue = (T) super.get(Range.MAX_PROPERTY);
        }

        return this.maxValue;
    }

    public void setMax(T max) {
        this.maxValue = max;
        BridgeInternal.setProperty(this, Range.MAX_PROPERTY, max);
    }

    @JsonProperty("isMinInclusive")
    public boolean isMinInclusive() {
        return super.getBoolean(Range.IS_MIN_INCLUSIVE_PROPERTY);
    }

    public void setMinInclusive(boolean isMinInclusive) {
        BridgeInternal.setProperty(this, Range.IS_MIN_INCLUSIVE_PROPERTY, isMinInclusive);
    }

    @JsonProperty("isMaxInclusive")
    public boolean isMaxInclusive() {
        return super.getBoolean(Range.IS_MAX_INCLUSIVE_PROPERTY);
    }

    public void setMaxInclusive(boolean isMaxInclusive) {
        BridgeInternal.setProperty(this, Range.IS_MAX_INCLUSIVE_PROPERTY, isMaxInclusive);
    }

    public boolean isSingleValue() {
        return this.isMinInclusive() && this.isMaxInclusive() && this.getMin().compareTo(this.getMax()) == 0;
    }

    public boolean isEmpty() {
        return this.getMin().compareTo(this.getMax()) == 0 && !(this.isMinInclusive() && this.isMaxInclusive());
    }

    public boolean contains(T value) {
        int minToValueRelation = this.getMin().compareTo(value);
        int maxToValueRelation = this.getMax().compareTo(value);

        return ((this.isMinInclusive() && minToValueRelation <= 0)
                || (!this.isMinInclusive() && minToValueRelation < 0))
                && ((this.isMaxInclusive() && maxToValueRelation >= 0)
                        || (!this.isMaxInclusive() && maxToValueRelation > 0));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Range<?>))
            return false;
        if (obj == this)
            return true;
        @SuppressWarnings("unchecked")
        Range<T> otherRange = (Range<T>) obj;

        return this.getMin().compareTo(otherRange.getMin()) == 0 && this.getMax().compareTo(otherRange.getMax()) == 0
                && this.isMinInclusive() == otherRange.isMinInclusive()
                && this.isMaxInclusive() == otherRange.isMaxInclusive();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = (hash * 397) ^ this.getMin().hashCode();
        hash = (hash * 397) ^ this.getMax().hashCode();
        hash = (hash * 397) ^ Boolean.compare(this.isMinInclusive(), false);
        hash = (hash * 397) ^ Boolean.compare(this.isMaxInclusive(), false);
        return hash;
    }

    public static class MinComparator<T extends Comparable<T>> implements Comparator<Range<T>> {
        @Override
        public int compare(Range<T> range1, Range<T> range2) {
            int result = range1.getMin().compareTo(range2.getMin());
            if (result != 0 || range1.isMinInclusive() == range2.isMinInclusive()) {
                return result;
            }

            return range1.isMinInclusive() ? -1 : 1;
        }
    }

    public static class MaxComparator<T extends Comparable<T>> implements Comparator<Range<T>> {
        @Override
        public int compare(Range<T> range1, Range<T> range2) {
            int result = range1.getMax().compareTo(range2.getMax());
            if (result != 0 || range1.isMaxInclusive() == range2.isMaxInclusive()) {
                return result;
            }

            return range1.isMaxInclusive() ? 1 : -1;
        }
    }
}