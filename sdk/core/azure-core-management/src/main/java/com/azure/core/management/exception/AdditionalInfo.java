// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An instance of this class provides additional information about a {@link ManagementError}.
 */
@Immutable
public final class AdditionalInfo {
    /**
     * The type of additional info.
     */
    private final String type;

    /**
     * The additional info.
     */
    private final Object info;

    /**
     * Constructs a new {@link AdditionalInfo} object.
     *
     * @param type the type of addition info.
     * @param info the additional info.
     */
    @JsonCreator
    public AdditionalInfo(@JsonProperty("type") String type, @JsonProperty("info") Object info) {
        this.type = type;
        this.info = info;
    }

    /**
     * Gets the type of addition info.
     *
     * @return the type of addition info.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Gets the additional info.
     *
     * @return the additional info.
     */
    public Object getInfo() {
        return this.info;
    }

    @Override
    public String toString() {
        return type == null ? super.toString() : type;
    }
}
