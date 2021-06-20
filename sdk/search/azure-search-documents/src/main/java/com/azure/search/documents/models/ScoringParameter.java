// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.models.GeoPoint;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.Utility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a parameter value to be used in scoring functions (for example, referencePointParameter).
 */
public final class ScoringParameter {
    private final ClientLogger logger = new ClientLogger(ScoringParameter.class);
    private final String name;
    private final List<String> values;

    private static final String DASH = "-";
    private static final String COMMA = ",";
    private static final String SINGLE_QUOTE = "'";

    /**
     * Constructor to take name value pair string of ScoringParameter. Name and values are separated by dash, and
     * values are separared by comma.
     *
     * @param nameValuePair The dash separated name value pairs.
     */
    public ScoringParameter(String nameValuePair) {
        Objects.requireNonNull(nameValuePair);
        if (!nameValuePair.contains(DASH)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format("The name and value string: %s is invalid.", nameValuePair)));
        }
        this.name = nameValuePair.split(DASH)[0];
        this.values = Arrays.asList(nameValuePair.split(DASH)[1].split(COMMA));
    }

    /**
     * Initializes a new instance of the ScoringParameter class with the given name and string values.
     *
     * @param name Name of the scoring parameter.
     * @param values Values of the scoring parameter.
     * @throws NullPointerException if {@code name} or {@code values} is null.
     */
    @JsonCreator
    public ScoringParameter(@JsonProperty(value = "name") String name,
        @JsonProperty(value = "values") List<String> values) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(values);
        this.name = name;
        // Deep clone the values.
        this.values = new ArrayList<>(values);
    }

    /**
     * Initializes a new instance of the ScoringParameter class with the given name and GeographyPoint value.
     *
     * @param name Name of the scoring parameter.
     * @param value Value of the scoring parameter.
     * @throws NullPointerException If {@code value} is null.
     */
    public ScoringParameter(String name, GeoPoint value) {
        this(name, toLonLatStrings(value));
    }

    private static List<String> toLonLatStrings(GeoPoint point) {
        Objects.requireNonNull(point);
        return Arrays.asList(Utility.formatCoordinate(point.getCoordinates().getLongitude()),
            Utility.formatCoordinate(point.getCoordinates().getLatitude()));
    }

    /**
     * Gets the name of the scoring parameter.
     *
     * @return The name of scoring parameter.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the values of the scoring parameter.
     *
     * @return The values of scoring parameter.
     */
    public List<String> getValues() {
        return new ArrayList<>(values);
    }

    /**
     * Covert {@link ScoringParameter} to string.
     *
     * @return Service accepted string format.
     * @throws IllegalArgumentException if all values in the list are null or empty.
     */
    @Override
    @JsonValue
    public String toString() {
        String flattenValue = values.stream().filter(value -> !CoreUtils.isNullOrEmpty(value))
            .map(ScoringParameter::escapeValue).collect(Collectors.joining(COMMA));
        if (CoreUtils.isNullOrEmpty(flattenValue)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("There must be at least one valid value for scoring parameter values."));
        }
        return name + DASH + flattenValue;
    }

    private static String escapeValue(String value) {
        if (value.contains("'")) {
            value = value.replace("'", "''");
        }
        if (value.contains(COMMA)) {
            value = SINGLE_QUOTE + value + SINGLE_QUOTE;
        }
        return value;
    }
}
