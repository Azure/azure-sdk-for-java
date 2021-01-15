// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.util.Collections;
import java.util.List;

/**
 * Defines the output arrow serialization for a file quick query request.
 */
public class FileQueryArrowSerialization implements FileQuerySerialization {

    private List<FileQueryArrowField> schema;

    /**
     * Gets the arrow fields.
     *
     * @return the arrow fields.
     */
    public List<FileQueryArrowField> getSchema() {
        return schema == null ? null : Collections.unmodifiableList(schema);
    }

    /**
     * Sets the arrow fields.
     *
     * @param schema the arrow fields.
     * @return the updated FileQueryArrowSerialization object.
     */
    public FileQueryArrowSerialization setSchema(List<FileQueryArrowField> schema) {
        this.schema = schema == null ? null : Collections.unmodifiableList(schema);
        return this;
    }
}
