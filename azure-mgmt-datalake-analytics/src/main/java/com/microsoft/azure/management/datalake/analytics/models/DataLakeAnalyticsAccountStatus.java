/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for DataLakeAnalyticsAccountStatus.
 */
public enum DataLakeAnalyticsAccountStatus {
    /** Enum value Failed. */
    FAILED("Failed"),

    /** Enum value Creating. */
    CREATING("Creating"),

    /** Enum value Running. */
    RUNNING("Running"),

    /** Enum value Succeeded. */
    SUCCEEDED("Succeeded"),

    /** Enum value Patching. */
    PATCHING("Patching"),

    /** Enum value Suspending. */
    SUSPENDING("Suspending"),

    /** Enum value Resuming. */
    RESUMING("Resuming"),

    /** Enum value Deleting. */
    DELETING("Deleting"),

    /** Enum value Deleted. */
    DELETED("Deleted");

    /** The actual serialized value for a DataLakeAnalyticsAccountStatus instance. */
    private String value;

    DataLakeAnalyticsAccountStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a DataLakeAnalyticsAccountStatus instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a DataLakeAnalyticsAccountStatus instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DataLakeAnalyticsAccountStatus object, or null if unable to parse.
     */
    @JsonCreator
    public static DataLakeAnalyticsAccountStatus fromValue(String value) {
        DataLakeAnalyticsAccountStatus[] items = DataLakeAnalyticsAccountStatus.values();
        for (DataLakeAnalyticsAccountStatus item : items) {
            if (item.toValue().equals(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toValue();
    }
}
