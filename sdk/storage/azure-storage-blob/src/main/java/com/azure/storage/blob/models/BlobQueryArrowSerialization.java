// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.Collections;
import java.util.List;

/**
 * Defines the output arrow serialization for a blob quick query request.
 */
public class BlobQueryArrowSerialization implements BlobQuerySerialization {

    private List<BlobQueryArrowField> schema;

    /**
     * Gets the arrow fields.
     *
     * @return the arrow fields.
     */
    public List<BlobQueryArrowField> getSchema() {
        return schema == null ? null : Collections.unmodifiableList(schema);
    }

    /**
     * Sets the arrow fields.
     *
     * @param schema the arrow fields.
     * @return the updated BlobQueryArrowSerialization object.
     */
    public BlobQueryArrowSerialization setSchema(List<BlobQueryArrowField> schema) {
        this.schema = schema == null ? null : Collections.unmodifiableList(schema);
        return this;
    }
}
