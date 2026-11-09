// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Enumeration specifying Priority Level of requests
 */
public final class PriorityLevel {

    private final String name;
    private final byte priorityValue;

    private PriorityLevel(String name, byte priorityValue) {
        this.name = name;
        this.priorityValue = priorityValue;
    }

    /**
     * High Priority level
     */
    public static final PriorityLevel HIGH = new PriorityLevel("High", (byte)1);

    /**
     * Low Priority level
     */
    public static final PriorityLevel LOW = new PriorityLevel("Low", (byte)2);

    /**
     * Gets the corresponding priority level from its string representation.
     *
     * @param name The name of the Cosmos priority level to convert.
     *
     * @return The corresponding Cosmos priority level.
     */
    public static PriorityLevel fromString(String name) {
        checkNotNull(name, "Argument 'name' must not be null.");

        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        switch (normalizedName) {
            case "low": return PriorityLevel.LOW;
            case "high": return PriorityLevel.HIGH;

            default:
                String errorMessage = String.format(
                    "Argument 'name' has invalid value '%s' - valid values are: %s",
                    name,
                    getValidValues());

                throw new IllegalArgumentException(errorMessage);
        }
    }

    @Override
    @JsonValue
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(CosmosMetricCategory.class, this.name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!PriorityLevel.class.isAssignableFrom(obj.getClass())) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (this.name == null) {
            return ((PriorityLevel) obj).name == null;
        } else {
            return this.name.equals(((PriorityLevel) obj).name);
        }
    }

    byte getPriorityValue() {
        return this.priorityValue;
    }

    private static String getValidValues() {
        return new StringJoiner(", ")
            .add(PriorityLevel.HIGH.name)
            .add(PriorityLevel.LOW.name)
            .toString();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.PriorityLevelHelper.setPriorityLevelAccessor(
            new ImplementationBridgeHelpers.PriorityLevelHelper.PriorityLevelAccessor() {

                @Override
                public byte getPriorityValue(PriorityLevel level) {
                    checkNotNull(level, "Argument 'level' must not be null.");
                    return level.getPriorityValue();
                }
            });
    }

    static { initialize(); }
}
