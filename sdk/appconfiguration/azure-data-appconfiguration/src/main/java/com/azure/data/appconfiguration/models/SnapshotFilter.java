// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;

/**
 * SnapshotFilter
 */
@Fluent
public final class SnapshotFilter {
    private String key;
    private String label;

    /**
     * Create an intance of SnapshotFilter.
     *
     * @param key Filters key-values by their key field.
     */
    public SnapshotFilter(String key) {
        this.key = key;
    }

    /**
     * Get the key property: Filters key-values by their key field.
     *
     * @return the key value.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Get the label property: Filters key-values by their label field.
     *
     * @return the label value.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Set the label property: Filters key-values by their label field.
     *
     * @param label the label value to set.
     * @return the KeyValueFilter object itself.
     */
    public SnapshotFilter setLabel(String label) {
        this.label = label;
        return this;
    }
}
