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
     * @param type {@link FileQueryArrowFieldType}
     */
    public FileQueryArrowField(FileQueryArrowFieldType type) {
        StorageImplUtils.assertNotNull("type", type);
        this.type = type;
    }

    /**
     * @param name The name of the field.
     * @return The updated options.
     */
    public FileQueryArrowField setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param precision The precision of the field. Required if type is {@link FileQueryArrowFieldType#DECIMAL}
     * @return The updated options.
     */
    public FileQueryArrowField setPrecision(Integer precision) {
        this.precision = precision;
        return this;
    }

    /**
     * @param scale The scale of the field. Required if type is {@link FileQueryArrowFieldType#DECIMAL}
     * @return The updated options.
     */
    public FileQueryArrowField setScale(Integer scale) {
        this.scale = scale;
        return this;
    }

    /**
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The precision.
     */
    public Integer getPrecision() {
        return precision;
    }

    /**
     * @return The scale.
     */
    public Integer getScale() {
        return scale;
    }

    /**
     * @return {@link FileQueryArrowFieldType}
     */
    public FileQueryArrowFieldType getType() {
        return type;
    }
}
