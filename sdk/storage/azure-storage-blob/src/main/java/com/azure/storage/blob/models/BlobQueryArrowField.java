// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Defines an arrow field for a blob quick query request.
 */
@Fluent
public class BlobQueryArrowField {
    private String name;
    private Integer precision;
    private Integer scale;
    private final BlobQueryArrowFieldType type;

    /**
     * Creates a new instance of {@link BlobQueryArrowField}.
     *
     * @param type {@link BlobQueryArrowFieldType}
     */
    public BlobQueryArrowField(BlobQueryArrowFieldType type) {
        StorageImplUtils.assertNotNull("type", type);
        this.type = type;
    }

    /**
     * Sets the name of the field.
     *
     * @param name The name of the field.
     * @return The updated options.
     */
    public BlobQueryArrowField setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the precision of the field. Required if type is {@link BlobQueryArrowFieldType#DECIMAL}.
     *
     * @param precision The precision of the field. Required if type is {@link BlobQueryArrowFieldType#DECIMAL}
     * @return The updated options.
     */
    public BlobQueryArrowField setPrecision(Integer precision) {
        this.precision = precision;
        return this;
    }

    /**
     * Sets the scale of the field. Required if type is {@link BlobQueryArrowFieldType#DECIMAL}.
     *
     * @param scale The scale of the field. Required if type is {@link BlobQueryArrowFieldType#DECIMAL}
     * @return The updated options.
     */
    public BlobQueryArrowField setScale(Integer scale) {
        this.scale = scale;
        return this;
    }

    /**
     * Gets the name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the precision.
     *
     * @return The precision.
     */
    public Integer getPrecision() {
        return precision;
    }

    /**
     * Gets the scale.
     *
     * @return The scale.
     */
    public Integer getScale() {
        return scale;
    }

    /**
     * Gets the {@link BlobQueryArrowFieldType}.
     *
     * @return {@link BlobQueryArrowFieldType}
     */
    public BlobQueryArrowFieldType getType() {
        return type;
    }
}
