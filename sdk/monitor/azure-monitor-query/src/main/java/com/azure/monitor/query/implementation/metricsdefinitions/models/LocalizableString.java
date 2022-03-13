// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.implementation.metricsdefinitions.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The localizable string class. */
@Fluent
public final class LocalizableString {
    private final ClientLogger logger = new ClientLogger(LocalizableString.class);
    /*
     * the invariant value.
     */
    @JsonProperty(value = "value", required = true)
    private String value;

    /*
     * the locale specific value.
     */
    @JsonProperty(value = "localizedValue")
    private String localizedValue;

    /**
     * Creates an instance of LocalizableString class.
     *
     * @param value the value value to set.
     */
    @JsonCreator
    public LocalizableString(@JsonProperty(value = "value", required = true) String value) {
        this.value = value;
    }

    /**
     * Get the value property: the invariant value.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get the localizedValue property: the locale specific value.
     *
     * @return the localizedValue value.
     */
    public String getLocalizedValue() {
        return this.localizedValue;
    }

    /**
     * Set the localizedValue property: the locale specific value.
     *
     * @param localizedValue the localizedValue value to set.
     * @return the LocalizableString object itself.
     */
    public LocalizableString setLocalizedValue(String localizedValue) {
        this.localizedValue = localizedValue;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (getValue() == null) {
            throw logger.logExceptionAsError(
                    new IllegalArgumentException("Missing required property value in model LocalizableString"));
        }
    }
}
