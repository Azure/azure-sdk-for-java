// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.maps.weather.models;

import com.azure.core.util.ExpandableEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines values for CurrentConditionDuration.
 */
public final class CurrentConditionDuration implements ExpandableEnum<Integer> {
    private static final Map<Integer, CurrentConditionDuration> VALUES = new ConcurrentHashMap<>();

    /**
     * The most current weather conditions.
     */
    public static final CurrentConditionDuration MOST_RECENT = fromValue(0);

    /**
     * Past 6 Hours.
     */
    public static final CurrentConditionDuration PAST_SIX_HOURS = fromValue(6);

    /**
     * Past 24 Hours.
     */
    public static final CurrentConditionDuration PAST_TWENTY_FOUR_HOURS = fromValue(24);

    private final Integer value;

    private CurrentConditionDuration(Integer value) {
        this.value = value;
    }

    /**
     * Creates or finds a CurrentConditionDuration.
     *
     * @param value a value to look for.
     * @return the corresponding CurrentConditionDuration.
     */
    public static CurrentConditionDuration fromValue(Integer value) {
        return VALUES.computeIfAbsent(value, CurrentConditionDuration::new);
    }

    /**
     * Gets known CurrentConditionDuration values.
     *
     * @return known CurrentConditionDuration values.
     */
    public static Collection<CurrentConditionDuration> values() {
        return new ArrayList<>(VALUES.values());
    }

    /**
     * Gets the value of the CurrentConditionDuration instance.
     *
     * @return the value of the CurrentConditionDuration instance.
     */
    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    @JsonValue
    public String toString() {
        return Objects.toString(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }
}
