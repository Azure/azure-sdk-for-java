// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Defines an arrow field for a file quick query request.
 */
@Fluent
public class FileQueryArrowField {
    private String name;
    private Integer precision;
    private Integer scale;
    private final FileQueryArrowFieldType type;

    /**
     * Creates a new instance of {@link FileQueryArrowField}.
     *
     * @param type {@link FileQueryArrowFieldType}
     */
    public FileQueryArrowField(FileQueryArrowFieldType type) {
        StorageImplUtils.assertNotNull("type", type);
        this.type = type;
    }

    /**
     * Sets the name of the field.
     *
     * @param name The name of the field.
     * @return The updated options.
     */
    public FileQueryArrowField setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the precision of the field. Required if type is {@link FileQueryArrowFieldType#DECIMAL}.
     *
     * @param precision The precision of the field. Required if type is {@link FileQueryArrowFieldType#DECIMAL}
     * @return The updated options.
     */
    public FileQueryArrowField setPrecision(Integer precision) {
        this.precision = precision;
        return this;
    }

    /**
     * Sets the scale of the field. Required if type is {@link FileQueryArrowFieldType#DECIMAL}.
     *
     * @param scale The scale of the field. Required if type is {@link FileQueryArrowFieldType#DECIMAL}
     * @return The updated options.
     */
    public FileQueryArrowField setScale(Integer scale) {
        this.scale = scale;
        return this;
    }

    /**
     * Gets the name of the field.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the precision of the field.
     *
     * @return The precision.
     */
    public Integer getPrecision() {
        return precision;
    }

    /**
     * Gets the scale of the field.
     *
     * @return The scale.
     */
    public Integer getScale() {
        return scale;
    }

    /**
     * Gets the {@link FileQueryArrowFieldType} of the field.
     *
     * @return {@link FileQueryArrowFieldType}
     */
    public FileQueryArrowFieldType getType() {
        return type;
    }
}
