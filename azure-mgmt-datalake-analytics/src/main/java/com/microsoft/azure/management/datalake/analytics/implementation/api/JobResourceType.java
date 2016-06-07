/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for JobResourceType.
 */
public enum JobResourceType {
    /** Enum value VertexResource. */
    VERTEX_RESOURCE("VertexResource"),

    /** Enum value JobManagerResource. */
    JOB_MANAGER_RESOURCE("JobManagerResource"),

    /** Enum value StatisticsResource. */
    STATISTICS_RESOURCE("StatisticsResource"),

    /** Enum value VertexResourceInUserFolder. */
    VERTEX_RESOURCE_IN_USER_FOLDER("VertexResourceInUserFolder"),

    /** Enum value JobManagerResourceInUserFolder. */
    JOB_MANAGER_RESOURCE_IN_USER_FOLDER("JobManagerResourceInUserFolder"),

    /** Enum value StatisticsResourceInUserFolder. */
    STATISTICS_RESOURCE_IN_USER_FOLDER("StatisticsResourceInUserFolder");

    /** The actual serialized value for a JobResourceType instance. */
    private String value;

    JobResourceType(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a JobResourceType instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a JobResourceType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed JobResourceType object, or null if unable to parse.
     */
    @JsonCreator
    public static JobResourceType fromValue(String value) {
        JobResourceType[] items = JobResourceType.values();
        for (JobResourceType item : items) {
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
